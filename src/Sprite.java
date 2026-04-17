import org.joml.Vector3f;
import org.joml.Vector2f;

public class Sprite {
    public Vector3f position;
    public Vector2f scale;
    public float rotation;
    private Texture texture;

    public Sprite(String fileName, float x, float y, float z) {
        this.texture = TextureAtlas.get().getRegion(fileName);

        if (texture == null) {
            throw new RuntimeException(fileName + " not found in the atlas.");
        }

        this.position = new Vector3f(x, y, z);
        this.scale = new Vector2f(texture.width, texture.height);
        this.rotation = 0.0f;
    }

    public Texture getTexture() {
        return texture;
    }
}
