package components;

import org.joml.Vector2f;

import framework.engine.Component;

public class TransformComponent implements Component {
    public Vector2f position;
    public Vector2f scale;

    public TransformComponent(Vector2f position, Vector2f scale) {
        this.position = position;
        this.scale = scale;
    }

    public TransformComponent(Vector2f position) {
        this(position, new Vector2f(1.0f, 1.0f));
    }
}
