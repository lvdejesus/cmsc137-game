package client.components;

import org.joml.Vector2f;
import framework.engine.Component;


public class UpgradeComponent implements Component {
    public Vector2f currentPosition = new Vector2f();
    public Vector2f targetPosition = new Vector2f();
    public float targetRotation = 0f;
    public boolean faceUp = false;

    // Animation
    public float moveSpeed = 10f;
    public float rotationSpeed = 10f;

    public UpgradeComponent(Vector2f targetPos){
        this.targetPosition.set(targetPos);
    }
}
