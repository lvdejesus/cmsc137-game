package client.systems.client;

import client.components.CollisionComponent;
import client.components.NetworkIdComponent;
import client.components.WallComponent;
import client.components.bullet.BulletComponent;
import client.components.TransformComponent;
import client.network.NetworkSpawnManager;
import client.util.SpatialHashGrid;
import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.IteratingEntitySystem;
import org.joml.primitives.AABBf;

import java.util.Set;
import java.util.HashSet;

public class BulletWallTileCollisionSystem extends IteratingEntitySystem<Context> {
    private ComponentMapper<TransformComponent> transformMapper;
    private ComponentMapper<CollisionComponent> collisionMapper;
    private ComponentMapper<NetworkIdComponent> networkIdMapper;
    private final NetworkSpawnManager networkSpawnManager;
    private final SpatialHashGrid wallGrid;

    private final AABBf bulletWorldBox = new AABBf(0.0f, 0.0f, Float.NEGATIVE_INFINITY, 0.0f, 0.0f, Float.POSITIVE_INFINITY);
    private final AABBf wallWorldBox = new AABBf(0.0f, 0.0f, Float.NEGATIVE_INFINITY, 0.0f, 0.0f, Float.POSITIVE_INFINITY);
    private final Set<Integer> potentialWalls = new HashSet<>();

    public BulletWallTileCollisionSystem(NetworkSpawnManager nsm) {
        super(BulletComponent.class);
        this.networkSpawnManager = nsm;
        this.wallGrid = new SpatialHashGrid(64);
    }

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);
        this.transformMapper = engine.getMapper(TransformComponent.class);
        this.collisionMapper = engine.getMapper(CollisionComponent.class);
        this.networkIdMapper = engine.getMapper(NetworkIdComponent.class);
    }

    @Override
    public void update(Context ctx) {
        wallGrid.clear();
        Iterable<Integer> walls = engine.getFamily(WallComponent.class)::iterator;

        for (int wallId : walls) {
            syncWorldBox(wallId, wallWorldBox);
            System.out.println(wallWorldBox);
            wallGrid.addEntity(wallId, wallWorldBox);
        }

        super.update(ctx);
    }

    @Override
    protected void processEntity(int bulletId, Context ctx) {
        syncWorldBox(bulletId, bulletWorldBox);

        potentialWalls.clear();
        wallGrid.getPotentialColliders(bulletWorldBox, potentialWalls);

        for (int wallId : potentialWalls) {
            syncWorldBox(wallId, wallWorldBox);

            if (bulletWorldBox.intersectsAABB(wallWorldBox)) {
                var networkIdComp = networkIdMapper.get(bulletId);
                if (networkIdComp != null) {
                    networkSpawnManager.despawn(bulletId, networkIdComp.networkId);
                    return;
                }
            }
        }
    }

    private void syncWorldBox(int entityId, AABBf out) {
        var transform = transformMapper.get(entityId);
        var collision = collisionMapper.get(entityId);

        if (transform != null && collision != null) {
            out.minX = collision.boundingBox.minX() + transform.position.x;
            out.minY = collision.boundingBox.minY() + transform.position.y;
            out.maxX = collision.boundingBox.maxX() + transform.position.x;
            out.maxY = collision.boundingBox.maxY() + transform.position.y;
        }
    }
}
