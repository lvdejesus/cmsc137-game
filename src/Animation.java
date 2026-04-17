public class Animation {
    Texture[] frames;
    Float frameDuration;

    float width;
    float height;

    public Animation(Texture[] frames, float width, float height) {
        this.width = width;
        this.height = height;
        this.frames = frames;
        this.frameDuration = null;
    }

    public Animation(Texture[] frames, float width, float height, float frameDuration) {
        this.width = width;
        this.height = height;
        this.frames = frames;
        this.frameDuration = frameDuration;
    }

    static Animation fromFile(String fileName, int width, int height, float frameDuration) {
        Texture texture = TextureAtlas.get().getRegion(fileName);
        int xCount = texture.width / width;
        int yCount = texture.height / height;
        Texture[] textures = new Texture[xCount * yCount];

        float du = (texture.u2 - texture.u1) / xCount;
        float dv = (texture.v2 - texture.v1) / yCount;
        System.out.printf("%d %d\n", xCount, yCount);

        for (int i = 0; i < yCount; i++) {
            for (int j = 0; j < xCount; j++) {
                textures[i * xCount + j] = new Texture(texture.u1 + du * j, texture.v1 + dv * i,
                        texture.u1 + du * (j + 1),
                        texture.v1 + dv * (i + 1), width, height);
            }
        }

        return new Animation(textures, width, height, frameDuration);
    }

    static Animation createStaticAnimation(String fileName) {
        Texture texture = TextureAtlas.get().getRegion(fileName);
        if (texture == null) {
            throw new RuntimeException(fileName + " not found in the atlas.");
        }
        return new Animation(new Texture[] { texture }, texture.width, texture.height);
    }
}
