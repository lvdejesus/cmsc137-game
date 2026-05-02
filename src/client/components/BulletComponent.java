package client.components;

import framework.engine.Component;
import org.joml.Vector2f;

public class BulletComponent implements Component {
    public Vector2f velocity;

    public BulletComponent(Vector2f velocity) {
        this.velocity = velocity;
    }
}
