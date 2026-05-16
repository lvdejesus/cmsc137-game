package client.entities;

import client.components.CollisionComponent;
import client.components.RenderComponent;
import client.components.TransformComponent;
import client.components.NetworkIdComponent;
import client.rendering.Texture;
import client.rendering.TextureAtlas;
import client.systems.client.Context;
import framework.engine.Engine;
import framework.engine.Entity;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.primitives.AABBf;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class Key extends Prefab {
    private final int networkId;
    private final float x;
    private final float y;

    public Key(Engine<Context> engine, int networkId, float x, float y) {
        super(engine);
        this.networkId = networkId;
        this.x = x;
        this.y = y;
    }

    @Override
    public void spawnClientInternal() {
        TextureAtlas atlas = TextureAtlas.get();
        Texture keyTexture = atlas.getRegion("key.png");
        double currentTime = glfwGetTime();
        
        // Just a static texture for the key - no animation
        entity.addComponent(new RenderComponent(keyTexture, 0.0f));
    }

    @Override
    public void spawnCommon() {
        // Small collision box for pickup (smaller than player)
        entity.addComponent(new CollisionComponent(new AABBf(
                new Vector3f(-16.0f, -16.0f, 0.0f),
                new Vector3f(16.0f, 16.0f, 0.1f)
        )));
        entity.addComponent(new TransformComponent(new Vector2f(x, y), new Vector2f(2.0f, 2.0f)));
        entity.addComponent(new NetworkIdComponent(networkId));
    }

    public static Key deserialize(Engine<Context> engine, int networkId, ByteBuffer bytes) {
        float x = bytes.getFloat();
        float y = bytes.getFloat();
        return new Key(engine, networkId, x, y);
    }

    public static byte[] serialize(float x, float y) {
        ByteBuffer buf = ByteBuffer.allocate(8);
        buf.putFloat(x);
        buf.putFloat(y);
        return buf.array();
    }
}