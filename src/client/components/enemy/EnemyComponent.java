package client.components.enemy;

import client.entities.Enemy;
import framework.engine.Component;

public class EnemyComponent implements Component {
    public float shootTimer = 0.0f;
    public float shootInterval;
    public float shootVariance;
    public Enemy.EnemyType type;
    public float bulletSpeed = 300.0f;

    public EnemyComponent(Enemy.EnemyType type, float shootInterval, float shootVariance) {
        this.type = type;
        this.shootInterval = shootInterval;
        this.shootVariance = shootVariance;
    }
}