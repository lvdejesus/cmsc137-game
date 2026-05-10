package client.scenes;

import client.components.RenderComponent;
import client.components.TextComponent;
import client.components.TransformComponent;
import client.rendering.*;
import client.systems.*;
import client.systems.player.PlayerRotationSystem;
import framework.engine.Engine;
import framework.engine.Entity;
import framework.engine.EntitySystem;
import framework.engine.Window;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class MenuScene implements Scene {
    private Engine<Context> engine;
    private Window window;
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

        // Disable depth test for 2D menu rendering to ensure correct layering by draw order
        glDisable(GL_DEPTH_TEST);

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
        title.addComponent(new TransformComponent(new Vector2f(centerX, centerY - 100), new Vector2f(1, 1)));
        title.addComponent(new RenderComponent(TextureAtlas.get().getRegion("menu_title.png"), 0.0f));

        // Options
        optionPositions = new Vector2f[] {
            new Vector2f(centerX, centerY + 50),
            new Vector2f(centerX, centerY + 100)
        };

        Entity<Context> startText = engine.createEntity();
        startText.addComponent(new TransformComponent(optionPositions[0], new Vector2f(1, 1), Anchor.CENTER));
        startText.addComponent(new TextComponent(menuFont, "start"));

        Entity<Context> exitText = engine.createEntity();
        exitText.addComponent(new TransformComponent(optionPositions[1], new Vector2f(1, 1), Anchor.CENTER));
        exitText.addComponent(new TextComponent(menuFont, "exit"));

        // Selector
        selectorEntity = engine.createEntity();
        selectorEntity.addComponent(new TransformComponent(new Vector2f(optionPositions[0].x - 60, optionPositions[0].y), new Vector2f(1, 1)));
        selectorEntity.addComponent(new RenderComponent(TextureAtlas.get().getRegion("menu_selector.png"), 0.0f));
    }

    @Override
    public void update() {
        InputHandler input = InputHandler.getInstance();

        if (input.keyDown(GLFW_KEY_UP) || input.keyDown(GLFW_KEY_W)) {
            selectedOption = (selectedOption - 1 + 2) % 2;
            updateSelector();
        }
        if (input.keyDown(GLFW_KEY_DOWN) || input.keyDown(GLFW_KEY_S)) {
            selectedOption = (selectedOption + 1) % 2;
            updateSelector();
        }

        if (input.keyDown(GLFW_KEY_ENTER) || input.keyDown(GLFW_KEY_SPACE)) {
            if (selectedOption == 0) {
                // Start Game
                SceneManager.setScene(new LevelScene(), engine);
            } else {
                // Exit Game
                glfwSetWindowShouldClose(window.getHandle(), true);
            }
        }
    }

    private void updateSelector() {
        TransformComponent tc = selectorEntity.getComponent(TransformComponent.class);
        if (tc != null) {
            tc.position.set(optionPositions[selectedOption].x - 60, optionPositions[selectedOption].y);
        }
    }

    private void setGameSystemsEnabled(boolean enabled) {
        if (engine == null) return;
        
        disableSystem(EnemySystem.class, enabled);
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
        // Re-enable depth test for the game
        glEnable(GL_DEPTH_TEST);
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
