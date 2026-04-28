package client.components;

import client.rendering.Anchor;
import org.joml.Vector2f;

import framework.engine.Component;

public class TransformComponent implements Component {
    public Vector2f position;
    public Vector2f scale;
    public Anchor anchor;
    public float rotation = 0.0f;

    public TransformComponent(Vector2f position, Vector2f scale, Anchor anchor) {
        this.position = position;
        this.scale = scale;
        this.anchor = anchor;
    }

    public TransformComponent(Vector2f position, Vector2f scale) {
        this(position, scale, Anchor.CENTER);
    }

    public TransformComponent(Vector2f position) {
        this(position, new Vector2f(1.0f, 1.0f));
    }

}
