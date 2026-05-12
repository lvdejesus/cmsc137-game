package client.systems.client;

import client.network.NetworkSpawnManager;
import framework.engine.EntitySystem;
import client.entities.Enemy;

import java.util.Random;

public class EnemySpawnSystem extends EntitySystem<Context> {
    private float spawnTimer = 3.0f;
    private final float spawnInterval = 3.0f;
    private final Random rand = new Random();

    private final NetworkSpawnManager nsm;

    public EnemySpawnSystem(NetworkSpawnManager nsm) {
        this.nsm = nsm;
    }

    @Override
    public void update(Context ctx) {
        spawnTimer -= ctx.deltaTime;
        if (spawnTimer <= 0.0f) {

            float x = 50.0f + rand.nextFloat() * 700.0f;
            float y = 50.0f + rand.nextFloat() * 500.0f;

            nsm.spawn(Enemy.class, Enemy.serialize(x, y));
            spawnTimer = spawnInterval;
        }
    }
}