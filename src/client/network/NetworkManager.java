package client.network;

import client.components.AnimationComponent;
import client.components.RenderComponent;
import client.components.TransformComponent;
import client.components.player.PlayerStateComponent;
import client.network.messages.Message;
import client.network.messages.server.*;
import client.util.Statistics;

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

    public volatile int playerIndex = -1;
    public int networkId;
    private volatile int playerCount = 0;
    private volatile boolean gameStarted = false;
    private volatile boolean bossDefeated = false;
    private volatile boolean gameOver = false;
    private volatile boolean bossFightStarted = false;
    private S_GameResult gameResult = null;
    private String lastServerIp = "127.0.0.1";

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

        registerHandler(S_GameResult.class, (m) -> {
            gameResult = m;
            if (m.victory) {
                bossDefeated = true;
            } else {
                gameOver = true;
            }
        });

        registerHandler(S_BossFightStart.class, (m) -> {
            bossFightStarted = true;
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

    public boolean isBossDefeated() {
        return bossDefeated;
    }

    public boolean isBossFightStarted() {
        return bossFightStarted;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public S_GameResult getGameResult() {
        return gameResult;
    }

    public java.util.List<String> discoverHosts() {
        return new DiscoveryService().discoverHosts();
    }

    public void joinGame(String ip, int port) {
        lastServerIp = ip;
        client.connect(ip, port);
    }

    public String getLastServerIp() {
        return lastServerIp;
    }

    public boolean getConnectionFailed() {
        return client.connectionFailed;
    }

    public void sendMessage(Message message) {
        client.sendMessage(message);
    }

    public void stop() {
        client.stop();
        playerIndex = 0;
        networkId = 0;
        playerCount = 0;
        gameStarted = false;
        bossDefeated = false;
        bossFightStarted = false;
        gameOver = false;
        gameResult = null;
        inQueue.clear();
    }
}