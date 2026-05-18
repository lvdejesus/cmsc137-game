package client.entities;

import client.components.*;
import client.rendering.Texture;
import client.rendering.TextureAtlas;
import client.systems.client.Context;
import framework.engine.Engine;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.primitives.AABBf;

import java.nio.ByteBuffer;

public class HealthPickup extends Prefab {
    private final int networkId;
    private final float x;
    private final float y;

    public HealthPickup(Engine<Context> engine, int networkId, float x, float y) {
        super(engine);
        this.networkId = networkId;
        this.x = x;
        this.y = y;
    }

    @Override
    public void spawnClientInternal() {
        Texture texture = TextureAtlas.get().getRegion("health.png");
        entity.addComponent(new RenderComponent(texture, 0.0f));
    }

    @Override
    public void spawnCommon() {
        entity.addComponent(new CollisionComponent(new AABBf(
                new Vector3f(-12.0f, -12.0f, 0.0f),
                new Vector3f(12.0f, 12.0f, 0.1f)
        )));
        entity.addComponent(new TransformComponent(new Vector2f(x, y), new Vector2f(1.5f, 1.5f)));
        entity.addComponent(new HealthPickupComponent());
        entity.addComponent(new NetworkIdComponent(networkId));
    }

    public static HealthPickup deserialize(Engine<Context> engine, int networkId, ByteBuffer bytes) {
        float x = bytes.getFloat();
        float y = bytes.getFloat();
        return new HealthPickup(engine, networkId, x, y);
    }

    public static byte[] serialize(float x, float y) {
        ByteBuffer buf = ByteBuffer.allocate(8);
        buf.putFloat(x);
        buf.putFloat(y);
        return buf.array();
    }
}
