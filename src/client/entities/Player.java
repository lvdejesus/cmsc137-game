package client.entities;

import client.components.player.MovementInputComponent;
import client.components.player.PlayerNetworkComponent;
import framework.engine.Engine;
import client.systems.client.Context;
import client.components.*;
import client.components.player.PlayerStateComponent;
import client.components.player.PlayerTagComponent;
import client.components.CollisionComponent;
import client.rendering.Animation;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.primitives.AABBf;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class Player extends Prefab {
    private int playerIndex;
    private int networkId;

    public static float maxHealth = 125.0f;
    public static float movement_speed = 1000.0f;
    public static float friction = 200.0f;
    public static float acceleration = 500.0f;

    public Player(Engine<Context> engine, int playerIndex, int networkId) {
        super(engine);

        this.playerIndex = playerIndex;
        this.networkId = networkId;
    }

    public void spawnClientInternal() {
        double currentTime = glfwGetTime();

        var spawnPosition = new Vector2f(156 * 64.0f, 156 * 64.0f);
        spawnPosition.add(new Vector2f((float) Math.cos(Math.toRadians(45.0f * playerIndex)), (float) Math.sin(Math.toRadians(45.0f * playerIndex))).mul(16.0f * 5.0f));

        // Add components to the internal entity
        entity.addComponent(new TransformComponent(spawnPosition, new Vector2f(2.0f, 2.0f)));
        entity.addComponent(new MovementComponent(movement_speed, acceleration, friction));
        entity.addComponent(new RenderComponent());

        String spritePath = "players/player" + playerIndex + ".png";
        entity.addComponent(new AnimationComponent(Animation.fromFile(spritePath, 22, 0.1f), (float) currentTime, 0, 0, false));
        entity.addComponent(new PlayerNetworkComponent(playerIndex));
        entity.addComponent(new PlayerStateComponent());
        entity.addComponent(new PlayerTagComponent());
        entity.addComponent(new CollisionComponent(new AABBf(
            new Vector3f(-16.0f, -16.0f, 0.0f),
            new Vector3f(16.0f, 16.0f, 0.1f)
        )));
        entity.addComponent(new HealthComponent(maxHealth));
        entity.addComponent(new MovementInputComponent());
        entity.addComponent(new NetworkIdComponent(networkId));
        entity.addComponent(new PlayerKeysComponent());
        entity.addComponent(new PlayerUpgradeComponent());
        entity.addComponent(new ExperienceComponent());
        entity.addComponent(new FollowComponent());
        entity.addComponent(new NetworkDuplicateComponent(PlayerUpgradeComponent.class, ExperienceComponent.class, PlayerKeysComponent.class, HealthComponent.class));
    }

    public static Player deserialize(Engine<Context> engine, int networkId, ByteBuffer bytes) throws IOException {
        int playerIndex = bytes.getInt();
        return new Player(engine, playerIndex, networkId);
    }

    public static byte[] serialize(int playerIndex) {
        ByteBuffer bytes = ByteBuffer.allocate(4);
        bytes.putInt(playerIndex);
        return bytes.array();
    }

    public String getState() {
        PlayerStateComponent stateComponent = entity.getComponent(PlayerStateComponent.class);
        return stateComponent != null ? stateComponent.get() : "unknown";
    }

    public float getHealth() {
        HealthComponent healthComponent = entity.getComponent(HealthComponent.class);
        return healthComponent.getHealth();
    }
}