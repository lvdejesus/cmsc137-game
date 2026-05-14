package client.entities;

import client.components.player.MovementInputComponent;
import client.components.player.PlayerStateComponent;
import framework.engine.Engine;
import client.systems.client.Context;
import client.components.*;
import client.components.enemy.EnemyComponent;
import client.components.CollisionComponent;
import client.rendering.Texture;
import client.rendering.Animation;
import client.rendering.TextureAtlas;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.primitives.AABBf;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class Enemy extends Prefab {
    private final int networkId;
    private final float x;
    private final float y;

    public Enemy(Engine<Context> engine, int networkId, float x, float y) {
        super(engine);

        this.networkId = networkId;
        this.x = x;
        this.y = y;
    }

    @Override
    public void spawnClientInternal() {
        TextureAtlas atlas = TextureAtlas.get();
        Texture enemyTexture;
        double currentTime = glfwGetTime();

        String spritePath = "enemy.png";
        this.entity.addComponent(new AnimationComponent(Animation.fromFile(spritePath, 8, 0.1f), (float) currentTime, true));
        this.entity.addComponent(new RenderComponent());
    }


    @Override
    public void spawnCommon() {
        this.entity.addComponent(new TransformComponent(new Vector2f(x, y), new Vector2f(1.5f, 1.5f)));
        this.entity.addComponent(new MovementComponent(50.0f, 20.0f, 10.0f, new Vector2f(0.0f, 0.0f)));
        this.entity.addComponent(new EnemyComponent());
        this.entity.addComponent(new CollisionComponent(new AABBf(
            new Vector3f(-12.0f, -12.0f, 0.0f),
            new Vector3f(12.0f, 12.0f, 0.1f)
        )));
        this.entity.addComponent(new HealthComponent(50.0f)); // Enemy health
        this.entity.addComponent(new NetworkIdComponent(networkId));
        this.entity.addComponent(new NetworkDuplicateComponent(TransformComponent.class, HealthComponent.class));
    }

    public static Enemy deserialize(Engine<Context> engine, int networkId, ByteBuffer bytes) throws IOException {
        float x = bytes.getFloat();
        float y = bytes.getFloat();

        return new Enemy(engine, networkId, x, y);
    }

    public static byte[] serialize(float x, float y) {
        ByteBuffer buf = ByteBuffer.allocate(8);
        buf.putFloat(x);
        buf.putFloat(y);
        return buf.array();
    }
}