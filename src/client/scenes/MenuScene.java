package client.scenes;

import client.components.RenderComponent;
import client.components.TextComponent;
import client.components.TransformComponent;
import client.rendering.*;
import client.systems.client.*;
import client.systems.client.player.PlayerRotationSystem;
import framework.engine.*;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import client.network.GameServer;
import client.network.NetworkManager;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class MenuScene extends Scene {
    private Engine<Context> engine;
    private final Window window;
    private Font menuFont;
    private int selectedOption = 0; // 0: Start, 1: Exit
    private Entity<Context> selectorEntity;
    private Vector2f[] optionPositions;

    public MenuScene(Window window) {
        this.window = window;
    }

    @Override
    public void init(Engine<Context> engine) {
        this.engine = engine;

        // Ensure depth testing is enabled for proper layering
        glEnable(GL_DEPTH_TEST);

        // Disable game systems
        setGameSystemsEnabled(false);
        
        // Load Font
        try {
            ByteBuffer fontBuffer = loadResource("res/fonts/KiwiSoda.ttf");
            menuFont = new Font(fontBuffer, 48);
        } catch (IOException e) {
            e.printStackTrace();
        }

        float centerX = window.getWidth() / 2.0f;
        float centerY = window.getHeight() / 2.0f;

        // Background
        Texture bgTex = TextureAtlas.get().getRegion("menu_bg.png");
        float scaleX = (float) window.getWidth() / bgTex.width;
        float scaleY = (float) window.getHeight() / bgTex.height;
        
        Entity<Context> bg = engine.createEntity();
        bg.addComponent(new TransformComponent(new Vector2f(centerX, centerY), new Vector2f(scaleX, scaleY)));
        bg.addComponent(new RenderComponent(bgTex, 0.0f));

        // Title
        Entity<Context> title = engine.createEntity();
        title.addComponent(new TransformComponent(new Vector2f(centerX, centerY - 100), new Vector2f(1, 1), Anchor.CENTER));
        title.addComponent(new RenderComponent(TextureAtlas.get().getRegion("menu_title.png"), 0.1f));

        // Options
        optionPositions = new Vector2f[] {
            new Vector2f(centerX, centerY + 20),
            new Vector2f(centerX, centerY + 70),
            new Vector2f(centerX, centerY + 120)
        };

        Entity<Context> startText = engine.createEntity();
        startText.addComponent(new TransformComponent(optionPositions[0], new Vector2f(1, 1), Anchor.CENTER));
        startText.addComponent(new TextComponent(menuFont, "start game", new Vector4f(1, 1, 1, 1), 1.0f, 0.2f));

        Entity<Context> joinText = engine.createEntity();
        joinText.addComponent(new TransformComponent(optionPositions[1], new Vector2f(1, 1), Anchor.CENTER));
        joinText.addComponent(new TextComponent(menuFont, "join game", new Vector4f(1, 1, 1, 1), 1.0f, 0.2f));

        Entity<Context> exitText = engine.createEntity();
        exitText.addComponent(new TransformComponent(optionPositions[2], new Vector2f(1, 1), Anchor.CENTER));
        exitText.addComponent(new TextComponent(menuFont, "exit", new Vector4f(1, 1, 1, 1), 1.0f, 0.2f));

        // Selector
        selectorEntity = engine.createEntity();
        selectorEntity.addComponent(new TransformComponent(new Vector2f(Math.round(optionPositions[0].x - 120), Math.round(optionPositions[0].y)), new Vector2f(1, 1), Anchor.CENTER));
        selectorEntity.addComponent(new RenderComponent(TextureAtlas.get().getRegion("menu_selector.png"), 0.3f));
    }

    @Override
    public void update() {
        InputHandler input = InputHandler.getInstance();

        if (input.keyDown(GLFW_KEY_UP) || input.keyDown(GLFW_KEY_W)) {
            selectedOption = (selectedOption - 1 + 3) % 3;
            updateSelector();
        }
        if (input.keyDown(GLFW_KEY_DOWN) || input.keyDown(GLFW_KEY_S)) {
            selectedOption = (selectedOption + 1) % 3;
            updateSelector();
        }

        if (input.keyDown(GLFW_KEY_ENTER) || input.keyDown(GLFW_KEY_SPACE)) {
            if (selectedOption == 0) {
                // Start Game (Host) - start server first, then join
                GameServer server = new GameServer("0.0.0.0");
                new Thread(server).start();

                NetworkManager.getInstance().joinGame("127.0.0.1");
                SceneManager.setScene(new LobbyScene("127.0.0.1"), engine);
            } else if (selectedOption == 1) {
                // Join Game (Client)
                String hostIP = javax.swing.JOptionPane.showInputDialog(null, "Enter Host IP:", "Join Game", javax.swing.JOptionPane.QUESTION_MESSAGE);
                if (hostIP != null && !hostIP.isEmpty()) {
                    NetworkManager.getInstance().joinGame(hostIP);
                    SceneManager.setScene(new LobbyScene(hostIP), engine);
                }
            } else {
                // Exit Game
                glfwSetWindowShouldClose(window.getHandle(), true);
            }
        }
    }

    private void updateSelector() {
        TransformComponent tc = selectorEntity.getComponent(TransformComponent.class);
        if (tc != null) {
            float xOffset = (selectedOption == 0) ? -120 : (selectedOption == 1) ? -120 : -80;
            tc.position.set(Math.round(optionPositions[selectedOption].x + xOffset), Math.round(optionPositions[selectedOption].y));
        }
    }

    private void setGameSystemsEnabled(boolean enabled) {
        if (engine == null) return;
        
        // disableSystem(EnemySystem.class, enabled);
        disableSystem(BulletSystem.class, enabled);
        disableSystem(MovementSystem.class, enabled);
        disableSystem(PhysicsSystem.class, enabled);
        disableSystem(PlayerRotationSystem.class, enabled);
        disableSystem(DamageSystem.class, enabled);
    }

    private <T extends EntitySystem<Context>> void disableSystem(Class<T> type, boolean enabled) {
        T system = engine.getSystem(type);
        if (system != null) {
            system.setEnabled(enabled);
        }
    }

    @Override
    public void clean() {
        // We re-enable systems when leaving the menu scene
        setGameSystemsEnabled(true);
    }

    private ByteBuffer loadResource(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        ByteBuffer buffer = BufferUtils.createByteBuffer(bytes.length);
        buffer.put(bytes);
        buffer.flip();
        return buffer;
    }
}
