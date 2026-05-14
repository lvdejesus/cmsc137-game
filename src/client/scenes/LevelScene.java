package client.scenes;

import client.components.*;
import client.components.player.MovementInputComponent;
import client.components.player.PlayerStateComponent;
import client.entities.*;
import client.network.NetworkManager;
import client.network.messages.Message;
import client.network.messages.server.*;
import client.rendering.*;
import client.systems.client.*;
import editor.components.TileGridComponent;
import editor.util.LevelManager;
import editor.util.TileRegistry;
import framework.engine.*;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.joml.primitives.AABBf;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static client.entities.Tile.placeTile;
import static org.lwjgl.glfw.GLFW.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import client.scenes.overlays.*;

public class LevelScene extends Scene {
    private Engine<Context> engine;
    private Player player;
    private Menu menu;
    private UpgradeOverlay upgrade;
    private TransformComponent playerTransform;
    private MovementInputComponent playerMovementInput;
    private PlayerStateComponent playerState;
    private MovementComponent playerMovement;

    private Font pauseFont;
    private ClientPrefabRegistry prefabRegistry = new ClientPrefabRegistry();

    private final Map<Integer, Integer> networkEntityMap = new ConcurrentHashMap<>();

    //Debug text
    private DebugText debugText;

    // Health bar
    private HealthBar healthBar;
    @Override
    public void init(Engine<Context> engine) {
        this.engine = engine;

        // Ensure systems are enabled
        setGameSystemsEnabled(true);

        int playerIndex = NetworkManager.getInstance().getPlayerIndex();
        this.player = new Player(engine, playerIndex, NetworkManager.getInstance().getNetworkId());
        this.player.spawn();

        this.playerTransform = player.getEntity().getComponent(TransformComponent.class);
        this.playerMovement = player.getEntity().getComponent(MovementComponent.class);
        this.playerMovementInput = player.getEntity().getComponent(MovementInputComponent.class);
        this.playerState = player.getEntity().getComponent(PlayerStateComponent.class);

        try {
            TileRegistry.loadTiles();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        TileGridComponent tgc = new TileGridComponent();
        tgc.tiles = TileRegistry.loadTileTextures();

        int[][] grid = LevelManager.loadGrid("room1.json");

        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
                int tileIndex = grid[y][x] - 1;
                if (tileIndex < 0 || tileIndex >= tgc.tiles.size()) {
                    continue;
                }

                Entity<Context> entity = placeTile(engine, tgc, x, y, tileIndex);
                entity.addComponent(new CollisionComponent(new AABBf((float) x, (float) y, 0.0f, x + 48.0f, y + 48.0f, 0.1f)));
                entity.addComponent(new WallComponent());
            }
        }

        // Load background map
//        Entity<Context> mapBg = engine.createEntity();
//        mapBg.addComponent(new TransformComponent(new Vector2f(400, 300), new Vector2f(800.0f / 1339.0f, 600.0f / 1175.0f), Anchor.CENTER));
//        mapBg.addComponent(new RenderComponent(TextureAtlas.get().getRegion("map_1.png"), -0.5f));

        // Load Font for pause menu
        try {
            ByteBuffer fontBuffer = loadResource("res/fonts/KiwiSoda.ttf");
            pauseFont = new Font(fontBuffer, 32); // Smaller font
            menu = new Menu(engine, pauseFont);
        } catch (IOException e) {
            e.printStackTrace();
        }

        upgrade = new UpgradeOverlay(engine);
        healthBar = new HealthBar(engine);
        healthBar.updateHealth(player.getHealth()); // Set initial health
        healthBar.createHealthBar();

        // Initialize debug text
        debugText = DebugText.create(engine, "State: idle");

    }

    private void setGameSystemsEnabled(boolean enabled) {
        // enableSystem(EnemySystem.class, enabled);
        enableSystem(BulletSystem.class, enabled);
        enableSystem(MovementSystem.class, enabled);
        enableSystem(client.systems.client.player.PlayerRotationSystem.class, enabled);
        enableSystem(DamageSystem.class, enabled);
        enableSystem(PhysicsSystem.class, enabled);
    }

    private <T extends EntitySystem<Context>> void enableSystem(Class<T> type, boolean enabled) {
        T system = engine.getSystem(type);
        if (system != null) {
            system.setEnabled(enabled);
        }
    }

    @Override
    public void update() {
        
        this.debugText.setText("State: " + player.getState());
        this.healthBar.updateHealth(player.getHealth());
        // Toggle menu with Esc
        InputHandler input = InputHandler.getInstance();
        if (input.keyDown(GLFW_KEY_ESCAPE)) {
            menu.toggleMenu();
        }

        if (menu.isVisible()) {
            menu.handlePauseMenuInput(input);
        }

        if (input.key(GLFW_KEY_V)){
            upgrade.splay();
        }
        upgrade.handleInput(input);

        NetworkManager nm = NetworkManager.getInstance();

        for (InputHandler.MouseEvent event : input.getEvents()) {
            if (event.type == InputHandler.MouseEventType.LEFT_CLICK && !event.consumed) {
                event.consume();

                if (playerTransform != null) {
                    Vector2f d = camera.toWorldPosition(event.position).sub(playerTransform.position);
                    float angle = (float) Math.toDegrees(Math.atan2(d.y, d.x));

                    // Get player's current velocity for velocity inheritance
                    float pvx = playerMovementInput.x * playerMovement.speed;
                    float pvy = playerMovementInput.y * playerMovement.speed;
                    nm.shoot(playerTransform.position.x, playerTransform.position.y, angle, pvx, pvy);
                }
            }
        }

        // 1. Broadcast our position
        if (playerTransform != null && playerState != null && playerMovementInput != null) {
            nm.broadcastPosition(playerTransform.position.x, playerTransform.position.y, playerTransform.rotation, playerState.previous, playerState.current, playerMovementInput.x, playerMovementInput.y);
        }

        Message msg;
        while ((msg = nm.inQueue.poll()) != null) {
            if (msg instanceof S_Spawn m) {
                // if self, skip
                if (nm.networkId == m.getNetworkId()) continue;
                Prefab prefab = prefabRegistry.spawn(engine, m.getPrefabId(), m.getNetworkId(), m.getBytes());
                networkEntityMap.put(m.getNetworkId(), prefab.getEntity().getId());
            } else if (msg instanceof S_Despawn m) {
                int entityId = networkEntityMap.remove(m.getNetworkId());
                engine.destroyEntity(entityId);
            } else if (msg instanceof S_Snapshot m) {
                for (var entitySnapshot : m.getEntitySnapshots()) {
                    Integer entityId = networkEntityMap.get(entitySnapshot.getNetworkId());
                    if (entitySnapshot.getNetworkId() == nm.networkId) continue;

                    if (entityId == null) {
                        continue;
                    }

                    for (var component : entitySnapshot.getComponents()) {
                        var cc = engine.getComponentClass(component.getComponentId());
                        var syncComponent = (SyncComponent) engine.getMapper(cc).get(entityId);
                        if (syncComponent == null) {
                            continue;
                        }
                        syncComponent.fromBytes(component.getData());
                    }
                }
            }
        }
    }

    private ByteBuffer loadResource(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        ByteBuffer buffer = BufferUtils.createByteBuffer(bytes.length);
        buffer.put(bytes);
        buffer.flip();
        return buffer;
    }

    @Override
    public void clean() {
        menu.hidePauseMenu();
    }
}