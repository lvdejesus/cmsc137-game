package client.scenes;

import client.components.*;
import client.components.player.PlayerStateComponent;
import client.entities.*;
import client.network.NetworkManager;
import client.network.messages.Message;
import client.network.messages.client.C_PlayerState;
import client.network.messages.client.C_Shoot;
import client.network.messages.server.*;
import client.rendering.*;
import client.systems.client.*;
import framework.engine.*;
import org.joml.Vector2f;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

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
    private MovementComponent playerMovement;
    private PlayerStateComponent playerState;

    private Font pauseFont;
    private ClientPrefabRegistry prefabRegistry = new ClientPrefabRegistry();

    private final Map<Integer, Prefab> networkPrefabMap = new ConcurrentHashMap<>();

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
        this.player.spawnClient();

        this.playerTransform = player.getEntity().getComponent(TransformComponent.class);
        this.playerMovement = player.getEntity().getComponent(MovementComponent.class);
        this.playerState = player.getEntity().getComponent(PlayerStateComponent.class);

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

        if (input.keyDown(GLFW_KEY_V)){
            upgrade.splay();
        }
        upgrade.handleInput(input);

        NetworkManager nm = NetworkManager.getInstance();

        Message msg;
        while ((msg = nm.inQueue.poll()) != null) {
            if (msg instanceof S_Spawn m) {
                // if self, skip
                if (nm.networkId == m.getNetworkId()) {
                    networkPrefabMap.put(m.getNetworkId(), player);
                    continue;
                }

                Prefab prefab = prefabRegistry.spawn(engine, m.getPrefabId(), m.getNetworkId(), m.getBytes());
                networkPrefabMap.put(m.getNetworkId(), prefab);
            } else if (msg instanceof S_Despawn m) {
                Prefab prefab = networkPrefabMap.remove(m.getNetworkId());
                if (prefab.onDespawn()) {
                    engine.destroyEntity(prefab.getEntity().getId());
                }
            } else if (msg instanceof S_Snapshot m) {
                for (var entitySnapshot : m.getEntitySnapshots()) {
                    Prefab entity = networkPrefabMap.get(entitySnapshot.getNetworkId());
                    if (entity == null) {
                        continue;
                    }

                    var keySet = engine.getMapper(NetworkDuplicateComponent.class).get(entity.getEntity().getId()).components.keySet();
                    for (var component : entitySnapshot.getComponents()) {
                        var cc = engine.getComponentClass(component.getComponentId());
                        if (!keySet.contains(cc)) continue;

                        var syncComponent = (SyncComponent) engine.getMapper(cc).get(entity.getEntity().getId());
                        if (syncComponent == null) continue;

                        syncComponent.fromBytes(component.getData());
                    }
                }
            }
        }

        for (InputHandler.MouseEvent event : input.getEvents()) {
            if (event.type == InputHandler.MouseEventType.LEFT_CLICK && !event.consumed) {
                event.consume();

                if (playerTransform != null) {
                    Vector2f d = camera.toWorldPosition(event.position);

                    // Get player's current velocity for velocity inheritance
                    float pvx = playerMovement.velocity.x;
                    float pvy = playerMovement.velocity.y;
                    nm.sendMessage(new C_Shoot(d.x, d.y, pvx, pvy));
                }
            }
        }

        nm.sendMessage(new C_PlayerState(playerTransform.position.x, playerTransform.position.y, playerTransform.rotation, playerState.previous, playerState.current, playerMovement.velocity.x, playerMovement.velocity.y));
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