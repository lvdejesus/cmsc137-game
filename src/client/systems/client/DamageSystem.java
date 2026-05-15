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
import org.joml.Vector3f;
import org.joml.primitives.AABBf;

public class DamageSystem extends EntitySystem<Context> {
    private ComponentMapper<PlayerStateComponent> playerM;
    private ComponentMapper<HealthComponent> healthM;
    private ComponentMapper<TransformComponent> transformM;
    private ComponentMapper<CollisionComponent> collisionM;
    private ComponentMapper<BulletComponent> bulletM;
    private ComponentMapper<NetworkIdComponent> nim;

    private final NetworkSpawnManager nsm;

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
        Iterable<Integer> bulletIterator = engine.getFamily(BulletComponent.class, NetworkIdComponent.class)::iterator;
        for (int bulletId : bulletIterator) {
            if (bulletM.get(bulletId).isEnemy) continue;
            AABBf bulletBox = getWorldBox(bulletId);
            NetworkIdComponent bulletnic = nim.get(bulletId);

            Iterable<Integer> targetIterator = engine.getFamily(HealthComponent.class, CollisionComponent.class, NetworkIdComponent.class)::iterator;
            for (int targetId : targetIterator) {
                if (playerM.get(targetId) != null) continue;

                HealthComponent targetHealth = healthM.get(targetId);

                if (!targetHealth.isAlive()) continue;
                if (!getWorldBox(targetId).intersectsAABB(bulletBox)) continue;

                System.out.println("damaged something");
                targetHealth.damage(25.0f);

                nsm.despawn(bulletId, bulletnic.networkId);

                if (!targetHealth.isAlive()) {
                    NetworkIdComponent enemynic = nim.get(targetId);
                    nsm.despawn(targetId, enemynic.networkId);
                }

                break;
            }
        }

        // Enemy bullets hitting player
        bulletIterator = engine.getFamily(BulletComponent.class)::iterator;
        for (int bulletId : bulletIterator) {
            if (!bulletM.get(bulletId).isEnemy) continue;

            AABBf bulletBox = getWorldBox(bulletId);
            Iterable<Integer> playerIterator = engine.getFamily(PlayerNetworkComponent.class, HealthComponent.class)::iterator;
            for (int playerId : playerIterator) {
                HealthComponent playerHealth = healthM.get(playerId);
                if (!playerHealth.isAlive()) continue;

                AABBf playerBox = getWorldBox(playerId);
                if (playerBox.intersectsAABB(bulletBox)) {
                    playerHealth.damage(1.0f);
                    NetworkIdComponent bulletnic = engine.getMapper(NetworkIdComponent.class).get(bulletId);
                    nsm.despawn(bulletId, bulletnic.networkId);
                }
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