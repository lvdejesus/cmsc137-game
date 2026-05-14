package client.systems.client;

import client.components.CollisionComponent;
import client.components.MovementComponent;
import client.components.WallComponent;
import client.components.TransformComponent;
import client.components.player.PlayerTagComponent;
import client.util.SpatialHashGrid;
import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.IteratingEntitySystem;
import org.joml.Vector2f;
import org.joml.primitives.AABBf;

import java.util.Set;
import java.util.HashSet;

public class PlayerWallTileCollisionSystem extends IteratingEntitySystem<Context> {
    private ComponentMapper<TransformComponent> transformM;
    private ComponentMapper<CollisionComponent> collisionM;
    private ComponentMapper<MovementComponent> movementM;

    private final SpatialHashGrid wallGrid = new SpatialHashGrid(64);
    private final Set<Integer> potentialColliders = new HashSet<>();

    private final AABBf scratchBox = new AABBf(0.0f, 0.0f, Float.NEGATIVE_INFINITY, 0.0f, 0.0f, Float.POSITIVE_INFINITY);
    private final AABBf wallBox = new AABBf(0.0f, 0.0f, Float.NEGATIVE_INFINITY, 0.0f, 0.0f, Float.POSITIVE_INFINITY);

    public PlayerWallTileCollisionSystem() {
        super(TransformComponent.class, CollisionComponent.class, MovementComponent.class, PlayerTagComponent.class);
    }

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);

        transformM = engine.getMapper(TransformComponent.class);
        collisionM = engine.getMapper(CollisionComponent.class);
        movementM = engine.getMapper(MovementComponent.class);
    }

    @Override
    public void update(Context ctx) {
        wallGrid.clear();
        Iterable<Integer> walls = engine.getFamily(WallComponent.class, CollisionComponent.class, TransformComponent.class)::iterator;
        for (int wallId : walls) {
            updateWorldBox(wallId, wallBox);
            wallGrid.addEntity(wallId, wallBox);
        }

        super.update(ctx);
    }

    @Override
    protected void processEntity(int entityId, Context ctx) {
        var pos = transformM.get(entityId).position;
        var vel = movementM.get(entityId).velocity;
        var localBox = collisionM.get(entityId).boundingBox;
        float dt = ctx.deltaTime;

        pos.x += vel.x * dt;
        syncWorldBox(pos, localBox, scratchBox);
        wallGrid.getPotentialColliders(scratchBox, potentialColliders);

        for (int wallId : potentialColliders) {
            if (wallId == entityId) continue;

            updateWorldBox(wallId, wallBox);

            if (scratchBox.intersectsAABB(wallBox)) {
                float overlapX = calculateOverlapX(scratchBox, wallBox);

                pos.x += (overlapX > 0) ? overlapX + 0.01f : overlapX - 0.01f;
                vel.x = 0;
                syncWorldBox(pos, localBox, scratchBox);
            }
        }

        pos.y += vel.y * dt;
        syncWorldBox(pos, localBox, scratchBox);
        wallGrid.getPotentialColliders(scratchBox, potentialColliders);

        for (int wallId : potentialColliders) {
            if (wallId == entityId) continue;
            updateWorldBox(wallId, wallBox);

            if (scratchBox.intersectsAABB(wallBox)) {
                float overlapY = calculateOverlapY(scratchBox, wallBox);
                pos.y += (overlapY > 0) ? overlapY + 0.01f : overlapY - 0.01f;
                vel.y = 0;
                syncWorldBox(pos, localBox, scratchBox);
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
