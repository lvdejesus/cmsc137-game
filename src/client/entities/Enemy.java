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
    public enum EnemyType {
        Regular,
        Boss,
    }

    private final int networkId;
    private final float x;
    private final float y;
    private final EnemyType type;

    public Enemy(Engine<Context> engine, int networkId, float x, float y, EnemyType type) {
        super(engine);

        this.networkId = networkId;
        this.x = x;
        this.y = y;
        this.type = type;
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
        this.entity.addComponent(new MovementComponent(50.0f, 20.0f, 10.0f, new Vector2f(0.0f, 0.0f)));
        if (type == EnemyType.Regular) {
            this.entity.addComponent(new HealthComponent(25.0f)); // Enemy health
            this.entity.addComponent(new TransformComponent(new Vector2f(x, y), new Vector2f(2.0f, 2.0f)));
            this.entity.addComponent(new CollisionComponent(new AABBf(
                new Vector3f(-16.0f, -16.0f, 0.0f),
                new Vector3f(16.0f, 16.0f, 0.1f)
            )));
            this.entity.addComponent(new EnemyComponent(1.2f, 0.8f));
        } else if (type == EnemyType.Boss) {
            this.entity.addComponent(new HealthComponent(1250.0f)); // Enemy health
            this.entity.addComponent(new TransformComponent(new Vector2f(x, y), new Vector2f(5.0f, 5.0f)));
            this.entity.addComponent(new CollisionComponent(new AABBf(
                new Vector3f(-40.0f, -40.0f, 0.0f),
                new Vector3f(40.0f, 40.0f, 0.1f)
            )));
            this.entity.addComponent(new EnemyComponent(0.2f, 0.1f));
        }
        this.entity.addComponent(new NetworkIdComponent(networkId));
        this.entity.addComponent(new NetworkDuplicateComponent(TransformComponent.class, HealthComponent.class));
    }

    public static Enemy deserialize(Engine<Context> engine, int networkId, ByteBuffer bytes) throws IOException {
        float x = bytes.getFloat();
        float y = bytes.getFloat();
        EnemyType type = EnemyType.values()[bytes.getInt()];

        return new Enemy(engine, networkId, x, y, type);
    }

    public static byte[] serialize(float x, float y, EnemyType type) {
        ByteBuffer buf = ByteBuffer.allocate(12);
        buf.putFloat(x);
        buf.putFloat(y);
        buf.putInt(type.ordinal());
        return buf.array();
    }

    @Override
    public boolean onDespawn() {
        this.entity.removeComponent(MovementComponent.class);
        this.entity.removeComponent(EnemyComponent.class);
        this.entity.removeComponent(CollisionComponent.class);
        this.entity.removeComponent(HealthComponent.class);
        this.entity.removeComponent(NetworkIdComponent.class);
        this.entity.removeComponent(NetworkDuplicateComponent.class);
        this.entity.removeComponent(AnimationComponent.class);

        double currentTime = glfwGetTime();
        String spritePath = "enemyExplosion.png";
        this.entity.addComponent(new AnimationComponent(Animation.fromFile(spritePath, 12, 0.1f), (float) currentTime, false));
        this.entity.addComponent(new DespawnTimerComponent(1.2f));

        return false;
    }
}