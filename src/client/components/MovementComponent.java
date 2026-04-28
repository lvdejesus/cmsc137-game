package client.components;

import org.joml.Vector2f;

import framework.engine.Component;

public class MovementComponent implements Component {
    public Vector2f velocity;
    public float acceleration;
    public float friction;
    public float speed;
    

    public MovementComponent(float speed, float acceleration, float friction, Vector2f velocity) {
        this.speed = speed;
        this.velocity = velocity;
        this.acceleration = acceleration;
        this.friction = friction;
    }

    public MovementComponent(float speed) {
        this(speed, 0.0f, 0.0f, new Vector2f(0.0f, 0.0f));
    }
}
