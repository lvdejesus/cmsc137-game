package client.network;

import client.components.AnimationComponent;
import client.components.RenderComponent;
import client.components.TransformComponent;
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
    private static final int TCP_PORT = 12345;
    
    private final GameClient client;
    private final ConcurrentHashMap<Integer, Vector3f> remotePlayerStates = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<Integer, Integer> networkEntityMap = new ConcurrentHashMap<>();

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

    public int getPlayerIndex() { return playerIndex; }
    public int getNetworkId() { return networkId; }
    public int getPlayerCount() { return playerCount; }
    public boolean isGameStarted() { return gameStarted; }
    public ConcurrentHashMap<Integer, Vector3f> getRemotePlayerStates() { return remotePlayerStates; }

    public java.util.List<String> discoverHosts() {
        return new DiscoveryService().discoverHosts();
    }

    public void joinGame(String ip) {
        client.connect(ip, TCP_PORT);
    }

    public void broadcastPosition(float x, float y, float rot) {
        client.sendPosition(playerIndex, x, y, rot);
    }

    private String findLocalIP() {
        try {
            java.util.Enumeration<java.net.NetworkInterface> interfaces = java.net.NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                java.net.NetworkInterface ni = interfaces.nextElement();
                if (ni.isLoopback() || !ni.isUp()) continue;
                java.util.Enumeration<java.net.InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    java.net.InetAddress addr = addresses.nextElement();
                    if (addr instanceof java.net.Inet4Address) return addr.getHostAddress();
                }
            }
        } catch (java.net.SocketException e) { e.printStackTrace(); }
        return "127.0.0.1";
    }

    public void stop() {
        client.stop();
        remotePlayerStates.clear();
    }
}