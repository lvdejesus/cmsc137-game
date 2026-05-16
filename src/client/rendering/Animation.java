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

    public static Animation fromFile(String fileName, int numFrames, float frameDuration) {
        Texture texture = TextureAtlas.get().getRegion(fileName);
        int width = texture.width / numFrames;
        Texture[] textures = new Texture[numFrames];

        float du = (texture.u2 - texture.u1) / numFrames;

        for (int j = 0; j < numFrames; j++) {
            textures[j] = new Texture(texture.u1 + du * j, texture.v1,
                    texture.u1 + du * (j + 1),
                    texture.v2, width, texture.height);
        }

        return new Animation(textures, numFrames, frameDuration);
    }

    static Animation createStaticAnimation(String fileName) {
        Texture texture = TextureAtlas.get().getRegion(fileName);
        if (texture == null) {
            throw new RuntimeException(fileName + " not found in the atlas.");
        }
        return new Animation(new Texture[] { texture }, 1);
    }
}
