package client.network;

import client.components.player.PlayerStateComponent;
import client.network.messages.Message;
import client.network.messages.client.C_PlayerState;
import client.network.messages.client.C_Shoot;
import client.network.messages.client.ClientRegistry;
import client.network.messages.server.ServerRegistry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GameClient {
    private final ClientRegistry clientRegistry = new ClientRegistry();
    private final ServerRegistry serverRegistry = new ServerRegistry();

    private Socket socket;
    private DataOutputStream out;
    private volatile boolean connected = false;

    private final Map<Class<?>, NetworkManager.MessageHandler<?>> handlers;
    private final ConcurrentLinkedQueue<Message> inQueue;

    public GameClient(Map<Class<?>, NetworkManager.MessageHandler<?>> handlers, ConcurrentLinkedQueue<Message> inQueue) {
        this.handlers = handlers;
        this.inQueue = inQueue;
    }

    @SuppressWarnings("unchecked")
    private <T> void invokeHandler(NetworkManager.MessageHandler<T> handler, Message msg) {
        handler.handle((T) msg);
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
                            var handler = handlers.get(msg.getClass());
                            if (handler == null) {
                                inQueue.offer(msg);
                            } else {
                                invokeHandler(handler, msg);
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

    public void sendPosition(float x, float y, float rot, PlayerStateComponent.State previous, PlayerStateComponent.State current, float mx, float my) {
        if (!connected || out == null) return;
        synchronized (out) {
            try {
                clientRegistry.send(out, new C_PlayerState(x, y, rot, previous, current, mx, my));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendBullet(int playerIndex, float px, float py, float angle) {
        if (!connected || out == null) return;
        synchronized (out) {
            try {
                clientRegistry.send(out, new C_Shoot(playerIndex, px, py, angle));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        connected = false;
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}