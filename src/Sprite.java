import org.joml.Vector3f;
import org.joml.Vector2f;

public class Sprite {
    public Vector3f position;
    public Vector2f scale;
    public float rotation;

    private Animation animation;
    private float offset;

    public Sprite(String fileName, float x, float y, float z) {
        this(Animation.createStaticAnimation(fileName), x, y, z);
    }

    public Sprite(Animation animation, float x, float y, float z) {
        this.animation = animation;
        this.offset = 0.0f;
        this.position = new Vector3f(x, y, z);
        this.scale = new Vector2f(animation.width, animation.height);
        this.rotation = 0.0f;
    }

    public Texture getTexture(double time) {
        if (animation.frameDuration == null) {
            return animation.frames[0];
        }

        int index = (int) Math.floor((time - offset) / animation.frameDuration) % animation.frames.length;
        return animation.frames[index];
    }
}
