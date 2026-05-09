package client.systems;

import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.EntitySystem;
import client.components.enemy.EnemyComponent;
import client.components.TransformComponent;
import client.components.MovementComponent;
import client.components.player.PlayerTagComponent;
import client.components.HealthComponent;
import client.entities.Enemy;
import client.entities.Bullet;

import java.util.Random;

public class EnemySystem extends EntitySystem<Context> {
    private ComponentMapper<TransformComponent> tm;
    private ComponentMapper<MovementComponent> mm;
    private ComponentMapper<HealthComponent> healthM;
    private Engine<Context> engine;
    private Random random = new Random();
    private float spawnTimer = 0.0f;
    private float spawnInterval = 3.0f;
    private float shootTimer = 0.0f;
    private float shootInterval = 1.0f;

    public EnemySystem() {
        super(EnemyComponent.class);
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


        spawnTimer -= ctx.deltaTime;
        if (spawnTimer <= 0.0f) {
            spawnEnemy();
            spawnTimer = spawnInterval;
        }

        shootTimer -= ctx.deltaTime;
        if (shootTimer <= 0.0f) {
            shootAtPlayer();
            shootTimer = shootInterval;
        }
    }

    private void spawnEnemy() {
        new Enemy(engine);
    }

    private void shootAtPlayer() {
        long[] bitsets = getBitsets();
        int playerIndex = getComponentIndex(PlayerTagComponent.class);
        long playerMask = 1L << playerIndex;
        int healthIndex = getComponentIndex(HealthComponent.class);
        long healthMask = 1L << healthIndex;
        int entityMax = getEntityMax();

        for (int i = 0; i < entityMax; i++) {
            if ((bitsets[i] & playerMask) == playerMask && (bitsets[i] & healthMask) == healthMask) {
                HealthComponent playerHealth = healthM.get(i);
                if (!playerHealth.isAlive()) {
                    continue;
                }

                TransformComponent playerTransform = tm.get(i);

                for (int enemyId = 0; enemyId < entityMax; enemyId++) {
                    if ((bitsets[enemyId] & getFamilyMask()) == getFamilyMask()) {
                        HealthComponent enemyHealth = healthM.get(enemyId);
                        if (!enemyHealth.isAlive()) {
                            continue;
                        }

                        TransformComponent enemyTransform = tm.get(enemyId);
                        float dx = playerTransform.position.x - enemyTransform.position.x;
                        float dy = playerTransform.position.y - enemyTransform.position.y;
                        float angle = (float) Math.toDegrees(Math.atan2(dy, dx));

                        new Bullet(engine, enemyTransform.position.x, enemyTransform.position.y, angle, true);
                    }
                }
                break;
            }
        }
    }
}