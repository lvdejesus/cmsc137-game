package client.components.bullet;

import framework.engine.Component;

public class BulletComponent implements Component {
    public float lifetime = 2.0f;
    public float damage = 25.0f;
    public float age = 0.0f;
    public int origin;
    public int splatter;

    public boolean isEnemy() {
        return origin == -1;
    }
}