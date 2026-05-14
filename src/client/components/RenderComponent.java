package client.components;

import framework.engine.Component;
import client.rendering.Texture;
import org.joml.Vector4f;

public class RenderComponent implements Component {
    public String layer;
    public Texture texture;
    public float z;
    public Vector4f tint;

    // For upgrades
    public boolean isFlipping = false;
    public boolean facingBack = true;
    public Texture backTex;

    public RenderComponent(Texture texture, float z, Vector4f tint, String layer) {
        this.texture = texture;
        this.z = z;
        this.tint = tint;
        this.layer = layer;
    }

    public RenderComponent(Texture texture, float z, Vector4f tint) {
        this(texture, z, tint, "default");
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
