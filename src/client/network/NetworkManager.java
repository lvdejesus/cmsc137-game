package client.network;

import client.network.messages.server.S_PlayerPosition;
import java.util.concurrent.ConcurrentHashMap;
import org.joml.Vector3f;

public class NetworkManager {
    private static NetworkManager instance;
    private static final int TCP_PORT = 12345;
    
    private final GameClient client;
    private final ConcurrentHashMap<Integer, Vector3f> remotePlayerStates = new ConcurrentHashMap<>();
    
    private volatile int playerIndex = 1;
    private volatile int playerCount = 0;
    private volatile boolean gameStarted = false;

    private NetworkManager() {
        this.client = new GameClient();
        client.setMessageHandler(msg -> {
            if (msg instanceof client.network.messages.server.S_AssignId m) {
                playerIndex = m.getPlayerId();
            } else if (msg instanceof client.network.messages.server.S_PlayerCount m) {
                playerCount = m.getCount();
            } else if (msg instanceof client.network.messages.server.S_StartGame) {
                gameStarted = true;
            } else if (msg instanceof S_PlayerPosition m) {
                if (m.getSenderId() != playerIndex) {
                    remotePlayerStates.put(m.getSenderId(), new Vector3f(m.getX(), m.getY(), m.getRotation()));
                }
            }
        });
    }

    public static NetworkManager getInstance() {
        if (instance == null) instance = new NetworkManager();
        return instance;
    }

    public int getPlayerIndex() { return playerIndex; }
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