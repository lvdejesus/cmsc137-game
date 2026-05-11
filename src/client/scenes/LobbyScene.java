package client.scenes;

import client.components.TextComponent;
import client.components.TransformComponent;
import client.network.NetworkManager;
import client.rendering.Anchor;
import client.rendering.Font;
import client.systems.Context;
import client.systems.InputHandler;
import framework.engine.Engine;
import framework.engine.Entity;
import framework.engine.Window;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class LobbyScene implements Scene {
    private Engine<Context> engine;
    private Font font;
    private Entity<Context> statusTextEntity;
    private Entity<Context> ipTextEntity;
    private List<Entity<Context>> entities = new ArrayList<>();
    
    private boolean isHost;
    private String hostIP;

    public LobbyScene(boolean isHost, String hostIP) {
        this.isHost = isHost;
        this.hostIP = hostIP;
    }

    @Override
    public void init(Engine<Context> engine) {
        this.engine = engine;

        // Load Font
        try {
            byte[] bytes = Files.readAllBytes(Paths.get("res/fonts/KiwiSoda.ttf"));
            ByteBuffer buffer = BufferUtils.createByteBuffer(bytes.length);
            buffer.put(bytes);
            buffer.flip();
            font = new Font(buffer, 32);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Window window = Window.getWindow();
        float centerX = window.getWidth() / 2.0f;
        float centerY = window.getHeight() / 2.0f;

        // Status Text
        statusTextEntity = engine.createEntity();
        statusTextEntity.addComponent(new TransformComponent(new Vector2f(centerX, centerY - 50), new Vector2f(1, 1), Anchor.CENTER));
        statusTextEntity.addComponent(new TextComponent(font, "Waiting for players...", new Vector4f(1, 1, 1, 1), 1.0f, 0.5f));
        entities.add(statusTextEntity);

        // IP Text
        ipTextEntity = engine.createEntity();
        ipTextEntity.addComponent(new TransformComponent(new Vector2f(centerX, centerY + 50), new Vector2f(1, 1), Anchor.CENTER));
        ipTextEntity.addComponent(new TextComponent(font, "Host IP: " + hostIP, new Vector4f(1, 1, 1, 1), 1.0f, 0.4f));
        entities.add(ipTextEntity);

        if (isHost) {
            NetworkManager.getInstance().startHost();
        }

        // Disable game systems while waiting
        setGameSystemsEnabled(false);
    }

    private void setGameSystemsEnabled(boolean enabled) {
        if (engine == null) return;
        
        enableSystem(client.systems.EnemySystem.class, enabled);
        enableSystem(client.systems.BulletSystem.class, enabled);
        enableSystem(client.systems.MovementSystem.class, enabled);
        enableSystem(client.systems.PhysicsSystem.class, enabled);
        enableSystem(client.systems.player.PlayerRotationSystem.class, enabled);
        enableSystem(client.systems.DamageSystem.class, enabled);
    }

    private <T extends framework.engine.EntitySystem<Context>> void enableSystem(Class<T> type, boolean enabled) {
        T system = engine.getSystem(type);
        if (system != null) {
            system.setEnabled(enabled);
        }
    }

    private int lastCount = -1;

    @Override
    public void update() {
        NetworkManager nm = NetworkManager.getInstance();
        int count = nm.getPlayerCount();

        if (count != lastCount) {
            System.out.println("UI Update: Player count is now " + count + "/4 (isHost: " + isHost + ")");
            lastCount = count;
        }

        TextComponent tc = statusTextEntity.getComponent(TextComponent.class);
        if (tc != null) {
            tc.text = "Waiting for players (" + count + "/4)";
        }

        // Host triggers start for everyone
        if (isHost && count >= 4 && !nm.isGameStarted()) {
            nm.startGame();
        }

        // Both host and client transition when gameStarted is true
        if (nm.isGameStarted()) {
            SceneManager.setScene(new LevelScene(), engine);
        }

        if (InputHandler.getInstance().keyDown(GLFW_KEY_ESCAPE)) {
            nm.stop();
            SceneManager.setScene(new MenuScene(Window.getWindow()), engine);
        }
    }

    @Override
    public void clean() {
        for (Entity<Context> e : entities) {
            engine.destroyEntity(e.getId());
        }
    }
}
