package client.network;

import client.components.NetworkIdComponent;
import client.components.TransformComponent;
import client.entities.Bullet;
import client.entities.ClientPrefabRegistry;
import client.entities.PrefabRegistry;
import client.entities.RemotePlayer;
import client.network.messages.Message;
import client.network.messages.client.C_PlayerPosition;
import client.network.messages.client.C_Shoot;
import client.network.messages.client.ClientRegistry;
import client.network.messages.server.*;
import client.systems.client.Context;
import client.systems.server.ServerNetworkInputSystem;
import client.systems.server.ServerNetworkOutputSystem;
import client.systems.server.SnapshotSystem;
import client.util.EngineConfig;
import framework.engine.ComponentMapper;
import framework.engine.Engine;
import org.joml.Vector2f;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class GameServer implements Runnable {
    private static final int TCP_PORT = 12345;

    private final ClientRegistry clientRegistry = new ClientRegistry();
    private final ServerRegistry serverRegistry = new ServerRegistry();
    private final PrefabRegistry prefabRegistry = new ClientPrefabRegistry();

    private final DiscoveryService discoveryService = new DiscoveryService();

    private int lastId = 1;
    private final Map<Integer, ClientConnection> connectedClients = new ConcurrentHashMap<>();

    private ServerSocket serverSocket;
    private volatile boolean gameStarted = false;

    private static final int NUM_PLAYERS = 2;
    private String localIP;

    private final ConcurrentLinkedQueue<MessagePair> inQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<MessagePair> outQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<EntitySnapshot> snapshotQueue = new ConcurrentLinkedQueue<>();

    private static final double NANO_TO_SECOND = 1_000_000_000.0;

    private AtomicInteger networkId = new AtomicInteger(0);

    public GameServer(String localIP) {
        this.localIP = localIP;
        this.gameStarted = false;
    }

    public void startGame() {
        if (gameStarted) return;

        gameStarted = true;
        for (ClientConnection client : connectedClients.values()) {
            client.send(new S_StartGame());
        }
    }

    private void broadcastPlayerCount() {
        int count = connectedClients.size();
        for (ClientConnection client : connectedClients.values()) {
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
            for (ClientConnection c : connectedClients.values()) c.close();
            connectedClients.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(TCP_PORT));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Server failed to start.");
            return;
        }

        System.out.println("Server started on IP: " + localIP);
        discoveryService.startResponding(localIP);

        Engine<Context> engine = new Engine<>();
        EngineConfig.registerSyncComponents(engine);

        Map<Integer, Integer> playerToEntityMap = new HashMap<>();
        Map<Integer, Integer> playerToNetworkMap = new HashMap<>();

        while (!serverSocket.isClosed() && !gameStarted) {
            try {
                Socket socket = serverSocket.accept();
                int playerId = lastId++;

                ClientConnection client = new ClientConnection(inQueue, socket, playerId, networkId.get());
                playerToNetworkMap.put(playerId, networkId.get());

                connectedClients.put(lastId, client);
                broadcastPlayerCount();

                var entity = engine.createEntity();
                entity.addComponent(new TransformComponent(new Vector2f(200.0f, 200.0f)));
                entity.addComponent(new NetworkIdComponent(networkId.get()));

                playerToEntityMap.put(playerId, entity.getId());

                checkStartGame();
                System.out.println("Client connected: " + socket.getInetAddress() + " assigned ID: " + playerId);

                int prefabId = prefabRegistry.get(RemotePlayer.class);
                Message msg = new S_Spawn(prefabId, networkId.get(), RemotePlayer.serialize(playerId));
                outQueue.add(new MessagePair(-1, msg));

                networkId.incrementAndGet();
            } catch (IOException e) {
                if (!serverSocket.isClosed()) e.printStackTrace();
            }
        }

        Map<Class<? extends Message>, ServerNetworkInputSystem.MessageHandler> handlers = new HashMap<>();
        final ComponentMapper<TransformComponent> tm = engine.getMapper(TransformComponent.class);

        handlers.put(C_PlayerPosition.class, (id, message) -> {
            if (!(message instanceof C_PlayerPosition pp)) return;

            int entityId = playerToEntityMap.get(id);
            TransformComponent tc = tm.get(entityId);

            tc.position.x = pp.getX();
            tc.position.y = pp.getY();
            tc.rotation = pp.getRotation();
        });

        handlers.put(C_Shoot.class, (id, message) -> {
            if (!(message instanceof C_Shoot pp)) return;

            int prefabId = prefabRegistry.get(Bullet.class);
            Message msg = new S_Spawn(prefabId, networkId.getAndIncrement(), Bullet.serialize(pp.getPx(), pp.getPy(), pp.getAngle(), false));
            outQueue.add(new MessagePair(-1, msg));
        });

        engine.addSystem(new ServerNetworkInputSystem(inQueue, handlers));
        engine.addSystem(new SnapshotSystem(snapshotQueue, outQueue));
        engine.addSystem(new ServerNetworkOutputSystem(outQueue, connectedClients));

        double lastTime = System.nanoTime() / NANO_TO_SECOND;
        Context ctx = new Context();

        while (true) {
            double currentTime = System.nanoTime() / NANO_TO_SECOND;
            float dt = (float) (currentTime - lastTime);
            lastTime = currentTime;

            ctx.currentTime = (float) currentTime;
            ctx.deltaTime = dt;

            engine.update(ctx);
        }
    }

    public class ClientConnection {
        Socket socket;
        DataOutputStream out;
        int playerId;

        ClientConnection(ConcurrentLinkedQueue<MessagePair> queue, Socket socket, int playerId, int networkId) throws IOException {
            this.socket = socket;
            this.playerId = playerId;
            this.out = new DataOutputStream(socket.getOutputStream());
            send(new S_AssignId(playerId, networkId));

            new Thread(() -> {
                try {
                    DataInputStream in = new DataInputStream(socket.getInputStream());
                    while (!socket.isClosed()) {
                        Message msg = clientRegistry.receive(in);
                        queue.offer(new MessagePair(playerId, msg));
                    }
                } catch (IOException e) {
                    System.out.println("Client " + playerId + " disconnected.");
                    handleDisconnect();
                }
            }).start();
        }

        private void handleDisconnect() {
            connectedClients.remove(this);
            broadcastPlayerCount();
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }

        public void send(Message msg) {
            synchronized (out) {
                try {
                    serverRegistry.send(out, msg);
                } catch (IOException e) {
                    handleDisconnect();
                }
            }
        }

        void close() {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }
}