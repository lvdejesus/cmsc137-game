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

import java.util.Random;

public class EnemySystem extends IteratingEntitySystem<Context> {
    private ComponentMapper<TransformComponent> tm;
    private ComponentMapper<MovementComponent> mm;
    private ComponentMapper<HealthComponent> healthM;
    private final Random random = new Random();
    private float shootTimer = 0.0f;
    private final float shootInterval = 1.0f;

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
    }

    @Override
    public void processEntity(int id, Context ctx) {
        TransformComponent transform = tm.get(id);
        MovementComponent movement = mm.get(id);

        transform.rotation += 45.0f * ctx.deltaTime;
        transform.rotation %= 360.0f;

        // random movement
        if (random.nextFloat() < 0.02f) {
            float angle = random.nextFloat() * (float) Math.PI * 2;
            float speed = 30.0f + random.nextFloat() * 40.0f;
            movement.velocity.x = (float) Math.cos(angle) * speed;
            movement.velocity.y = (float) Math.sin(angle) * speed;
        }
    }

    @Override
    public void update(Context ctx) {
        super.update(ctx);

        shootTimer -= ctx.deltaTime;
        if (shootTimer <= 0.0f) {
            shootAtPlayer();
            shootTimer = shootInterval;
        }
    }

    private void shootAtPlayer() {
        long[] bitsets = getBitsets();
        int playerIndex = getComponentIndex(PlayerNetworkComponent.class);
        long playerMask = 1L << playerIndex;
        int entityMax = getEntityMax();

        for (int i = 0; i < entityMax; i++) {
            if ((bitsets[i] & playerMask) != playerMask) {
                continue;
            }

            HealthComponent playerHealth = healthM.get(i);
            if (!playerHealth.isAlive()) {
                continue;
            }

            TransformComponent playerTransform = tm.get(i);

            for (int enemyId = 0; enemyId < entityMax; enemyId++) {
                if ((bitsets[enemyId] & getFamilyMask()) != getFamilyMask()) {
                    continue;
                }

                HealthComponent enemyHealth = healthM.get(enemyId);
                if (!enemyHealth.isAlive()) {
                    continue;
                }

                TransformComponent enemyTransform = tm.get(enemyId);
                float dx = playerTransform.position.x - enemyTransform.position.x;
                float dy = playerTransform.position.y - enemyTransform.position.y;
                float angle = (float) Math.toDegrees(Math.atan2(dy, dx));

                nsm.spawn(Bullet.class, Bullet.serialize(enemyTransform.position.x, enemyTransform.position.y, angle, true));
            }
            break;
        }
    }
}