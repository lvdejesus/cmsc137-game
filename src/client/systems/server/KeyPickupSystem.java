package client.systems.server;

import client.components.*;
import client.network.NetworkSpawnManager;
import client.systems.client.Context;
import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.EntitySystem;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.primitives.AABBf;

import java.util.ArrayList;
import java.util.List;

public class KeyPickupSystem extends EntitySystem<Context> {
    private ComponentMapper<TransformComponent> tm;
    private ComponentMapper<CollisionComponent> cm;
    private ComponentMapper<PlayerKeysComponent> pkcm;
    private ComponentMapper<NetworkIdComponent> nim;
    private NetworkSpawnManager nsm;

    public KeyPickupSystem(NetworkSpawnManager nsm) {
        this.nsm = nsm;
    }

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);
        tm = engine.getMapper(TransformComponent.class);
        cm = engine.getMapper(CollisionComponent.class);
        pkcm = engine.getMapper(PlayerKeysComponent.class);
        nim = engine.getMapper(NetworkIdComponent.class);
    }

    @Override
    public void update(Context ctx) {
        List<Integer> playerEntities = engine.getFamily(TransformComponent.class, PlayerKeysComponent.class).toList();
        List<Integer> keyEntities = engine.getFamily(TransformComponent.class, KeyComponent.class).toList();

        for (int playerEntity : playerEntities) {
            var playerBox = getWorldBox(playerEntity);
            for (int keyEntity : keyEntities) {
                var keyBox = getWorldBox(keyEntity);
                if (keyBox.intersectsAABB(playerBox)) {
                    var playerKeys = pkcm.get(playerEntity);
                    playerKeys.addKey();
                    NetworkIdComponent keyNic = nim.get(keyEntity);
                    nsm.despawn(keyEntity, keyNic.networkId);
                    break;
                }
            }
        }
    }

    private AABBf getWorldBox(int entityId) {
        TransformComponent transform = tm.get(entityId);
        CollisionComponent collision = cm.get(entityId);
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