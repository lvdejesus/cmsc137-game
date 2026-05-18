package client.systems.server;

import client.components.*;
import client.network.NetworkSpawnManager;
import client.systems.client.Context;
import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.EntitySystem;
import org.joml.Vector3f;
import org.joml.primitives.AABBf;

public class HealthPickupSystem extends EntitySystem<Context> {
    private ComponentMapper<TransformComponent> tm;
    private ComponentMapper<CollisionComponent> cm;
    private ComponentMapper<HealthComponent> hm;
    private NetworkSpawnManager nsm;

    public HealthPickupSystem(NetworkSpawnManager nsm) {
        this.nsm = nsm;
    }

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);
        tm = engine.getMapper(TransformComponent.class);
        cm = engine.getMapper(CollisionComponent.class);
        hm = engine.getMapper(HealthComponent.class);
    }

    @Override
    public void update(Context ctx) {
        var playerEntities = engine.getFamily(TransformComponent.class, HealthComponent.class).toList();
        var pickupEntities = engine.getFamily(TransformComponent.class, HealthPickupComponent.class).toList();

        for (int playerEntity : playerEntities) {
            HealthComponent hc = hm.get(playerEntity);
            if (!hc.isAlive() || hc.currentHealth >= hc.maxHealth) continue;

            AABBf playerBox = getWorldBox(playerEntity);
            if (playerBox == null) continue;
            for (int pickupEntity : pickupEntities) {
                AABBf pickupBox = getWorldBox(pickupEntity);
                if (pickupBox == null) continue;
                if (pickupBox.intersectsAABB(playerBox)) {
                    hc.currentHealth = Math.min(hc.maxHealth, hc.currentHealth + 25);
                    nsm.despawn(pickupEntity);
                    break;
                }
            }
        }
    }

    private AABBf getWorldBox(int entityId) {
        TransformComponent transform = tm.get(entityId);
        CollisionComponent collision = cm.get(entityId);
        if (transform == null || collision == null) return null;
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
