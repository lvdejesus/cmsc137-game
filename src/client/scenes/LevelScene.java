package client.scenes;

import client.components.*;
import client.components.player.PlayerTagComponent;
import client.entities.*;
import client.network.NetworkManager;
import client.network.messages.Message;
import client.rendering.*;
import client.systems.client.*;
import client.systems.client.player.PlayerRotationSystem;
import client.systems.client.player.PlayerTiltSystem;
import framework.engine.*;


import static org.lwjgl.glfw.GLFW.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import client.scenes.overlays.*;

public class LevelScene extends Scene {
    private Engine<Context> engine;
    private UpgradeNotification upgradeNotification;
    private Player player;
    private Menu menu;
    private UpgradeOverlay upgrade;
    private GameResultOverlay gameResultOverlay;

    private Font pauseFont;
    private double gameOverTime = -1;
    private static final double GAME_OVER_DELAY = 3.0;

    //Debug text
    private DebugText debugText;

    // Health bar
    private HealthBar healthBar;

    @Override
    public void init(Engine<Context> engine) {
        this.engine = engine;
        int playerIndex = NetworkManager.getInstance().getPlayerIndex();
        this.player = new Player(engine, playerIndex, NetworkManager.getInstance().getNetworkId());
        this.player.spawnClient();

        // Load Font for pause menu
        try {
            pauseFont = Font.load("res/fonts/KiwiSoda.ttf", 32);
            menu = new Menu(engine, pauseFont);
        } catch (Exception e) {
            e.printStackTrace();
        }

        upgrade = new UpgradeOverlay(engine, player);
        upgradeNotification = new UpgradeNotification(engine);
        gameResultOverlay = new GameResultOverlay(engine, pauseFont);
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

        engine.addSystem(new ClientNetworkInputSystem(NetworkManager.getInstance().inQueue, networkPrefabMap, player));

        engine.addSystem(new DeathSystem());
        engine.addSystem(new PlayerRotationSystem(camera));
        engine.addSystem(new PlayerShootSystem(outQueue, camera));
        engine.addSystem(new PlayerUpdateSystem(outQueue));
        engine.addSystem(new PlayerUpgradeUpdateSystem(outQueue));
        engine.addSystem(new MovementInputSystem());
        engine.addSystem(new PlayerTiltSystem());
        engine.addSystem(new MovementSystem());
        engine.addSystem(new WallTileCollisionSystem());
        engine.addSystem(new PlayerFollowSystem(camera));
        engine.addSystem(new DespawnSystem());
        engine.addSystem(new HealthBarUpdateSystem(healthBar));
        engine.addSystem(new KeyUISystem());
        engine.addSystem(new UpgradeUISystem());
        engine.addSystem(new TimerSystem());
        engine.addSystem(new ClientNetworkOutputSystem(outQueue));
    }

    @Override
    public void clearSystems(Engine<Context> engine) {
        engine.removeSystems(0);
    }

    @Override
    public void update() {
        var pos = player.getEntity().getComponent(TransformComponent.class).position;
        this.debugText.setText(String.format("Position: %.2f,  %.2f", pos.x / 64.0f, pos.y / 64.0f));

        // Toggle menu with Esc
        InputHandler input = InputHandler.getInstance();
        if (input.keyDown(GLFW_KEY_ESCAPE)) {
            menu.toggleMenu();
        }

        if (menu.isVisible()) {
            if (menu.handlePauseMenuInput(input)) {
                return;
            }
        }

        if (NetworkManager.getInstance().isBossDefeated()) {
            engine.removeSystems(0);
            if (!gameResultOverlay.isVisible()) {
                gameResultOverlay.show(true);
            }
            gameResultOverlay.handleInput(input);
            return;
        }

        if (NetworkManager.getInstance().isGameOver()) {
            if (gameOverTime < 0) {
                gameOverTime = glfwGetTime();
                engine.removeSystems(0);
            }
            if (glfwGetTime() - gameOverTime < GAME_OVER_DELAY) return;
            if (!gameResultOverlay.isVisible()) {
                gameResultOverlay.show(false);
            }
            gameResultOverlay.handleInput(input);
            return; 
        }

        var xpc = player.getEntity().getComponent(ExperienceComponent.class);
        if (xpc.getRemainingUpgrades() > 0){
            if (xpc.lastLevelNotifShown != xpc.getLevels()){
                upgradeNotification.toggle();
                xpc.lastLevelNotifShown = xpc.getLevels();
            }
        }

        if (input.keyDown(GLFW_KEY_V) && xpc.getRemainingUpgrades() > 0 && !upgrade.visible) {
            upgrade.splay();
        }
        upgrade.handleInput(input);
    }

    @Override
    public void clean() {
        menu.hidePauseMenu();
    }
}