package client.network;

import client.network.messages.Message;
import client.network.messages.client.ClientRegistry;
import client.network.messages.client.C_PlayerPosition;
import client.network.messages.server.ServerRegistry;
import client.network.messages.server.S_AssignId;
import client.network.messages.server.S_PlayerCount;
import client.network.messages.server.S_StartGame;
import client.network.messages.server.S_PlayerPosition;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import org.joml.Vector3f;

public class NetworkManager {
    private static NetworkManager instance;
    private static final int TCP_PORT = 12345;
    
    private final DiscoveryService discoveryService = new DiscoveryService();
    private final ServerRegistry serverRegistry = new ServerRegistry();
    private final ClientRegistry clientRegistry = new ClientRegistry();

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private final List<ClientHandler> connectedClients = new CopyOnWriteArrayList<>();
    private volatile boolean isHost = false;
    private final String localIP;
    private volatile int playerIndex = 1;
    private volatile int remotePlayerCount = 1;
    private volatile boolean gameStarted = false;

    private final ConcurrentHashMap<Integer, Vector3f> remotePlayerStates = new ConcurrentHashMap<>();

    private Thread serverThread;
    private Thread clientListenerThread;
    private DataOutputStream clientOut;

    public static NetworkManager getInstance() {
        if (instance == null) instance = new NetworkManager();
        return instance;
    }

    private NetworkManager() {
        this.localIP = findLocalIP();
    }

    public String getLocalIP() { return localIP; }
    public boolean isHost() { return isHost; }
    public int getPlayerIndex() { return playerIndex; }
    public int getPlayerCount() { return isHost ? connectedClients.size() + 1 : remotePlayerCount; }
    public boolean isGameStarted() { return gameStarted; }
    public ConcurrentHashMap<Integer, Vector3f> getRemotePlayerStates() { return remotePlayerStates; }

    public List<String> discoverHosts() { return discoveryService.discoverHosts(); }

    public void startHost() {
        try {
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(TCP_PORT));
            
            this.isHost = true;
            this.playerIndex = 1;
            this.gameStarted = false;
            System.out.println("Host started on IP: " + localIP);

            serverThread = new Thread(() -> {
                while (!serverSocket.isClosed() && connectedClients.size() < 3) {
                    try {
                        Socket socket = serverSocket.accept();
                        ClientHandler handler = new ClientHandler(socket, connectedClients.size() + 2);
                        connectedClients.add(handler);
                        broadcastPlayerCount();
                        System.out.println("Client connected: " + socket.getInetAddress() + " assigned ID: " + handler.id);
                    } catch (IOException e) {
                        if (!serverSocket.isClosed()) e.printStackTrace();
                    }
                }
            });
            serverThread.start();
            discoveryService.startResponding(localIP);
        } catch (IOException e) {
            System.err.println("Failed to start host: " + e.getMessage());
            javax.swing.JOptionPane.showMessageDialog(null, 
                "Failed to start host on port " + TCP_PORT + ".\n" +
                "Make sure no other instance of the game is already hosting!\n" +
                "If you recently closed the game, wait a few seconds or check for zombie processes.", 
                "Hosting Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void broadcastPlayerCount() {
        for (ClientHandler client : connectedClients) {
            client.sendMessage(new S_PlayerCount(getPlayerCount()));
        }
    }

    public void startGame() {
        if (isHost) {
            this.gameStarted = true;
            for (ClientHandler client : connectedClients) {
                client.sendMessage(new S_StartGame());
            }
        }
    }

    public void broadcastPosition(float x, float y, float rot) {
        C_PlayerPosition msg = new C_PlayerPosition(playerIndex, x, y, rot);
        if (isHost) {
            for (ClientHandler client : connectedClients) {
                client.sendMessage(new S_PlayerPosition(playerIndex, x, y, rot));
            }
        } else if (clientOut != null) {
            synchronized (clientOut) {
                try {
                    clientRegistry.send(clientOut, msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void joinGame(String ip) throws IOException {
        clientSocket = new Socket();
        clientSocket.connect(new InetSocketAddress(ip, TCP_PORT), 10000);
        isHost = false;
        this.gameStarted = false;
        this.clientOut = new DataOutputStream(clientSocket.getOutputStream());
        System.out.println("Successfully joined host at: " + ip);
        
        clientListenerThread = new Thread(() -> {
            try (DataInputStream in = new DataInputStream(clientSocket.getInputStream())) {
                while (!clientSocket.isClosed()) {
                    Message msg = serverRegistry.receive(in);
                    handleServerMessage(msg);
                }
            } catch (IOException e) {
                System.out.println("Disconnected from host.");
                handleHostDisconnect();
            }
        });
        clientListenerThread.start();
    }

    private void handleHostDisconnect() {
        this.gameStarted = false;
        javax.swing.JOptionPane.showMessageDialog(null, "Host disconnected.", "Network Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        System.exit(0);
    }

    private void handleServerMessage(Message msg) {
        if (msg instanceof S_AssignId m) {
            this.playerIndex = m.getPlayerId();
            System.out.println("Assigned Player ID: " + playerIndex);
        } else if (msg instanceof S_PlayerCount m) {
            this.remotePlayerCount = m.getCount();
            System.out.println("Received player count update: " + m.getCount());
        } else if (msg instanceof S_StartGame) {
            this.gameStarted = true;
            System.out.println("Received start game signal!");
        } else if (msg instanceof S_PlayerPosition m) {
            if (m.getSenderId() != this.playerIndex) {
                remotePlayerStates.put(m.getSenderId(), new Vector3f(m.getX(), m.getY(), m.getRotation()));
            }
        }
    }

    private class ClientHandler {
        Socket socket;
        DataOutputStream out;
        DataInputStream in;
        int id;

        ClientHandler(Socket socket, int id) throws IOException {
            this.socket = socket;
            this.id = id;
            this.out = new DataOutputStream(socket.getOutputStream());
            this.in = new DataInputStream(socket.getInputStream());
            sendMessage(new S_AssignId(id));
            
            new Thread(() -> {
                try {
                    while (!socket.isClosed()) {
                        Message msg = clientRegistry.receive(in);
                        handleClientMessage(msg);
                    }
                } catch (IOException e) {
                    System.out.println("Client " + id + " disconnected.");
                    handleDisconnect();
                }
            }).start();
        }
        
        private void handleClientMessage(Message msg) {
            if (msg instanceof C_PlayerPosition m) {
                remotePlayerStates.put(m.getSenderId(), new Vector3f(m.getX(), m.getY(), m.getRotation()));
                for (ClientHandler other : connectedClients) {
                    if (other != this) {
                        other.sendMessage(new S_PlayerPosition(m.getSenderId(), m.getX(), m.getY(), m.getRotation()));
                    }
                }
            }
        }
        
        private void handleDisconnect() {
            connectedClients.remove(this);
            remotePlayerStates.remove(id);
            broadcastPlayerCount();
            try { socket.close(); } catch (IOException ignored) {}
        }

        void sendMessage(Message msg) {
            synchronized (out) {
                try {
                    serverRegistry.send(out, msg);
                    System.out.println("Sent: " + msg.getClass().getSimpleName());
                } catch (IOException e) {
                    handleDisconnect();
                }
            }
        }
    }

    private String findLocalIP() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface networkInterface : Collections.list(interfaces)) {
                if (networkInterface.isLoopback() || !networkInterface.isUp()) continue;
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                for (InetAddress address : Collections.list(addresses)) {
                    if (address instanceof Inet4Address) return address.getHostAddress();
                }
            }
        } catch (SocketException e) { e.printStackTrace(); }
        return "127.0.0.1";
    }

    public void stop() {
        discoveryService.stop();
        try {
            if (serverSocket != null) serverSocket.close();
            if (clientSocket != null) clientSocket.close();
            for (ClientHandler c : connectedClients) c.socket.close();
            connectedClients.clear();
            remotePlayerStates.clear();
        } catch (IOException e) { e.printStackTrace(); }
    }
}