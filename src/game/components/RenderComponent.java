package game.components;

import framework.engine.Component;
import game.rendering.Texture;
import org.joml.Vector4f;

public class RenderComponent implements Component {
    public Texture texture;
    public int z;
    public Vector4f tint;

    public RenderComponent(Texture texture, int z, Vector4f tint) {
        this.texture = texture;
        this.z = z;
        this.tint = tint;
    }

    public RenderComponent(Texture texture, int z) {
        this(texture, z, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f));
    }

    public RenderComponent(Texture texture) {
        this(texture, 0);
    }

    public RenderComponent() {
        this(null);
    }
}
