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
import client.systems.client.player.PlayerRotationSystem;
import client.systems.client.player.PlayerTiltSystem;
import framework.engine.*;
import org.joml.Vector2f;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.glfw.GLFW.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import client.scenes.overlays.*;

public class LevelScene extends Scene {
    private Player player;
    private Menu menu;
    private UpgradeOverlay upgrade;

    private Font pauseFont;

    //Debug text
    private DebugText debugText;

    // Health bar
    private HealthBar healthBar;

    @Override
    public void init(Engine<Context> engine) {
        int playerIndex = NetworkManager.getInstance().getPlayerIndex();
        this.player = new Player(engine, playerIndex, NetworkManager.getInstance().getNetworkId());
        this.player.spawnClient();

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

    @Override
    public void addSystems(Engine<Context> engine) {
        Map<Integer, Prefab> networkPrefabMap = new ConcurrentHashMap<>();

        ConcurrentLinkedQueue<Message> spawnQueue = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<Message> snapshotQueue = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<Message> outQueue = new ConcurrentLinkedQueue<>();

        engine.addSystem(new ClientNetworkInputSystem(NetworkManager.getInstance().inQueue, spawnQueue, snapshotQueue));
        engine.addSystem(new ClientSpawnSystem(spawnQueue, networkPrefabMap, player));
        engine.addSystem(new ClientSnapshotSystem(snapshotQueue, networkPrefabMap));

        engine.addSystem(new PhysicsSystem());
        engine.addSystem(new PlayerRotationSystem(camera));
        engine.addSystem(new PlayerShootSystem(outQueue, camera));
        engine.addSystem(new PlayerUpdateSystem(outQueue));
        engine.addSystem(new MovementInputSystem());
        engine.addSystem(new MovementSystem());
        engine.addSystem(new WallTileCollisionSystem());
        engine.addSystem(new PlayerTiltSystem());
        engine.addSystem(new PlayerFollowSystem(camera));
        engine.addSystem(new DespawnSystem());

        engine.addSystem(new ClientNetworkOutputSystem(outQueue));
    }

    @Override
    public void clearSystems(Engine<Context> engine) {
        engine.removeSystems(0);
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

        if (input.keyDown(GLFW_KEY_V)) {
            upgrade.splay();
        }
        upgrade.handleInput(input);
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