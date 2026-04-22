package game.components;

import org.joml.Vector2f;

import framework.engine.Component;

public class MovementComponent implements Component {
    public Vector2f velocity;
    public float speed;

    public MovementComponent(float speed, Vector2f velocity) {
        this.speed = speed;
        this.velocity = velocity;
    }

    public MovementComponent(float speed) {
        this(speed, new Vector2f(0.0f, 0.0f));
    }
}
