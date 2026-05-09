package client.systems;

import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.EntitySystem;
import client.components.HealthComponent;
import client.components.bullet.BulletComponent;
import client.components.enemy.EnemyComponent;
import client.components.TransformComponent;
import client.components.CollisionComponent;
import org.joml.Vector3f;
import org.joml.primitives.AABBf;
import client.systems.Context;

public class DamageSystem extends EntitySystem<Context> {
    private ComponentMapper<HealthComponent> healthM;
    private ComponentMapper<TransformComponent> transformM;
    private ComponentMapper<CollisionComponent> collisionM;

    public DamageSystem() {
        super(HealthComponent.class);
    }

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);
        this.healthM = engine.getMapper(HealthComponent.class);
        this.transformM = engine.getMapper(TransformComponent.class);
        this.collisionM = engine.getMapper(CollisionComponent.class);
    }

    @Override
    public void processEntity(int id, Context ctx) {
        // Doesn't iterate so use update
    }

    @Override
    public void update(Context ctx) {
        super.update(ctx);
        
        long[] bitsets = getBitsets();
        int bulletIndex = getComponentIndex(BulletComponent.class);
        int enemyIndex = getComponentIndex(EnemyComponent.class);

        long bulletMask = 1L << bulletIndex;
        long enemyMask = 1L << enemyIndex;

        int entityMax = getEntityMax();
        
        for (int bulletId = 0; bulletId < entityMax; bulletId++) {
            if ((bitsets[bulletId] & bulletMask) != bulletMask) {
                continue;
            }

            TransformComponent bulletTransform = transformM.get(bulletId);
            CollisionComponent bulletCollision = collisionM.get(bulletId);

            for (int enemyId = 0; enemyId < entityMax; enemyId++) {
                if ((bitsets[enemyId] & enemyMask) != enemyMask) {
                    continue;
                }

                HealthComponent enemyHealth = healthM.get(enemyId);
                TransformComponent enemyTransform = transformM.get(enemyId);
                CollisionComponent enemyCollision = collisionM.get(enemyId);

                if (!enemyHealth.isAlive()) {
                    continue;
                }

                AABBf bulletWorldBox = new AABBf(
                    new Vector3f(
                        bulletCollision.boundingBox.minX() + bulletTransform.position.x,
                        bulletCollision.boundingBox.minY() + bulletTransform.position.y,
                        bulletCollision.boundingBox.minZ()
                    ),
                    new Vector3f(
                        bulletCollision.boundingBox.maxX() + bulletTransform.position.x,
                        bulletCollision.boundingBox.maxY() + bulletTransform.position.y,
                        bulletCollision.boundingBox.maxZ()
                    )
                );

                AABBf enemyWorldBox = new AABBf(
                    new Vector3f(
                        enemyCollision.boundingBox.minX() + enemyTransform.position.x,
                        enemyCollision.boundingBox.minY() + enemyTransform.position.y,
                        enemyCollision.boundingBox.minZ()
                    ),
                    new Vector3f(
                        enemyCollision.boundingBox.maxX() + enemyTransform.position.x,
                        enemyCollision.boundingBox.maxY() + enemyTransform.position.y,
                        enemyCollision.boundingBox.maxZ()
                    )
                );

                boolean intersects =
                    bulletWorldBox.minX() <= enemyWorldBox.maxX() && bulletWorldBox.maxX() >= enemyWorldBox.minX() &&
                    bulletWorldBox.minY() <= enemyWorldBox.maxY() && bulletWorldBox.maxY() >= enemyWorldBox.minY() &&
                    bulletWorldBox.minZ() <= enemyWorldBox.maxZ() && bulletWorldBox.maxZ() >= enemyWorldBox.minZ();

                if (!intersects) {
                    continue;
                }

                enemyHealth.damage(25.0f);
                engine.destroyEntity(bulletId);

                if (!enemyHealth.isAlive()) {
                    engine.destroyEntity(enemyId);
                }
            }
        }
    }
}