package client.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import org.joml.Vector3f;

public class NetworkManager {
    private static NetworkManager instance;
    private static final int TCP_PORT = 12345;
    private static final int UDP_PORT = 12346;
    private static final String DISCOVERY_MESSAGE = "GAME_HOST_DISCOVERY";

    // Message types
    public static final int MSG_ASSIGN_ID = 1;
    public static final int MSG_PLAYER_COUNT = 2;
    public static final int MSG_START_GAME = 3;
    public static final int MSG_PLAYER_POS = 4;

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private final List<ClientHandler> connectedClients = new CopyOnWriteArrayList<>();
    private volatile boolean isHost = false;
    private final String localIP;
    private volatile int playerIndex = 1; // Default for host
    private volatile int remotePlayerCount = 1;
    private volatile boolean gameStarted = false;

    // Remote player positional data (ID -> Vector3f(x, y, rotation))
    private final ConcurrentHashMap<Integer, Vector3f> remotePlayerStates = new ConcurrentHashMap<>();

    private Thread serverThread;
    private Thread discoveryThread;
    private Thread clientListenerThread;
    private DataOutputStream clientOut;

    public static NetworkManager getInstance() {
        if (instance == null) instance = new NetworkManager();
        return instance;
    }

    private NetworkManager() {
        this.localIP = findLocalIP();
    }

    public String getLocalIP() {
        return localIP;
    }

    public boolean isHost() {
        return isHost;
    }

    public int getPlayerIndex() {
        return playerIndex;
    }

    public int getPlayerCount() {
        return isHost ? connectedClients.size() + 1 : remotePlayerCount;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public ConcurrentHashMap<Integer, Vector3f> getRemotePlayerStates() {
        return remotePlayerStates;
    }

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
            startDiscoveryResponder();
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
        int count = getPlayerCount();
        for (ClientHandler client : connectedClients) {
            client.sendMessage(MSG_PLAYER_COUNT, count, 0, 0, 0);
        }
    }

    public void startGame() {
        if (isHost) {
            this.gameStarted = true;
            for (ClientHandler client : connectedClients) {
                client.sendMessage(MSG_START_GAME, 0, 0, 0, 0);
            }
        }
    }

    public void broadcastPosition(float x, float y, float rot) {
        if (isHost) {
            // Host broadcasts to all clients
            for (ClientHandler client : connectedClients) {
                client.sendMessage(MSG_PLAYER_POS, playerIndex, x, y, rot);
            }
        } else if (clientOut != null) {
            // Client sends to host (who will then broadcast it)
            synchronized (clientOut) {
                try {
                    clientOut.writeInt(MSG_PLAYER_POS);
                    clientOut.writeInt(playerIndex);
                    clientOut.writeFloat(x);
                    clientOut.writeFloat(y);
                    clientOut.writeFloat(rot);
                    clientOut.flush();
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
                    int type = in.readInt();
                    int id = in.readInt();
                    float x = in.readFloat();
                    float y = in.readFloat();
                    float rot = in.readFloat();
                    handleMessage(type, id, x, y, rot);
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

    private void handleMessage(int type, int id, float x, float y, float rot) {
        switch (type) {
            case MSG_ASSIGN_ID:
                this.playerIndex = id;
                System.out.println("Assigned Player ID: " + playerIndex);
                break;
            case MSG_PLAYER_COUNT:
                this.remotePlayerCount = id; // id field is used for count here
                System.out.println("Received player count update: " + id);
                break;
            case MSG_START_GAME:
                this.gameStarted = true;
                System.out.println("Received start game signal!");
                break;
            case MSG_PLAYER_POS:
                // Update remote player state
                if (id != this.playerIndex) {
                    remotePlayerStates.put(id, new Vector3f(x, y, rot));
                }
                break;
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
            sendMessage(MSG_ASSIGN_ID, id, 0, 0, 0);
            
            new Thread(() -> {
                try {
                    while (!socket.isClosed()) {
                        int type = in.readInt();
                        int senderId = in.readInt();
                        float x = in.readFloat();
                        float y = in.readFloat();
                        float rot = in.readFloat();
                        
                        if (type == MSG_PLAYER_POS) {
                            // Update host's local state
                            remotePlayerStates.put(senderId, new Vector3f(x, y, rot));
                            // Broadcast to OTHER clients
                            for (ClientHandler other : connectedClients) {
                                if (other != this) {
                                    other.sendMessage(MSG_PLAYER_POS, senderId, x, y, rot);
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Client " + id + " disconnected.");
                    handleDisconnect();
                }
            }).start();
        }
        
        private void handleDisconnect() {
            connectedClients.remove(this);
            remotePlayerStates.remove(id); // Remove their entity state
            broadcastPlayerCount();
            try { socket.close(); } catch (IOException ignored) {}
        }

        void sendMessage(int type, int id, float x, float y, float rot) {
            synchronized (out) {
                try {
                    out.writeInt(type);
                    out.writeInt(id);
                    out.writeFloat(x);
                    out.writeFloat(y);
                    out.writeFloat(rot);
                    System.out.printf("Sent: %d %d %f %f %f\n", type, id, x, y, rot);
                    out.flush();
                } catch (IOException e) {
                    handleDisconnect();
                }
            }
        }
    }

    private void startDiscoveryResponder() {
        discoveryThread = new Thread(() -> {
            try (DatagramSocket socket = new DatagramSocket(UDP_PORT)) {
                byte[] buffer = new byte[256];
                while (!socket.isClosed()) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    String message = new String(packet.getData(), 0, packet.getLength());
                    if (DISCOVERY_MESSAGE.equals(message)) {
                        byte[] response = localIP.getBytes();
                        DatagramPacket responsePacket = new DatagramPacket(
                            response, response.length, packet.getAddress(), packet.getPort()
                        );
                        socket.send(responsePacket);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        discoveryThread.setDaemon(true);
        discoveryThread.start();
    }

    public List<String> discoverHosts() {
        List<String> hosts = new ArrayList<>();
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            socket.setSoTimeout(1000);
            byte[] message = DISCOVERY_MESSAGE.getBytes();
            DatagramPacket packet = new DatagramPacket(
                message, message.length, InetAddress.getByName("255.255.255.255"), UDP_PORT
            );
            socket.send(packet);
            byte[] buffer = new byte[256];
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < 1000) {
                try {
                    DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
                    socket.receive(responsePacket);
                    String hostIP = new String(responsePacket.getData(), 0, responsePacket.getLength());
                    if (!hosts.contains(hostIP)) hosts.add(hostIP);
                } catch (SocketTimeoutException e) { break; }
            }
        } catch (IOException e) { e.printStackTrace(); }
        return hosts;
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
        try {
            if (serverSocket != null) serverSocket.close();
            if (clientSocket != null) clientSocket.close();
            for (ClientHandler c : connectedClients) c.socket.close();
            connectedClients.clear();
            remotePlayerStates.clear();
        } catch (IOException e) { e.printStackTrace(); }
    }
}
