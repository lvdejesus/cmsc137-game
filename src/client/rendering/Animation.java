package client.rendering;

public class Animation {
    public Texture[] frames;
    public Float frameDuration;
    public int numFrames;

    public Animation(Texture[] frames, int numFrames, Float frameDuration) {
        this.numFrames = numFrames;
        this.frames = frames;
        this.frameDuration = frameDuration;
    }

    public Animation(Texture[] frames, int numFrames) {
        this(frames, numFrames, null);
    }

    public static Animation fromFile(String fileName, int xCount, float frameDuration) {
        Texture texture = TextureAtlas.get().getRegion(fileName);
        int width = texture.width / xCount;
        Texture[] textures = new Texture[xCount];

        float du = (texture.u2 - texture.u1) / xCount;

        for (int j = 0; j < xCount; j++) {
            textures[j] = new Texture(texture.u1 + du * j, texture.v1,
                    texture.u1 + du * (j + 1),
                    texture.v2, width, texture.height);
        }

        return new Animation(textures, width, frameDuration);
    }

    public static Animation fromFile(String fileName, int xCount, int yCount, int row, float frameDuration) {
        Texture texture = TextureAtlas.get().getRegion(fileName);
        int cellW = texture.width / xCount;
        int cellH = texture.height / yCount;
        return fromPixelRow(fileName, xCount, cellW, cellH, row * cellH, frameDuration);
    }

    /**
     * Extracts an animation row using explicit pixel coordinates.
     * Handles spritesheets with gaps between rows (non-uniform grids).
     *
     * @param xCount    number of columns (frames)
     * @param cellW     width of each frame in pixels
     * @param cellH     height of each frame in pixels
     * @param rowY      Y pixel offset from the TOP of the PNG where this row starts
     * @param frameDuration seconds per frame
     */
    public static Animation fromPixelRow(String fileName, int xCount, int cellW, int cellH, int rowY, float frameDuration) {
        Texture texture = TextureAtlas.get().getRegion(fileName);
        Texture[] textures = new Texture[xCount];

        float imgH = texture.height;
        float regionH = texture.v2 - texture.v1;
        float regionW = texture.u2 - texture.u1;

        // stbi_set_flip_vertically_on_load(true): texture.v2 = PNG-top, texture.v1 = PNG-bottom.
        // For a row at PNG-pixel rowY (from top) with height cellH:
        //   GL-top of cell    = texture.v2 - (rowY / imgH) * regionH
        //   GL-bottom of cell = texture.v2 - ((rowY + cellH) / imgH) * regionH
        float vCellTop = texture.v2 - (rowY / imgH) * regionH;
        float vCellBot = texture.v2 - ((rowY + cellH) / imgH) * regionH;

        float insetU = 0.5f / texture.width;
        float insetV = 0.5f / imgH;
        float du = regionW / xCount;

        for (int j = 0; j < xCount; j++) {
            textures[j] = new Texture(
                    texture.u1 + du * j       + insetU, vCellBot + insetV,
                    texture.u1 + du * (j + 1) - insetU, vCellTop - insetV,
                    cellW, cellH);
        }

        return new Animation(textures, cellW, frameDuration);
    }
    public static Animation fromFrames(String fileName, int xCount, int yCount, int[][] frameCoords, float frameDuration) {
        Texture texture = TextureAtlas.get().getRegion(fileName);
        int width = texture.width / xCount;
        int height = texture.height / yCount;
        Texture[] textures = new Texture[frameCoords.length];

        float du = (texture.u2 - texture.u1) / xCount;
        float dv = (texture.v2 - texture.v1) / yCount;

        for (int i = 0; i < frameCoords.length; i++) {
            int col = frameCoords[i][0];
            int pngRow = frameCoords[i][1];
            // stbi flip: PNG row 0 (top) = highest atlas v. Convert accordingly.
            int atlasRow = (yCount - 1) - pngRow;
            float cellV1 = texture.v1 + dv * atlasRow;
            float cellV2 = texture.v1 + dv * (atlasRow + 1);

            float insetU = 0.5f / texture.width;
            float insetV = 0.5f / texture.height;
            // Extra crop on the high-v edge (= PNG-top of this row) to hide foot bleed from the row above
            float extraTopCrop = 6.0f / texture.height;

            textures[i] = new Texture(
                    texture.u1 + du * col + insetU, cellV1 + insetV,
                    texture.u1 + du * (col + 1) - insetU, cellV2 - insetV - extraTopCrop,
                    width, height);
        }

        return new Animation(textures, width, frameDuration);
    }

    static Animation createStaticAnimation(String fileName) {
        Texture texture = TextureAtlas.get().getRegion(fileName);
        if (texture == null) {
            throw new RuntimeException(fileName + " not found in the atlas.");
        }
        return new Animation(new Texture[] { texture }, 1);
    }
}
