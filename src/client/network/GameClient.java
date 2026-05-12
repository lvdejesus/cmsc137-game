package client.network;

import client.network.messages.Message;
import client.network.messages.client.C_PlayerPosition;
import client.network.messages.client.ClientRegistry;
import client.network.messages.server.ServerRegistry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class GameClient {
    private final ClientRegistry clientRegistry = new ClientRegistry();
    private final ServerRegistry serverRegistry = new ServerRegistry();
    
    private Socket socket;
    private DataOutputStream out;
    private volatile boolean connected = false;

    public interface MessageHandler {
        void onMessage(Message msg);
    }
    private MessageHandler handler;

    public void setMessageHandler(MessageHandler handler) {
        this.handler = handler;
    }

    public void connect(String ip, int port) {
        new Thread(() -> {
            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(ip, port), 10000);
                connected = true;
                out = new DataOutputStream(socket.getOutputStream());
                System.out.println("Client connected to " + ip);

                Thread listenerThread = new Thread(() -> {
                    try (DataInputStream in = new DataInputStream(socket.getInputStream())) {
                        while (!socket.isClosed()) {
                            Message msg = serverRegistry.receive(in);
                            if (handler != null) {
                                handler.onMessage(msg);
                            }
                        }
                    } catch (IOException e) {
                        System.out.println("Disconnected from server.");
                        connected = false;
                    }
                });
                listenerThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void sendPosition(int playerIndex, float x, float y, float rot) {
        if (!connected || out == null) return;
        synchronized (out) {
            try {
                clientRegistry.send(out, new C_PlayerPosition(playerIndex, x, y, rot));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isConnected() { return connected; }

    public void stop() {
        connected = false;
        try {
            if (socket != null) socket.close();
        } catch (IOException e) { e.printStackTrace(); }
    }
}