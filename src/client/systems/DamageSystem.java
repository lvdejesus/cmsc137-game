package client.systems;

import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.EntitySystem;
import client.components.HealthComponent;
import client.components.bullet.BulletComponent;
import client.components.enemy.EnemyComponent;
import client.components.TransformComponent;
import client.components.CollisionComponent;
import client.components.player.PlayerTagComponent;
import org.joml.Vector3f;
import org.joml.primitives.AABBf;
import client.systems.Context;

public class DamageSystem extends EntitySystem<Context> {
    private ComponentMapper<HealthComponent> healthM;
    private ComponentMapper<TransformComponent> transformM;
    private ComponentMapper<CollisionComponent> collisionM;
    private ComponentMapper<BulletComponent> bulletM;

    public DamageSystem() {
        super(HealthComponent.class);
    }

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);
        this.healthM = engine.getMapper(HealthComponent.class);
        this.transformM = engine.getMapper(TransformComponent.class);
        this.collisionM = engine.getMapper(CollisionComponent.class);
        this.bulletM = engine.getMapper(BulletComponent.class);
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
        int playerIndex = getComponentIndex(PlayerTagComponent.class);

        long bulletMask = 1L << bulletIndex;
        long enemyMask = 1L << enemyIndex;
        long playerMask = 1L << playerIndex;

        int entityMax = getEntityMax();
        
        // Player bullets hitting enemies
        for (int bulletId = 0; bulletId < entityMax; bulletId++) {
            if (!hasComponents(bitsets[bulletId], bulletMask) || bulletM.get(bulletId).isEnemy) {
                continue;
            }

            AABBf bulletBox = getWorldBox(bulletId);

            for (int enemyId = 0; enemyId < entityMax; enemyId++) {
                if (!hasComponents(bitsets[enemyId], enemyMask)) {
                    continue;
                }

                HealthComponent enemyHealth = healthM.get(enemyId);
                if (!enemyHealth.isAlive()) {
                    continue;
                }

                if (checkIntersection(bulletBox, getWorldBox(enemyId))) {
                    enemyHealth.damage(25.0f);
                    engine.destroyEntity(bulletId);

                    if (!enemyHealth.isAlive()) {
                        engine.destroyEntity(enemyId);
                    }
                }
            }
        }

        // Enemy bullets hitting player
        for (int bulletId = 0; bulletId < entityMax; bulletId++) {
            if (!hasComponents(bitsets[bulletId], bulletMask) || !bulletM.get(bulletId).isEnemy) {
                continue;
            }

            AABBf bulletBox = getWorldBox(bulletId);

            for (int playerId = 0; playerId < entityMax; playerId++) {
                if (!hasComponents(bitsets[playerId], playerMask)) {
                    continue;
                }

                HealthComponent playerHealth = healthM.get(playerId);
                if (!playerHealth.isAlive()) {
                    continue;
                }

                if (checkIntersection(bulletBox, getWorldBox(playerId))) {
                    playerHealth.damage(10.0f);
                    engine.destroyEntity(bulletId);
                }
            }
        }
    }

    private boolean hasComponents(long bitset, long mask) {
        return (bitset & mask) == mask;
    }

    private AABBf getWorldBox(int entityId) {
        TransformComponent transform = transformM.get(entityId);
        CollisionComponent collision = collisionM.get(entityId);
        return new AABBf(
            new Vector3f(
                collision.boundingBox.minX() + transform.position.x,
                collision.boundingBox.minY() + transform.position.y,
                collision.boundingBox.minZ()
            ),
            new Vector3f(
                collision.boundingBox.maxX() + transform.position.x,
                collision.boundingBox.maxY() + transform.position.y,
                collision.boundingBox.maxZ()
            )
        );
    }

    private boolean checkIntersection(AABBf a, AABBf b) {
        return a.minX() <= b.maxX() && a.maxX() >= b.minX() &&
               a.minY() <= b.maxY() && a.maxY() >= b.minY() &&
               a.minZ() <= b.maxZ() && a.maxZ() >= b.minZ();
    }
}