package framework.rendering;

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

    static Animation createStaticAnimation(String fileName) {
        Texture texture = TextureAtlas.get().getRegion(fileName);
        if (texture == null) {
            throw new RuntimeException(fileName + " not found in the atlas.");
        }
        return new Animation(new Texture[] { texture }, 1);
    }
}
