package client.scenes;

import client.components.TextComponent;
import client.components.TransformComponent;
import client.network.NetworkManager;
import client.network.messages.client.C_RequestStartGame;
import client.rendering.Anchor;
import client.rendering.Font;
import client.systems.client.*;
import framework.engine.*;
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

public class LobbyScene extends Scene {
    private Engine<Context> engine;
    private Font font;
    private Entity<Context> statusTextEntity;
    private Entity<Context> ipTextEntity;
    private Entity<Context> startButtonEntity;
    private final List<Entity<Context>> entities = new ArrayList<>();
    
    private final String hostIP;
    private boolean isHost;

    public LobbyScene(String hostIP) {
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
        statusTextEntity.addComponent(new TextComponent(font, "Waiting for players...", new Vector4f(1, 1, 1, 1), 1.0f, 0.5f, "fixed"));
        entities.add(statusTextEntity);

        // IP Text
        ipTextEntity = engine.createEntity();
        ipTextEntity.addComponent(new TransformComponent(new Vector2f(centerX, centerY + 50), new Vector2f(1, 1), Anchor.CENTER));
        ipTextEntity.addComponent(new TextComponent(font, "Host IP: " + hostIP, new Vector4f(1, 1, 1, 1), 1.0f, 0.4f, "fixed"));
        entities.add(ipTextEntity);

        // Start Button (only visible to host)
        startButtonEntity = engine.createEntity();
        startButtonEntity.addComponent(new TransformComponent(new Vector2f(centerX, centerY + 120), new Vector2f(1, 1), Anchor.CENTER));
        startButtonEntity.addComponent(new TextComponent(font, "Start Game", new Vector4f(0, 1, 0, 1), 1.0f, 0.5f, "fixed"));
        entities.add(startButtonEntity);
    }

    private int lastCount = -1;

    @Override
    public void update() {
        NetworkManager nm = NetworkManager.getInstance();
        int count = nm.getPlayerCount();
        isHost = nm.isHost();

        if (count != lastCount) {
            System.out.println("UI Update: Player count is now " + count + "/4");
            lastCount = count;
        }

        TextComponent tc = statusTextEntity.getComponent(TextComponent.class);
        if (tc != null) {
            tc.text = "Waiting for players (" + count + ")";
        }

        // Show/hide start button based on host status
        TextComponent buttonText = startButtonEntity.getComponent(TextComponent.class);
        if (buttonText != null) {
            buttonText.text = isHost ? "Start Game (Press ENTER)" : "";
        }

        // Handle start game for host
        if (isHost && InputHandler.getInstance().keyDown(GLFW_KEY_ENTER)) {
            nm.sendMessage(C_RequestStartGame.INSTANCE);
        }

        // Transition to LevelScene when game starts
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
        engine.clearEntities();
    }
}
