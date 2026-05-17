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
import org.joml.Vector2f;

import java.util.Iterator;
import java.util.Random;

public class EnemySystem extends IteratingEntitySystem<Context> {
    private ComponentMapper<TransformComponent> tm;
    private ComponentMapper<MovementComponent> mm;
    private ComponentMapper<HealthComponent> healthM;
    private ComponentMapper<MovementComponent> movementM;
    private final Random random = new Random();

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

        EnemyComponent ec = engine.getMapper(EnemyComponent.class).get(id);

        ec.shootTimer -= ctx.deltaTime;

        if (ec.type == Enemy.EnemyType.Regular || ec.type == Enemy.EnemyType.Advanced) {
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

            if (ec.shootTimer <= 0.0f) {
                shootAtPlayer(id);
                ec.shootTimer = ec.shootInterval + ec.shootVariance * (random.nextFloat() * 0.5f);
            }
        }
    }

    public void shootAtPlayer(int enemyId) {
        HealthComponent enemyHealth = healthM.get(enemyId);
        if (!enemyHealth.isAlive()) return;

        EnemyComponent ec = engine.getMapper(EnemyComponent.class).get(enemyId);
        TransformComponent enemyTransform = tm.get(enemyId);
        float minDistance = Float.POSITIVE_INFINITY;
        Vector2f minPosition = null;

        Iterable<Integer> playerIterator = engine.getFamily(PlayerNetworkComponent.class, HealthComponent.class)::iterator;
        for (int i : playerIterator) {
            HealthComponent playerHealth = healthM.get(i);
            if (!playerHealth.isAlive()) continue;

            TransformComponent playerTransform = tm.get(i);
            float distance = new Vector2f(playerTransform.position).distance(enemyTransform.position);
            if (distance < minDistance) {
                minDistance = distance;
                minPosition = playerTransform.position;
            }
        }

        if (minPosition == null) return;

        MovementComponent enemyMovement = movementM.get(enemyId);

        float variance = minDistance * 0.3f;

        float dx = random.nextFloat() * variance * 2 - variance;
        float dy = random.nextFloat() * variance * 2 - variance;
        nsm.spawn(Bullet.class, Bullet.serialize(enemyTransform.position.x, enemyTransform.position.y, minPosition.x + dx, minPosition.y + dy, enemyMovement.velocity.x, enemyMovement.velocity.y, ec.bulletSpeed, -1));
    }
}