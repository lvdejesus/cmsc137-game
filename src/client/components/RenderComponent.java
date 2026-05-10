package client.components;

import framework.engine.Component;
import client.rendering.Texture;
import org.joml.Vector4f;

public class RenderComponent implements Component {
    public Texture texture;
    public float z;
    public Vector4f tint;

    public RenderComponent(Texture texture, float z, Vector4f tint) {
        this.texture = texture;
        this.z = z;
        this.tint = tint;
    }

    public RenderComponent(Texture texture, float z) {
        this(texture, z, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f));
    }

    public RenderComponent(Texture texture) {
        this(texture, 0);
    }

    public RenderComponent() {
        this(null);
    }
}
