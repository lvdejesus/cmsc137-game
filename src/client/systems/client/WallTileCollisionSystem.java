package client.systems.client;

import client.components.CollisionComponent;
import client.components.MovementComponent;
import client.components.WallComponent;
import client.components.TransformComponent;
import client.components.enemy.EnemyComponent;
import client.components.player.PlayerStateComponent;
import client.util.SpatialHashGrid;
import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.IteratingEntitySystem;
import org.joml.Vector2f;
import org.joml.primitives.AABBf;

import java.util.Set;
import java.util.HashSet;

public class WallTileCollisionSystem extends IteratingEntitySystem<Context> {
    private ComponentMapper<TransformComponent> transformM;
    private ComponentMapper<CollisionComponent> collisionM;
    private ComponentMapper<MovementComponent> movementM;

    private ComponentMapper<PlayerStateComponent> stateM;
    private ComponentMapper<EnemyComponent> enemyM;
    private ComponentMapper<WallComponent> wallM;

    private final SpatialHashGrid staticGrid = new SpatialHashGrid(64);
    private final SpatialHashGrid dynamicGrid = new SpatialHashGrid(64);
    private final Set<Integer> staticAdded = new HashSet<>();
    private final Set<Integer> potentialColliders = new HashSet<>();

    private final AABBf scratchBox = new AABBf(0.0f, 0.0f, Float.NEGATIVE_INFINITY, 0.0f, 0.0f, Float.POSITIVE_INFINITY);
    private final AABBf wallBox = new AABBf(0.0f, 0.0f, Float.NEGATIVE_INFINITY, 0.0f, 0.0f, Float.POSITIVE_INFINITY);

    public WallTileCollisionSystem() {
        super(TransformComponent.class, MovementComponent.class);
    }

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);

        transformM = engine.getMapper(TransformComponent.class);
        collisionM = engine.getMapper(CollisionComponent.class);
        movementM = engine.getMapper(MovementComponent.class);

        stateM = engine.getMapper(PlayerStateComponent.class);
        enemyM = engine.getMapper(EnemyComponent.class);
        wallM = engine.getMapper(WallComponent.class);
    }

    @Override
    public void update(Context ctx) {
        dynamicGrid.clear();
        Iterable<Integer> walls = engine.getFamily(WallComponent.class, CollisionComponent.class, TransformComponent.class)::iterator;
        for (int wallId : walls) {
            WallComponent wc = wallM.get(wallId);
            if (!wc.isActive()) continue;
            updateWorldBox(wallId, wallBox);
            if (wc.isStatic) {
                if (staticAdded.add(wallId)) staticGrid.addEntity(wallId, wallBox);
            } else {
                dynamicGrid.addEntity(wallId, wallBox);
            }
        }

        super.update(ctx);
    }

    @Override
    protected void processEntity(int entityId, Context ctx) {
        boolean canSlide = stateM.get(entityId) != null || enemyM.get(entityId) != null;

        var pos = transformM.get(entityId).position;
        var vel = movementM.get(entityId).velocity;
        var cc = collisionM.get(entityId);

        AABBf localBox = null;
        if (cc != null) {
            localBox = cc.boundingBox;
        }

        float dt = ctx.deltaTime;

        pos.x += vel.x * dt;
        if (localBox != null && canSlide) {
            syncWorldBox(pos, localBox, scratchBox);
            potentialColliders.clear();
            staticGrid.getPotentialColliders(scratchBox, potentialColliders);
            dynamicGrid.getPotentialColliders(scratchBox, potentialColliders);

            for (int wallId : potentialColliders) {
                if (wallId == entityId) continue;
                updateWorldBox(wallId, wallBox);

                if (scratchBox.minY < wallBox.maxY && scratchBox.maxY > wallBox.minY) {
                    if (scratchBox.intersectsAABB(wallBox)) {
                        float overlapX = calculateOverlapX(scratchBox, wallBox);
                        pos.x += overlapX;
                        vel.x = 0;
                        syncWorldBox(pos, localBox, scratchBox);
                    }
                }
            }
        }

        pos.y += vel.y * dt;
        if (localBox != null && canSlide) {
            syncWorldBox(pos, localBox, scratchBox);
            potentialColliders.clear();
            staticGrid.getPotentialColliders(scratchBox, potentialColliders);
            dynamicGrid.getPotentialColliders(scratchBox, potentialColliders);

            for (int wallId : potentialColliders) {
                if (wallId == entityId) continue;
                updateWorldBox(wallId, wallBox);

                if (scratchBox.minX < wallBox.maxX && scratchBox.maxX > wallBox.minX) {
                    if (scratchBox.intersectsAABB(wallBox)) {
                        float overlapY = calculateOverlapY(scratchBox, wallBox);
                        pos.y += overlapY;
                        vel.y = 0;
                        syncWorldBox(pos, localBox, scratchBox);
                    }
                }
            }
        }
    }

    private void syncWorldBox(Vector2f pos, AABBf local, AABBf out) {
        out.minX = local.minX + pos.x;
        out.minY = local.minY + pos.y;
        out.maxX = local.maxX + pos.x;
        out.maxY = local.maxY + pos.y;
    }

    private void updateWorldBox(int id, AABBf out) {
        var p = transformM.get(id).position;
        var b = collisionM.get(id).boundingBox;
        syncWorldBox(p, b, out);
    }

    private float calculateOverlapX(AABBf player, AABBf wall) {
        float playerMid = (player.minX + player.maxX) / 2f;
        float wallMid = (wall.minX + wall.maxX) / 2f;
        float overlap = Math.min(player.maxX, wall.maxX) - Math.max(player.minX, wall.minX);
        return (playerMid < wallMid) ? -overlap : overlap;
    }

    private float calculateOverlapY(AABBf player, AABBf wall) {
        float playerMid = (player.minY + player.maxY) / 2f;
        float wallMid = (wall.minY + wall.maxY) / 2f;
        float overlap = Math.min(player.maxY, wall.maxY) - Math.max(player.minY, wall.minY);
        return (playerMid < wallMid) ? -overlap : overlap;
    }
}
