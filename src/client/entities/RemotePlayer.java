package client.entities;

import client.components.*;
import client.components.player.MovementInputComponent;
import client.components.player.PlayerNetworkComponent;
import client.components.player.PlayerStateComponent;
import client.rendering.Animation;
import client.systems.client.Context;
import framework.engine.Engine;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.primitives.AABBf;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class RemotePlayer extends Prefab {
    private final int playerIndex;
    private final int networkId;

    public RemotePlayer(Engine<Context> engine, int playerIndex, int networkId) {
        super(engine);

        this.playerIndex = playerIndex;
        this.networkId = networkId;
    }

    @Override
    public void spawn() {
        entity.addComponent(new RenderComponent());

        String spritePath = "players/player" + playerIndex + ".png";
        entity.addComponent(new AnimationComponent(Animation.fromFile(spritePath, 22, 0.1f), (float) glfwGetTime(), 0, 0, false));

        spawnServer();
    }

    @Override
    public void spawnServer() {
        var tc = new TransformComponent(new Vector2f(400.0f, 300.0f), new Vector2f(2.0f, 2.0f));
        entity.addComponent(tc);
        entity.addComponent(new HealthComponent(100.0f));
        entity.addComponent(new CollisionComponent(new AABBf(
            new Vector3f(-16.0f, -16.0f, 0.0f),
            new Vector3f(16.0f, 16.0f, 0.1f)
        )));
        entity.addComponent(new PlayerNetworkComponent(playerIndex));
        entity.addComponent(new PlayerStateComponent());
        entity.addComponent(new MovementInputComponent());
        entity.addComponent(new NetworkIdComponent(networkId));
    }

    public static RemotePlayer deserialize(Engine<Context> engine, int networkId, ByteBuffer bytes) throws IOException {
        int playerIndex = bytes.getInt();
        return new RemotePlayer(engine, playerIndex, networkId);
    }

    public static byte[] serialize(int playerIndex) {
        ByteBuffer bytes = ByteBuffer.allocate(4);
        bytes.putInt(playerIndex);
        return bytes.array();
    }
}