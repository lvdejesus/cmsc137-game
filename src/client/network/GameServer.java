package client.network;

import client.components.NetworkIdComponent;
import client.network.messages.client.*;
import client.systems.server.MapEnemySpawnSystem;
import client.systems.server.MapKeySpawnSystem;
import client.systems.server.KeyPickupSystem;
import common.MapGenerator;
import client.components.TransformComponent;
import client.components.player.MovementInputComponent;
import client.components.player.PlayerStateComponent;
import client.entities.*;
import client.network.messages.Message;
import client.network.messages.server.*;
import client.systems.client.Context;
import client.systems.server.ServerNetworkInputSystem;
import client.systems.server.ServerNetworkOutputSystem;
import client.systems.server.SnapshotSystem;
import client.util.EngineConfig;
import common.RoomLoader;
import common.TileLoader;
import framework.engine.ComponentMapper;
import framework.engine.Engine;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GameServer implements Runnable {
    public static final int TCP_PORT = 7650;

    private final ClientRegistry clientRegistry = new ClientRegistry();
    private final ServerRegistry serverRegistry = new ServerRegistry();
    private final PrefabRegistry prefabRegistry = new ClientPrefabRegistry();

    private final DiscoveryService discoveryService = new DiscoveryService();

    private int lastId = 1;
    private final Map<Integer, ClientConnection> connectedClients = new ConcurrentHashMap<>();

    private ServerSocket serverSocket;
    private volatile boolean gameStarted = false;

    private String localIP;

    private final ConcurrentLinkedQueue<MessagePair> inQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<MessagePair> outQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<EntitySnapshot> snapshotQueue = new ConcurrentLinkedQueue<>();

    NetworkSpawnManager nsm;
    Map<Integer, Integer> playerToEntityMap;

    private static final double NANO_TO_SECOND = 1_000_000_000.0;

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

    private void broadcastDisconnect(int networkId) {
        for (ClientConnection client : connectedClients.values()) {
            client.send(new S_Disconnect(networkId));
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
            serverSocket.setSoTimeout(100);
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

        playerToEntityMap = new HashMap<>();

        nsm = new NetworkSpawnManager(engine, prefabRegistry, outQueue);

        while (!serverSocket.isClosed() && !gameStarted) {
            try {
                Socket socket = serverSocket.accept();
                int playerId = lastId++;

                NetworkSpawnManager.SpawnResult res = nsm.spawn(RemotePlayer.class, RemotePlayer.serialize(playerId));
                int networkId = res.networkId;

                ClientConnection client = new ClientConnection(inQueue, socket, playerId, networkId);

                connectedClients.put(lastId, client);
                broadcastPlayerCount();

                playerToEntityMap.put(playerId, res.prefab.getEntity().getId());

                System.out.println("Client connected: " + socket.getInetAddress() + " assigned ID: " + playerId);
            } catch (java.net.SocketTimeoutException e) {
                MessagePair msg = inQueue.poll();
                if (msg != null && msg.getMessage() instanceof C_RequestStartGame && msg.getPlayerId() == 1) {
                    startGame();
                }
            } catch (IOException e) {
                if (!serverSocket.isClosed()) e.printStackTrace();
            }
        }

        Map<Class<? extends Message>, ServerNetworkInputSystem.MessageHandler> handlers = new HashMap<>();
        final ComponentMapper<TransformComponent> tm = engine.getMapper(TransformComponent.class);
        final ComponentMapper<MovementInputComponent> mim = engine.getMapper(MovementInputComponent.class);
        final ComponentMapper<PlayerStateComponent> sm = engine.getMapper(PlayerStateComponent.class);

        handlers.put(C_PlayerState.class, (id, message) -> {
            if (!(message instanceof C_PlayerState pp)) return;

            int entityId = playerToEntityMap.get(id);

            TransformComponent tc = tm.get(entityId);

            tc.position.x = pp.getX();
            tc.position.y = pp.getY();
            tc.rotation = pp.getRotation();

            MovementInputComponent mic = mim.get(entityId);

            mic.x = pp.getMx();
            mic.y = pp.getMy();

            PlayerStateComponent sc = sm.get(entityId);

            sc.previous = pp.getPrevious();
            sc.current = pp.getCurrent();
        });

        handlers.put(C_Shoot.class, (id, message) -> {
            if (!(message instanceof C_Shoot pp)) return;

            int entityId = playerToEntityMap.get(id);
            TransformComponent tc = tm.get(entityId);

            nsm.spawn(Bullet.class, Bullet.serialize(tc.position.x, tc.position.y, pp.getPx(), pp.getPy(), pp.getPvx(), pp.getPvy(), false));
        });

        handlers.put(C_RequestStartGame.class, (id, message) -> {
            if (id == 1 && !gameStarted) {
                startGame();
            }
        });

        handlers.put(C_Disconnect.class, (id, message) -> {
            int entityId = playerToEntityMap.get(id);
            nsm.despawn(entityId);
        });

        MapGenerator.MapResult grid;
        try {
            TileLoader.loadTiles();
            grid = MapGenerator.generateMap(System.nanoTime());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        engine.addSystem(new ServerNetworkInputSystem(inQueue, handlers));

        EngineConfig.addServerSystems(engine, nsm);

        engine.addSystem(new MapEnemySpawnSystem(grid, nsm));
        engine.addSystem(new MapKeySpawnSystem(grid, nsm));
        engine.addSystem(new KeyPickupSystem(nsm));
        engine.addSystem(new SnapshotSystem(snapshotQueue, outQueue));
        engine.addSystem(new ServerNetworkOutputSystem(outQueue, connectedClients));

        double lastTime = System.nanoTime() / NANO_TO_SECOND;
        Context ctx = new Context();

        for (int y = 0; y < grid.grid.length; y++) {
            for (int x = 0; x < grid.grid[y].length; x++) {
                int tileIndex = grid.grid[y][x] - 1;
                if (tileIndex < 0) {
                    continue;
                }

                nsm.spawn(TilePrefab.class, TilePrefab.serialize(x, y, tileIndex));
            }
        }

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
        int networkId;

        ClientConnection(ConcurrentLinkedQueue<MessagePair> queue, Socket socket, int playerId, int networkId) throws IOException {
            this.socket = socket;
            this.playerId = playerId;
            this.networkId = networkId;
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
            connectedClients.remove(this.playerId);
            broadcastPlayerCount();
            broadcastDisconnect(this.networkId);
            inQueue.add(new MessagePair(this.playerId, new C_Disconnect()));
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