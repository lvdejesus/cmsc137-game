package client.systems.client;

import client.components.*;
import client.components.enemy.EnemyComponent;
import client.components.player.PlayerStateComponent;
import client.entities.Bullet;
import client.entities.Enemy;
import client.entities.Key;
import client.network.NetworkSpawnManager;
import client.network.messages.server.S_GameResult;
import client.util.Statistics;
import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.EntitySystem;
import client.components.bullet.BulletComponent;
import client.util.SpatialHashGrid;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.primitives.AABBf;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DamageSystem extends EntitySystem<Context> {
    private ComponentMapper<PlayerStateComponent> playerM;
    private ComponentMapper<HealthComponent> healthM;
    private ComponentMapper<TransformComponent> transformM;
    private ComponentMapper<CollisionComponent> collisionM;
    private ComponentMapper<BulletComponent> bulletM;
    private ComponentMapper<NetworkIdComponent> nim;
    private ComponentMapper<PlayerKeysComponent> playerKeysM;

    private final NetworkSpawnManager nsm;
    private final SpatialHashGrid spatialHash = new SpatialHashGrid(64);
    private final Set<Integer> potentialTargets = new HashSet<>();

    private Map<Integer, Integer> playerEntityMap;
    private Statistics statistics;

    public DamageSystem(NetworkSpawnManager nsm, Map<Integer, Integer> playerEntityMap, Statistics statistics) {
        this.nsm = nsm;
        this.playerEntityMap = playerEntityMap;
        this.statistics = statistics;
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
        this.playerKeysM = engine.getMapper(PlayerKeysComponent.class);
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
            BulletComponent bc = bulletM.get(bulletId);
            AABBf bulletBox = getWorldBox(bulletId);
            potentialTargets.clear();
            spatialHash.getPotentialColliders(bulletBox, potentialTargets);

            for (int targetId : potentialTargets) {
                PlayerStateComponent playerC = playerM.get(targetId);
                if (bc.isEnemy() == (playerC == null)) continue;

                HealthComponent targetHealth = healthM.get(targetId);
                if (!targetHealth.isAlive()) continue;

                AABBf playerBox = getWorldBox(targetId);
                if (!playerBox.intersectsAABB(bulletBox)) continue;

                var bulletVelocity = engine.getMapper(MovementComponent.class).get(bulletId).velocity;
                var targetMc = engine.getMapper(MovementComponent.class).get(targetId);

                float multiplier;
                if (targetMc != null) {
                    var targetVelocity = targetMc.velocity;
                    multiplier = bulletVelocity.distance(targetVelocity) / 700.0f;
                } else {
                    multiplier = bulletVelocity.length() / 700.0f;
                }

                if (bc.splatter > 1) {
                    TransformComponent tc = engine.getMapper(TransformComponent.class).get(targetId);
                    CollisionComponent cc = engine.getMapper(CollisionComponent.class).get(targetId);
                    float angle = (float) Math.atan2(bulletVelocity.y, bulletVelocity.x);
                    float increment = (float) (Math.PI * 2.0f / bc.splatter);
                    for (int i = 0; i < bc.splatter; i++) {
                        float finalAngle = angle + increment * i;
                        var minDist = Math.sqrt(cc.boundingBox.minX * cc.boundingBox.minX + cc.boundingBox.minY * cc.boundingBox.minY) + 1.0f;
                        float ox = (float) (tc.position.x + minDist * Math.cos(finalAngle));
                        float oy = (float) (tc.position.y + minDist * Math.sin(finalAngle));
                        float nx = (float) (tc.position.x + 300.0f * Math.cos(finalAngle));
                        float ny = (float) (tc.position.y + 300.0f * Math.sin(finalAngle));
                        nsm.spawn(Bullet.class, Bullet.serialize(ox, oy, nx, ny, 0.0f, 0.0f, bulletVelocity.length(), bc.origin, 1, 0.3f));
                    }
                }

                if (playerC == null) {
                    targetHealth.damage(bc.damage * multiplier);
                    if (!targetHealth.isAlive()) {
                        statistics.addKill(bc.origin);

                        BossComponent bossC = engine.getMapper(BossComponent.class).get(targetId);
                        if (bossC != null) {
                            statistics.addBossKill();
                            nsm.broadcastMessage(new S_GameResult(statistics, true));
                        }

                        var originEntityId = playerEntityMap.get(bc.origin);
                        if (originEntityId != null) {
                            var xpc = engine.getMapper(ExperienceComponent.class).get(originEntityId);
                            var ec = engine.getMapper(EnemyComponent.class).get(targetId);
                            if (ec != null)  {
                                if (xpc != null) {
                                    if (ec.type == Enemy.EnemyType.Regular) {
                                        xpc.exp += 5;
                                    } else if (ec.type == Enemy.EnemyType.Advanced) {
                                        xpc.exp += 8;
                                    }
                                }
                            }
                        }

                        spatialHash.removeEntity(targetId, getWorldBox(targetId));
                        nsm.despawn(targetId);
                    }
                } else {
                    targetHealth.damage(bc.damage);
                    if (!targetHealth.isAlive()) {
                        PlayerKeysComponent pkc = playerKeysM.get(targetId);
                        if (pkc != null && pkc.keyCount > 0) {
                            TransformComponent ptc = transformM.get(targetId);
                            for (int i = 0; i < pkc.keyCount; i++) {
                                nsm.spawn(Key.class, Key.serialize(ptc.position.x, ptc.position.y));
                            }
                            pkc.keyCount = 0;
                        }
                    }
                }

                nsm.despawn(bulletId);
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