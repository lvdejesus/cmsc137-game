package common;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
import java.util.zip.*;

public class ResourceLoader {

    public static InputStream getStream(String path) {
        InputStream is = ResourceLoader.class.getClassLoader().getResourceAsStream(path);
        if (is != null) return is;
        try {
            return Files.newInputStream(Paths.get(path));
        } catch (IOException e) {
            throw new RuntimeException("Resource not found: " + path, e);
        }
    }

    public static byte[] read(String path) {
        try (InputStream is = getStream(path)) {
            return is.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read resource: " + path, e);
        }
    }

    public static boolean exists(String path) {
        if (ResourceLoader.class.getClassLoader().getResource(path) != null) return true;
        return Files.exists(Paths.get(path));
    }

    public static List<String> list(String dir) {
        String dirNormalized = dir.replace('\\', '/');
        if (dirNormalized.endsWith("/")) dirNormalized = dirNormalized.substring(0, dirNormalized.length() - 1);
        final String dirPrefix = dirNormalized;

        Path dirPath = Paths.get(dirNormalized);
        if (Files.isDirectory(dirPath)) {
            try (Stream<Path> paths = Files.walk(dirPath)) {
                return paths
                    .filter(Files::isRegularFile)
                    .map(p -> dirPrefix + "/" + dirPath.relativize(p).toString().replace("\\", "/"))
                    .collect(Collectors.toList());
            } catch (IOException ignored) {}
        }

        List<String> results = new ArrayList<>();
        try {
            var location = ResourceLoader.class.getProtectionDomain().getCodeSource().getLocation();
            if (location != null) {
                var uri = location.toURI();
                if (uri.getPath().endsWith(".jar")) {
                    try (ZipFile zf = new ZipFile(new File(uri))) {
                        Enumeration<? extends ZipEntry> entries = zf.entries();
                        while (entries.hasMoreElements()) {
                            String name = entries.nextElement().getName();
                            if (name.startsWith(dirPrefix + "/") && !name.endsWith("/")) {
                                results.add(name);
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {}

        return results;
    }
}
