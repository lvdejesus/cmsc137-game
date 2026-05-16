package client.network;

import client.components.AnimationComponent;
import client.components.RenderComponent;
import client.components.TransformComponent;
import client.components.player.PlayerStateComponent;
import client.network.messages.Message;
import client.network.messages.server.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import client.rendering.Animation;
import client.systems.client.Context;
import framework.engine.Entity;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class NetworkManager {
    private static NetworkManager instance;
    private final GameClient client;

    public volatile int playerIndex = 1;
    public int networkId;
    private volatile int playerCount = 0;
    private volatile boolean gameStarted = false;

    public ConcurrentLinkedQueue<Message> inQueue = new ConcurrentLinkedQueue<>();

    private final Map<Class<?>, MessageHandler<?>> handlers = new HashMap<>();

    @FunctionalInterface
    public interface MessageHandler<T> {
        void handle(T msg);
    }

    private <T extends Message> void registerHandler(Class<T> clazz, MessageHandler<T> handler) {
        handlers.put(clazz, handler);
    }

    private NetworkManager() {
        this.client = new GameClient(handlers, inQueue);

        registerHandler(S_AssignId.class, (m) -> {
            playerIndex = m.getPlayerId();
            networkId = m.getNetworkId();
        });

        registerHandler(S_PlayerCount.class, (m) -> {
            playerCount = m.getCount();
        });

        registerHandler(S_StartGame.class, (m) -> {
            gameStarted = true;
        });
    }

    public static NetworkManager getInstance() {
        if (instance == null) instance = new NetworkManager();
        return instance;
    }

    public int getPlayerIndex() {
        return playerIndex;
    }

    public int getNetworkId() {
        return networkId;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public boolean isHost() {
        return playerIndex == 1;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public java.util.List<String> discoverHosts() {
        return new DiscoveryService().discoverHosts();
    }

    public void joinGame(String ip, int port) {
        client.connect(ip, port);
    }

    public void sendMessage(Message message) {
        client.sendMessage(message);
    }

    public void stop() {
        client.stop();
    }
}