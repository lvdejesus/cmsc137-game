package common;

import java.io.*;
import java.net.URL;
import java.nio.file.*;

public class NativeLoader {

    public static void loadNatives() {
        URL location = NativeLoader.class.getProtectionDomain().getCodeSource().getLocation();
        if (location == null || !location.getPath().endsWith(".jar")) return;

        String os = System.getProperty("os.name").toLowerCase();
        String nativeDir;
        String[] libs;

        if (os.contains("linux")) {
            nativeDir = "natives/x64/linux/";
            libs = new String[]{"libglfw.so", "liblwjgl.so", "liblwjgl_opengl.so", "liblwjgl_stb.so", "libopenal.so"};
        } else if (os.contains("win")) {
            nativeDir = "natives/x64/windows/";
            libs = new String[]{"glfw.dll", "lwjgl.dll", "lwjgl_opengl.dll", "lwjgl_stb.dll", "OpenAL.dll"};
        } else {
            return;
        }

        try {
            Path tempDir = Files.createTempDirectory("cmsc137-natives-");
            tempDir.toFile().deleteOnExit();

            for (String lib : libs) {
                String resourcePath = nativeDir + lib;
                try (InputStream is = NativeLoader.class.getClassLoader().getResourceAsStream(resourcePath)) {
                    if (is == null) continue;
                    Path target = tempDir.resolve(lib);
                    Files.copy(is, target, StandardCopyOption.REPLACE_EXISTING);
                    target.toFile().deleteOnExit();
                }
            }

            String tempPath = tempDir.toAbsolutePath().toString();
            System.setProperty("org.lwjgl.librarypath", tempPath);
            System.setProperty("java.library.path",
                tempPath + File.pathSeparator + System.getProperty("java.library.path", ""));
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract native libraries", e);
        }
    }
}
