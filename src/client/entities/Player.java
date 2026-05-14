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
    public Player(Engine<Context> engine, int playerIndex, int networkId) {
        super(engine);

        this.playerIndex = playerIndex;
        this.networkId = networkId;
    }

    public void spawn() {

        float movement_speed = 1000.0f;
        float friction = 200.0f;
        float acceleration = 500.0f;
        double currentTime = glfwGetTime();

        // Add components to the internal entity
        entity.addComponent(new TransformComponent(new Vector2f(400.0f, 300.0f), new Vector2f(2.0f, 2.0f)));
        entity.addComponent(new MovementComponent(movement_speed, acceleration, friction, new Vector2f(0.0f, 0.0f)));
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
        entity.addComponent(new HealthComponent(6.0f));
        entity.addComponent(new MovementInputComponent());
        entity.addComponent(new NetworkIdComponent(networkId));
    }

    @Override
    public void spawnServer() {

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