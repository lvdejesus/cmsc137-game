package client.network;

import client.components.ExperienceComponent;
import client.components.HealthComponent;
import client.components.MovementComponent;
import client.components.PlayerUpgradeComponent;
import client.network.messages.client.*;
import client.systems.client.*;
import client.systems.server.*;
import client.util.Statistics;
import common.MapGenerator;
import client.components.TransformComponent;
import client.components.player.MovementInputComponent;
import client.components.player.PlayerStateComponent;
import client.entities.*;
import client.network.messages.Message;
import client.network.messages.server.*;
import client.util.EngineConfig;
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

import static client.components.PlayerUpgradeComponent.BASE_BULLET_DISTANCE;

public class GameServer implements Runnable {
    public static final int TCP_PORT = 7650;

    private final ClientRegistry clientRegistry = new ClientRegistry();
    private final ServerRegistry serverRegistry = new ServerRegistry();
    private final PrefabRegistry prefabRegistry = new ClientPrefabRegistry();

    private final DiscoveryService discoveryService = new DiscoveryService();

    public static volatile GameServer hostServer;

    private int lastId = 1;
    private final Map<Integer, ClientConnection> connectedClients = new ConcurrentHashMap<>();

    private ServerSocket serverSocket;
    private volatile boolean gameStarted = false;
    private volatile boolean running = true;

    private String localIP;

    private final ConcurrentLinkedQueue<MessagePair> inQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<MessagePair> outQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<EntitySnapshot> snapshotQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<Integer> disconnectQueue = new ConcurrentLinkedQueue<>();

    NetworkSpawnManager nsm;
    Map<Integer, Integer> playerToEntityMap;

    private static final double NANO_TO_SECOND = 1_000_000_000.0;
    private static final long TICK_INTERVAL_NS = 20_000_000L; // 50Hz = 20ms per tick

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
        running = false;
        discoveryService.stop();
        try {
            if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
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
            int retries = 0;
            while (true) {
                try {
                    serverSocket.bind(new InetSocketAddress(TCP_PORT));
                    break;
                } catch (IOException e) {
                    retries++;
                    if (retries >= 50) throw e;
                    Thread.sleep(100);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Server failed to start.");
            return;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        System.out.println("Server started on IP: " + localIP);
        discoveryService.startResponding(localIP);

        while (!serverSocket.isClosed()) {
            gameStarted = false;
            running = true;
            lastId = 1;

            for (var client : connectedClients.values()) client.close();
            connectedClients.clear();
            disconnectQueue.clear();
            outQueue.clear();
            inQueue.clear();

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

                    connectedClients.put(playerId, client);
                    broadcastPlayerCount();

                    playerToEntityMap.put(playerId, res.prefab.getEntity().getId());

                    System.out.println("Client connected: " + socket.getInetAddress() + " assigned ID: " + playerId);
                } catch (java.net.SocketTimeoutException e) {
                    MessagePair msg = inQueue.poll();
                    if (msg != null && msg.getMessage() instanceof C_RequestStartGame && msg.getPlayerId() == 1) {
                        startGame();
                    }

                    Integer disconnectedId;
                    while ((disconnectedId = disconnectQueue.poll()) != null) {
                        Integer entityId = playerToEntityMap.remove(disconnectedId);
                        if (entityId != null) nsm.despawn(entityId);
                    }
                } catch (IOException e) {
                    if (!serverSocket.isClosed()) e.printStackTrace();
                }
            }

            if (serverSocket.isClosed()) break;

            Map<Class<? extends Message>, ServerNetworkInputSystem.MessageHandler> handlers = new HashMap<>();
            final ComponentMapper<TransformComponent> tm = engine.getMapper(TransformComponent.class);
            final ComponentMapper<MovementInputComponent> mim = engine.getMapper(MovementInputComponent.class);
            final ComponentMapper<MovementComponent> mm = engine.getMapper(MovementComponent.class);
            final ComponentMapper<PlayerStateComponent> sm = engine.getMapper(PlayerStateComponent.class);

            handlers.put(C_PlayerState.class, (id, message) -> {
                if (!(message instanceof C_PlayerState pp)) return;

                Integer entityId = playerToEntityMap.get(id);
                if (entityId == null) return;

                TransformComponent tc = tm.get(entityId);

                tc.position.x = pp.x;
                tc.position.y = pp.y;
                tc.rotation = pp.rotation;

                MovementInputComponent mic = mim.get(entityId);

                mic.x = pp.mx;
                mic.y = pp.my;

                MovementComponent mc = mm.get(entityId);

                mc.velocity.x = pp.vx;
                mc.velocity.y = pp.vy;

                PlayerStateComponent sc = sm.get(entityId);

                sc.previous = pp.previous;
                sc.current = pp.current;
            });

            handlers.put(C_ApplyUpgrade.class, (id, message) -> {
                if (!(message instanceof C_ApplyUpgrade pp)) return;

                Integer entityId = playerToEntityMap.get(id);
                if (entityId == null) return;
                PlayerUpgradeComponent puc = engine.getMapper(PlayerUpgradeComponent.class).get(entityId);
                ExperienceComponent xpc = engine.getMapper(ExperienceComponent.class).get(entityId);
                puc.applyActual(pp.index);
                xpc.usedLevels++;
            });

            handlers.put(C_Shoot.class, (id, message) -> {
                if (!(message instanceof C_Shoot pp)) return;

                Integer entityId = playerToEntityMap.get(id);
                if (entityId == null) return;
                TransformComponent tc = tm.get(entityId);
                PlayerUpgradeComponent puc = engine.getMapper(PlayerUpgradeComponent.class).get(entityId);

                nsm.spawn(Bullet.class, Bullet.serialize(tc.position.x, tc.position.y, pp.getPx(), pp.getPy(), pp.getPvx(), pp.getPvy(), puc.bulletSpeed, id, puc.splatter, BASE_BULLET_DISTANCE / puc.bulletSpeed));
            });

        handlers.put(C_RequestStartGame.class, (id, message) -> {
            if (id == 1 && !gameStarted) {
                startGame();
            }
        });

        handlers.put(C_ChatMessage.class, (id, message) -> {
            var chatMsg = (C_ChatMessage) message;
            var broadcast = new S_ChatMessage(id, chatMsg.getText());
            outQueue.add(new MessagePair(-1, broadcast));
        });



        MapGenerator.MapResult grid;
        try {
            TileLoader.loadTiles();
            grid = MapGenerator.generateMap(System.nanoTime(), 8 + 6 * connectedClients.size(), 13 + 8 * connectedClients.size());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

            Statistics statistics = new Statistics();

            engine.addSystem(new ServerNetworkInputSystem(inQueue, handlers));
            engine.addSystem(new EnemySystem(nsm));
            engine.addSystem(new BossSystem(nsm));
            engine.addSystem(new BulletSystem(nsm));
            engine.addSystem(new DamageSystem(nsm, playerToEntityMap, statistics));
            engine.addSystem(new BulletWallTileCollisionSystem(nsm));
            engine.addSystem(new WallTileCollisionSystem());
            engine.addSystem(new MapEnemySpawnSystem(grid, nsm));
            engine.addSystem(new MapKeySpawnSystem(grid, nsm));
            engine.addSystem(new KeyPickupSystem(nsm));
            engine.addSystem(new HealthPickupSystem(nsm));
            engine.addSystem(new DoorSystem(grid, nsm));
            engine.addSystem(new BossDoorSystem(nsm));
            engine.addSystem(new ExperienceSystem());
            engine.addSystem(new SnapshotSystem(snapshotQueue, outQueue));
            engine.addSystem(new ServerNetworkOutputSystem(outQueue, connectedClients));

            double lastTime = System.nanoTime() / NANO_TO_SECOND;
            Context ctx = new Context();
            boolean gameEnded = false;
            var healthMapper = engine.getMapper(HealthComponent.class);

            Set<Long> bossDoorSet = new HashSet<>();

            var bossRoom = grid.bossRoom;
            var bossDoors = bossRoom.room.doorBlocks.getFirst();
            int bx = bossDoors.originX;
            int by = bossDoors.originY;
            for (var bossDoor : bossDoors.tiles) {
                int x = bossRoom.offsetX + bossDoors.originX + bossDoor.dx;
                int y = bossRoom.offsetY + bossDoors.originY + bossDoor.dy;
                long hash = ((long) x << 32) | (y & 0xFFFFFFFFL);
                bossDoorSet.add(hash);
            }

            for (int y = 0; y < grid.grid.length; y++) {
                for (int x = 0; x < grid.grid[y].length; x++) {
                    long hash = ((long) x << 32) | (y & 0xFFFFFFFFL);
                    boolean isBoss = bossDoorSet.contains(hash);

                    int tileIndex = grid.grid[y][x] - 1;
                    if (tileIndex < 0) {
                        continue;
                    }

                    boolean isDoor = tileIndex >= 1 && tileIndex <= 4;

                    if (isBoss) {
                        nsm.spawn(TilePrefab.class, TilePrefab.serialize(x, y, tileIndex, bx, by));
                    } else {
                        nsm.spawn(TilePrefab.class, TilePrefab.serialize(x, y, tileIndex));
                    }

                    if (isDoor) {
                        nsm.spawn(TilePrefab.class, TilePrefab.serialize(x, y, 5));
                    }
                }
            }

            while (running && !gameEnded) {
                long tickStart = System.nanoTime();

                double currentTime = tickStart / NANO_TO_SECOND;
                float dt = (float) (currentTime - lastTime);
                lastTime = currentTime;

                ctx.currentTime = (float) currentTime;
                ctx.deltaTime = dt;

                Integer disconnectedId;
                while ((disconnectedId = disconnectQueue.poll()) != null) {
                    Integer entityId = playerToEntityMap.remove(disconnectedId);
                    if (entityId != null) nsm.despawn(entityId);
                    if (disconnectedId == 1) {
                        gameEnded = true;
                    }
                }

                if (gameEnded) break;

                engine.update(ctx);

                if (!gameEnded) {
                    boolean victory = statistics.bossKills > 0;
                    boolean allDead = true;
                    for (int entityId : playerToEntityMap.values()) {
                        HealthComponent hc = healthMapper.get(entityId);
                        if (hc != null && hc.isAlive()) {
                            allDead = false;
                            break;
                        }
                    }
                    if (victory || (allDead && !playerToEntityMap.isEmpty())) {
                        gameEnded = true;
                    }
                }

                long elapsed = System.nanoTime() - tickStart;
                long sleepNs = TICK_INTERVAL_NS - elapsed;
                if (sleepNs > 0) {
                    try {
                        Thread.sleep(sleepNs / 1_000_000, (int) (sleepNs % 1_000_000));
                    } catch (InterruptedException e) {
                        running = false;
                    }
                }
            }

            if (gameEnded) {
                S_GameResult result = new S_GameResult(statistics, statistics.bossKills > 0);
                for (var client : connectedClients.values()) {
                    client.send(result);
                }
            }
        }
        stop();
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
            disconnectQueue.offer(this.playerId);
            try { socket.close(); } catch (IOException ignored) {}
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