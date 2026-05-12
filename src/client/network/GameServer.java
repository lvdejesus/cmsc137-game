package client.network;

import client.network.messages.Message;
import client.network.messages.client.C_PlayerPosition;
import client.network.messages.client.ClientRegistry;
import client.network.messages.server.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameServer {
    private static final int TCP_PORT = 12345;
    
    private final ClientRegistry clientRegistry = new ClientRegistry();
    private final ServerRegistry serverRegistry = new ServerRegistry();
    private final DiscoveryService discoveryService = new DiscoveryService();
    private final List<ClientConnection> connectedClients = new CopyOnWriteArrayList<>();

    private ServerSocket serverSocket;
    private Thread serverThread;
    private volatile boolean gameStarted = false;

    private static final int NUM_PLAYERS = 2;

    public void start(String localIP) {
        this.gameStarted = false;
        this.serverThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket();
                serverSocket.setReuseAddress(true);
                serverSocket.bind(new InetSocketAddress(TCP_PORT));
                System.out.println("Server started on IP: " + localIP);

                discoveryService.startResponding(localIP);

                while (!serverSocket.isClosed() && connectedClients.size() < 3) {
                    try {
                        Socket socket = serverSocket.accept();
                        int playerId = connectedClients.size() + 2;
                        ClientConnection client = new ClientConnection(socket, playerId);
                        connectedClients.add(client);
                        broadcastPlayerCount();
                        checkStartGame();
                        System.out.println("Client connected: " + socket.getInetAddress() + " assigned ID: " + playerId);
                    } catch (IOException e) {
                        if (!serverSocket.isClosed()) e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                System.err.println("Failed to start server: " + e.getMessage());
            }
        });
        this.serverThread.start();
    }

    public void startGame() {
        if (!gameStarted) {
            gameStarted = true;
            for (ClientConnection client : connectedClients) {
                client.send(new S_StartGame());
            }
        }
    }

    private void broadcastPlayerCount() {
        int count = connectedClients.size();
        for (ClientConnection client : connectedClients) {
            client.send(new S_PlayerCount(count));
        }
    }

    private void checkStartGame() {
        if (!gameStarted && connectedClients.size() >= NUM_PLAYERS) {
            startGame();
        }
    }

    public void stop() {
        discoveryService.stop();
        try {
            if (serverSocket != null) serverSocket.close();
            for (ClientConnection c : connectedClients) c.close();
            connectedClients.clear();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private class ClientConnection {
        Socket socket;
        DataOutputStream out;
        int playerId;

        ClientConnection(Socket socket, int playerId) throws IOException {
            this.socket = socket;
            this.playerId = playerId;
            this.out = new DataOutputStream(socket.getOutputStream());
            send(new S_AssignId(playerId));

            new Thread(() -> {
                try {
                    DataInputStream in = new DataInputStream(socket.getInputStream());
                    while (!socket.isClosed()) {
                        Message msg = clientRegistry.receive(in);
                        handleMessage(msg);
                    }
                } catch (IOException e) {
                    System.out.println("Client " + playerId + " disconnected.");
                    handleDisconnect();
                }
            }).start();
        }

        private void handleMessage(Message msg) {
            if (msg instanceof C_PlayerPosition m) {
                for (ClientConnection client : connectedClients) {
                    if (client.playerId != this.playerId) {
                        client.send(new S_PlayerPosition(m.getSenderId(), m.getX(), m.getY(), m.getRotation()));
                    }
                }
            }
        }

        private void handleDisconnect() {
            connectedClients.remove(this);
            broadcastPlayerCount();
            try { socket.close(); } catch (IOException ignored) {}
        }

        void send(Message msg) {
            synchronized (out) {
                try {
                    serverRegistry.send(out, msg);
                } catch (IOException e) {
                    handleDisconnect();
                }
            }
        }

        void close() {
            try { socket.close(); } catch (IOException ignored) {}
        }
    }
}