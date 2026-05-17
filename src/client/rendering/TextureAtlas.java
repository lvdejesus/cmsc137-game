package client.rendering;

import common.ResourceLoader;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBImage.*;

public class TextureAtlas {
    private static TextureAtlas instance;
    private final Map<String, Texture> regions = new HashMap<>();
    private int textureID;
    private final int ATLAS_SIZE = 4096;

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

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, ATLAS_SIZE, ATLAS_SIZE, 0, GL_RGBA, GL_UNSIGNED_BYTE,
            (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        String[] roots = {"res/textures", "res/menu_assets"};
        List<String[]> textures = new ArrayList<>();

        for (String root : roots) {
            List<String> files = ResourceLoader.list(root);
            for (String file : files) {
                if (!file.toLowerCase().endsWith(".png")) continue;
                String relativePath = file.substring(root.length() + 1);
                System.out.println("[TextureAtlas] Found: " + relativePath + " at " + file);
                textures.add(new String[]{relativePath, file});
            }
        }

        int curX = 0;
        int curY = 0;
        int maxHeightInRow = 0;

        for (String[] entry : textures) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer w = stack.mallocInt(1);
                IntBuffer h = stack.mallocInt(1);
                IntBuffer comp = stack.mallocInt(1);

                stbi_set_flip_vertically_on_load(true);
                byte[] bytes = ResourceLoader.read(entry[1]);
                ByteBuffer imageBuffer = BufferUtils.createByteBuffer(bytes.length);
                imageBuffer.put(bytes);
                imageBuffer.flip();

                ByteBuffer data = stbi_load_from_memory(imageBuffer, w, h, comp, 4);

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

                regions.put(entry[0], new Texture(u1, v1, u2, v2, imgW, imgH));

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

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, textureID);
    }
}
