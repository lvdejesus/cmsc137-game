package client.rendering;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTTPackContext;
import org.lwjgl.stb.STBTTPackedchar;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL33.*;

public class Font {
    private int textureID;
    private final Map<Character, Glyph> glyphs = new HashMap<>();
    private final int fontSize;
    private final float ascent;
    private final float descent;
    private final float lineGap;

    public Font(ByteBuffer ttfBuffer, int fontSize) {
        this.fontSize = fontSize;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            STBTTFontinfo fontInfo = STBTTFontinfo.malloc();
            int fontOffset = STBTruetype.stbtt_GetFontOffsetForIndex(ttfBuffer, 0);
            if (!STBTruetype.stbtt_InitFont(fontInfo, ttfBuffer, fontOffset))
                throw new RuntimeException("Failed to initialize font");

            float scale = STBTruetype.stbtt_ScaleForPixelHeight(fontInfo, fontSize);
            IntBuffer ascent = stack.mallocInt(1);
            IntBuffer descent = stack.mallocInt(1);
            IntBuffer lineGap = stack.mallocInt(1);

            STBTruetype.stbtt_GetFontVMetrics(fontInfo, ascent, descent, lineGap);
            this.ascent = ascent.get() * scale;
            this.descent = descent.get() * scale;
            this.lineGap = lineGap.get() * scale;
            generateFontAtlas(ttfBuffer, fontSize);
        }
    }

    public float getAscent() {
        return ascent;
    }

    public float getDescent() {
        return descent;
    }

    public float getLineGap() {
        return lineGap;
    }

    private void generateFontAtlas(ByteBuffer ttfBuffer, float fontSize) {
        int atlasWidth = 1024;
        int atlasHeight = 1024;
        ByteBuffer atlasBitmap = BufferUtils.createByteBuffer(atlasWidth * atlasHeight);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            STBTTPackContext pc = STBTTPackContext.malloc(stack);
            STBTTPackedchar.Buffer packedChars = STBTTPackedchar.malloc(95, stack); // 32 to 126
            STBTruetype.stbtt_PackBegin(pc, atlasBitmap, atlasWidth, atlasHeight, 0, 1, 0);
            STBTruetype.stbtt_PackFontRange(pc, ttfBuffer, 0, fontSize, 32, packedChars);
            STBTruetype.stbtt_PackEnd(pc);

            for (int i = 0; i < 95; i++) {
                STBTTPackedchar pcData = packedChars.get(i);
                char c = (char) (32 + i);

                float u1 = pcData.x0() / (float) atlasWidth;
                float v1 = pcData.y0() / (float) atlasHeight;
                float u2 = pcData.x1() / (float) atlasWidth;
                float v2 = pcData.y1() / (float) atlasHeight;

                glyphs.put(c, new Glyph(u1, v2, u2, v1, pcData.x1() - pcData.x0(), pcData.y1() - pcData.y0(), pcData.xoff(), pcData.yoff(), pcData.xadvance()));
            }
        }

        int[] swizzle = {GL_ONE, GL_ONE, GL_ONE, GL_RED};

        textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteriv(GL_TEXTURE_2D, GL_TEXTURE_SWIZZLE_RGBA, swizzle);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, atlasWidth, atlasHeight, 0, GL_RED, GL_UNSIGNED_BYTE, atlasBitmap);
    }

    public Glyph getGlyph(char c) {
        return glyphs.get(c);
    }

    public int getTextureID() {
        return textureID;
    }

    public record Glyph(float u1, float v1, float u2, float v2, int width, int height, float xOffset, float yOffset,
                        float xAdvance) {
    }
}