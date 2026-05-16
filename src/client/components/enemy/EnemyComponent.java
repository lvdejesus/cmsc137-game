package client.components.enemy;

import framework.engine.Component;

public class EnemyComponent implements Component {
    public float shootTimer = 0.0f;
    public float shootInterval = 1.2f;
    public float shootVariance = 0.8f;

    public EnemyComponent(float shootInterval, float shootVariance) {
        this.shootInterval = shootInterval;
        this.shootVariance = shootVariance;
    }
}