package client.rendering;

import org.lwjgl.system.MemoryStack;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBImage.*;

record TextureEntry(String name, String path) {}

public class TextureAtlas {
    private static TextureAtlas instance;
    private final Map<String, Texture> regions = new HashMap<>();
    private int textureID;
    private final int ATLAS_SIZE = 2048;

    private TextureAtlas() {
        init();
    }

    public static TextureAtlas get() {
        if (instance == null)
            instance = new TextureAtlas();
        return instance;
    }

    private void init() {
        textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);

        // Initialize empty texture
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, ATLAS_SIZE, ATLAS_SIZE, 0, GL_RGBA, GL_UNSIGNED_BYTE,
                (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        Path[] roots = { Paths.get("res/textures"), Paths.get("res/menu_assets") };
        List<TextureEntry> textures = new ArrayList<>();

        for (Path root : roots) {
            if (!Files.exists(root)) continue;
                    try (Stream<Path> stream = Files.walk(root)) {
                        List<TextureEntry> rootTextures = stream
                                .filter(Files::isRegularFile)
                                .filter(path -> path.toString().toLowerCase().endsWith(".png"))
                                .map(path -> {
                                    String relativePath = root.relativize(path).toString().replace("\\", "/");
                                    System.out.println("[TextureAtlas] Found: " + relativePath + " at " + path);
                                    return new TextureEntry(relativePath, path.toString());
                                }).toList();
                        textures.addAll(rootTextures);
                    } catch (IOException e) {
                e.printStackTrace();
            }
        }

        int curX = 0;
        int curY = 0;
        int maxHeightInRow = 0;

        for (TextureEntry file : textures) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer w = stack.mallocInt(1);
                IntBuffer h = stack.mallocInt(1);
                IntBuffer comp = stack.mallocInt(1);

                stbi_set_flip_vertically_on_load(true);
                ByteBuffer data = stbi_load(file.path(), w, h, comp, 4);

                if (data == null)
                    continue;

                int imgW = w.get();
                int imgH = h.get();

                if (curX + imgW > ATLAS_SIZE) {
                    curX = 0;
                    curY += maxHeightInRow + 2; // Added 2px vertical padding
                    maxHeightInRow = 0;
                }

                if (curY + imgH > ATLAS_SIZE) {
                    throw new RuntimeException("Ran out of space in atlas.");
                }

                glTexSubImage2D(GL_TEXTURE_2D, 0, curX, curY, imgW, imgH, GL_RGBA, GL_UNSIGNED_BYTE, data);

                float u1 = (float) curX / ATLAS_SIZE;
                float v1 = (float) curY / ATLAS_SIZE;
                float u2 = (float) (curX + imgW) / ATLAS_SIZE;
                float v2 = (float) (curY + imgH) / ATLAS_SIZE;

                regions.put(file.name(), new Texture(u1, v1, u2, v2, imgW, imgH));

                curX += imgW + 2; // Added 2px horizontal padding
                maxHeightInRow = Math.max(maxHeightInRow, imgH);

                stbi_image_free(data);
            }
        }
    }

    public Texture getRegion(String name) {
        Texture texture = regions.get(name);
        if (texture == null) {
            throw new RuntimeException(name + " is not in the atlas.");
        }
        return texture;
    }

    public Iterable<String> listRegions() {
        return regions.keySet();
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, textureID);
    }
}
