package client.systems.client;

import client.components.NetworkIdComponent;
import client.components.player.PlayerNetworkComponent;
import client.network.NetworkSpawnManager;
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

import java.util.logging.Level;
import java.util.logging.Logger;

public class DamageSystem extends EntitySystem<Context> {
    private ComponentMapper<HealthComponent> healthM;
    private ComponentMapper<TransformComponent> transformM;
    private ComponentMapper<CollisionComponent> collisionM;
    private ComponentMapper<BulletComponent> bulletM;

    private NetworkSpawnManager nsm;

    public DamageSystem(NetworkSpawnManager nsm) {
        this.nsm = nsm;
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
    public void update(Context ctx) {
        long[] bitsets = getBitsets();
        int bulletIndex = getComponentIndex(BulletComponent.class);
        int enemyIndex = getComponentIndex(EnemyComponent.class);
        int networkIndex = getComponentIndex(NetworkIdComponent.class);
        int playerIndex = getComponentIndex(PlayerNetworkComponent.class);

        long bulletMask = 1L << bulletIndex;
        long enemyMask = 1L << enemyIndex;
        long networkMask = 1L << networkIndex;
        long playerMask = 1L << playerIndex;

        int entityMax = getEntityMax();
        
        // Player bullets hitting enemies
        for (int bulletId = 0; bulletId < entityMax; bulletId++) {
            if (!hasComponents(bitsets[bulletId], bulletMask | networkMask) || bulletM.get(bulletId).isEnemy) {
                continue;
            }

            AABBf bulletBox = getWorldBox(bulletId);

            for (int enemyId = 0; enemyId < entityMax; enemyId++) {
                if (!hasComponents(bitsets[enemyId], enemyMask | networkMask)) {
                    continue;
                }

                HealthComponent enemyHealth = healthM.get(enemyId);
                if (!enemyHealth.isAlive()) {
                    continue;
                }

                if (getWorldBox(enemyId).intersectsAABB(bulletBox)) {
                    enemyHealth.damage(25.0f);

                    NetworkIdComponent bulletnic = engine.getMapper(NetworkIdComponent.class).get(bulletId);
                    nsm.despawn(bulletId, bulletnic.networkId);

                    if (!enemyHealth.isAlive()) {
                        NetworkIdComponent enemynic = engine.getMapper(NetworkIdComponent.class).get(enemyId);
                        nsm.despawn(enemyId, enemynic.networkId);
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
                if (!hasComponents(bitsets[playerId], playerMask)) continue;

                HealthComponent playerHealth = healthM.get(playerId);
                if (playerHealth == null || !playerHealth.isAlive()) {
                    continue;
                }

                AABBf playerBox = getWorldBox(playerId);
                if (playerBox.intersectsAABB(bulletBox)) {
                    playerHealth.damage(10.0f);
                    NetworkIdComponent bulletnic = engine.getMapper(NetworkIdComponent.class).get(bulletId);
                    nsm.despawn(bulletId, bulletnic.networkId);
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