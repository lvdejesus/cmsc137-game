package client.systems.client;

import client.components.HealthComponent;
import client.components.MovementComponent;
import client.components.TransformComponent;
import client.components.enemy.EnemyComponent;
import client.components.player.PlayerNetworkComponent;
import client.entities.Bullet;
import client.entities.Enemy;
import client.network.NetworkSpawnManager;
import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.IteratingEntitySystem;

import java.util.Iterator;
import java.util.Random;

public class EnemySystem extends IteratingEntitySystem<Context> {
    private ComponentMapper<TransformComponent> tm;
    private ComponentMapper<MovementComponent> mm;
    private ComponentMapper<HealthComponent> healthM;
    private ComponentMapper<MovementComponent> movementM;
    private final Random random = new Random();
    private float shootTimer = 0.0f;
    private final float shootInterval = 1.2f;
    private final float shootIntervalVariance = 0.4f;

    private NetworkSpawnManager nsm;

    public EnemySystem(NetworkSpawnManager nsm) {
        super(EnemyComponent.class);

        this.nsm = nsm;
    }

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);
        this.engine = engine;

        this.tm = engine.getMapper(TransformComponent.class);
        this.mm = engine.getMapper(MovementComponent.class);
        this.healthM = engine.getMapper(HealthComponent.class);
        this.movementM = engine.getMapper(MovementComponent.class);
    }

    @Override
    public void processEntity(int id, Context ctx) {
        TransformComponent transform = tm.get(id);
        MovementComponent movement = mm.get(id);

        transform.rotation += 45.0f * ctx.deltaTime;
        transform.rotation %= 360.0f;

        // random movement
        if (random.nextFloat() < 0.0001f) {
            movement.velocity.x = 0.0f;
            movement.velocity.y = 0.0f;
        } else if (random.nextFloat() < 0.002f) {
            if (movement.velocity.x == 0 && movement.velocity.y == 0) {
                float angle = random.nextFloat() * (float) Math.PI * 2;
                float speed = 30.0f + random.nextFloat() * 40.0f;
                movement.velocity.x = (float) Math.cos(angle) * speed;
                movement.velocity.y = (float) Math.sin(angle) * speed;
            }
        }
    }

    @Override
    public void update(Context ctx) {
        super.update(ctx);

        shootTimer -= ctx.deltaTime;
        if (shootTimer <= 0.0f) {
            shootAtPlayer();
            shootTimer = shootInterval + shootIntervalVariance * (random.nextFloat() * 0.5f);
        }
    }

    private void shootAtPlayer() {
        Iterable<Integer> playerIterator = engine.getFamily(PlayerNetworkComponent.class, HealthComponent.class)::iterator;
        for (int i : playerIterator) {
            HealthComponent playerHealth = healthM.get(i);
            if (!playerHealth.isAlive()) continue;

            TransformComponent playerTransform = tm.get(i);

            Iterable<Integer> enemyIterator = engine.getFamily(EnemyComponent.class, HealthComponent.class)::iterator;
            for (int enemyId : enemyIterator) {
                HealthComponent enemyHealth = healthM.get(enemyId);
                MovementComponent enemyMovement = movementM.get(enemyId);
                if (!enemyHealth.isAlive()) continue;

                TransformComponent enemyTransform = tm.get(enemyId);

                float dx = random.nextFloat() * 100.0f - 50.0f;
                float dy = random.nextFloat() * 100.0f - 50.0f;
                nsm.spawn(Bullet.class, Bullet.serialize(enemyTransform.position.x, enemyTransform.position.y, playerTransform.position.x + dx, playerTransform.position.y + dy, enemyMovement.velocity.x, enemyMovement.velocity.y, true));
            }

            break;
        }
    }
}