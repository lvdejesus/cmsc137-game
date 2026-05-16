package client.systems.client;

import client.components.NetworkIdComponent;
import client.components.player.PlayerNetworkComponent;
import client.components.player.PlayerStateComponent;
import client.network.NetworkSpawnManager;
import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.EntitySystem;
import client.components.HealthComponent;
import client.components.bullet.BulletComponent;
import client.components.enemy.EnemyComponent;
import client.components.TransformComponent;
import client.components.CollisionComponent;
import client.util.SpatialHashGrid;
import org.joml.Vector3f;
import org.joml.primitives.AABBf;

import java.util.HashSet;
import java.util.Set;

public class DamageSystem extends EntitySystem<Context> {
    private ComponentMapper<PlayerStateComponent> playerM;
    private ComponentMapper<HealthComponent> healthM;
    private ComponentMapper<TransformComponent> transformM;
    private ComponentMapper<CollisionComponent> collisionM;
    private ComponentMapper<BulletComponent> bulletM;
    private ComponentMapper<NetworkIdComponent> nim;

    private final NetworkSpawnManager nsm;
    private final SpatialHashGrid spatialHash = new SpatialHashGrid(64);
    private final Set<Integer> potentialTargets = new HashSet<>();

    public DamageSystem(NetworkSpawnManager nsm) {
        this.nsm = nsm;
    }

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);
        this.playerM = engine.getMapper(PlayerStateComponent.class);
        this.healthM = engine.getMapper(HealthComponent.class);
        this.transformM = engine.getMapper(TransformComponent.class);
        this.collisionM = engine.getMapper(CollisionComponent.class);
        this.bulletM = engine.getMapper(BulletComponent.class);
        this.nim = engine.getMapper(NetworkIdComponent.class);
    }

    @Override
    public void update(Context ctx) {
        spatialHash.clear();

        Iterable<Integer> damageableIterator = engine.getFamily(HealthComponent.class, CollisionComponent.class, NetworkIdComponent.class)::iterator;
        for (int entityId : damageableIterator) {
            if (!healthM.get(entityId).isAlive()) continue;
            spatialHash.addEntity(entityId, getWorldBox(entityId));
        }

        Iterable<Integer> bulletIterator = engine.getFamily(BulletComponent.class, NetworkIdComponent.class)::iterator;
        for (int bulletId : bulletIterator) {
            if (bulletM.get(bulletId).isEnemy) continue;
            AABBf bulletBox = getWorldBox(bulletId);
            NetworkIdComponent bulletnic = nim.get(bulletId);

            potentialTargets.clear();
            spatialHash.getPotentialColliders(bulletBox, potentialTargets);

            for (int targetId : potentialTargets) {
                if (playerM.get(targetId) != null) continue;

                HealthComponent targetHealth = healthM.get(targetId);
                if (!targetHealth.isAlive()) continue;
                if (!getWorldBox(targetId).intersectsAABB(bulletBox)) continue;

                targetHealth.damage(25.0f);
                nsm.despawn(bulletId, bulletnic.networkId);

                if (!targetHealth.isAlive()) {
                    NetworkIdComponent enemynic = nim.get(targetId);
                    spatialHash.removeEntity(targetId, getWorldBox(targetId));

                    nsm.despawn(targetId, enemynic.networkId);
                }

                break;
            }
        }

        bulletIterator = engine.getFamily(BulletComponent.class)::iterator;
        for (int bulletId : bulletIterator) {
            if (!bulletM.get(bulletId).isEnemy) continue;

            AABBf bulletBox = getWorldBox(bulletId);

            potentialTargets.clear();
            spatialHash.getPotentialColliders(bulletBox, potentialTargets);

            for (int targetId : potentialTargets) {
                if (playerM.get(targetId) == null) continue;

                HealthComponent playerHealth = healthM.get(targetId);
                if (!playerHealth.isAlive()) continue;

                AABBf playerBox = getWorldBox(targetId);
                if (!playerBox.intersectsAABB(bulletBox)) continue;

                playerHealth.damage(1.0f);
                NetworkIdComponent bulletnic = engine.getMapper(NetworkIdComponent.class).get(bulletId);
                nsm.despawn(bulletId, bulletnic.networkId);
                break;
            }
        }
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
}