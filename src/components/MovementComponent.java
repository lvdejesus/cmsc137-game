package components;

import org.joml.Vector2f;

import framework.engine.Component;

public class MovementComponent implements Component {
    public Vector2f velocity;

    public MovementComponent(Vector2f velocity) {
        this.velocity = velocity;
    }
}
