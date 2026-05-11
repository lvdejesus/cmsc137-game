package client.scenes;

import client.components.RenderComponent;
import client.components.TextComponent;
import client.components.TransformComponent;
import client.entities.Bullet;
import client.entities.Player;
import client.rendering.*;
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

public class LevelScene implements Scene {
    private Engine<Context> engine;
    private Player player;
    private TransformComponent playerTransform;
    
    private boolean isPaused = false;
    private Font pauseFont;
    private List<Entity<Context>> pauseMenuEntities = new ArrayList<>();
    private int selectedOption = 0; // 0: restart, 1: back to title
    private Vector2f[] optionPositions;
    private Entity<Context> selectorEntity;

    @Override
    public void init(Engine<Context> engine) {
        this.engine = engine;
        
        // Ensure systems are enabled
        setGameSystemsEnabled(true);

        this.player = new Player(engine);
        this.playerTransform = player.getEntity().getComponent(TransformComponent.class);

        // Load background map
        Entity<Context> mapBg = engine.createEntity();
        mapBg.addComponent(new TransformComponent(new Vector2f(400, 300), new Vector2f(800.0f/1339.0f, 600.0f/1175.0f), Anchor.CENTER));
        mapBg.addComponent(new RenderComponent(TextureAtlas.get().getRegion("map_1.png"), -0.5f));


        // Load Font for pause menu
        try {
            ByteBuffer fontBuffer = loadResource("res/fonts/KiwiSoda.ttf");
            pauseFont = new Font(fontBuffer, 32); // Smaller font
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setGameSystemsEnabled(boolean enabled) {
        enableSystem(client.systems.EnemySystem.class, enabled);
        enableSystem(client.systems.BulletSystem.class, enabled);
        enableSystem(client.systems.MovementSystem.class, enabled);
        enableSystem(client.systems.player.PlayerRotationSystem.class, enabled);
        enableSystem(client.systems.DamageSystem.class, enabled);
        enableSystem(client.systems.PhysicsSystem.class, enabled);
    }

    private <T extends framework.engine.EntitySystem<Context>> void enableSystem(Class<T> type, boolean enabled) {
        T system = engine.getSystem(type);
        if (system != null) {
            system.setEnabled(enabled);
        }
    }

    @Override
    public void update() {
        InputHandler input = InputHandler.getInstance();

        // Toggle menu with Esc
        if (input.keyDown(GLFW_KEY_ESCAPE)) {
            toggleMenu();
        }

        if (isPaused) {
            handlePauseMenuInput(input);
        }

        for (InputHandler.MouseEvent event : input.getEvents()) {
            if (event.type == InputHandler.MouseEventType.LEFT_CLICK && !event.consumed) {
                event.consume();

                if (playerTransform != null) {
                    float playerX = playerTransform.position.x;
                    float playerY = playerTransform.position.y;

                    float mouseX = event.position.x;
                    float mouseY = event.position.y;

                    float dx = mouseX - playerX;
                    float dy = mouseY - playerY;
                    float angle = (float) Math.toDegrees(Math.atan2(dy, dx));

                    new Bullet(engine, playerX, playerY, angle);
                }
            }
        }
    }

    private void toggleMenu() {
        isPaused = !isPaused;
        
        if (isPaused) {
            showPauseMenu();
        } else {
            hidePauseMenu();
        }
    }

    private void showPauseMenu() {
        Window window = Window.getWindow();
        float centerX = window.getWidth() / 2.0f;
        float centerY = window.getHeight() / 2.0f;

        // Popup background (menu_select)
        Entity<Context> popup = engine.createEntity();
        popup.addComponent(new TransformComponent(new Vector2f(centerX, centerY), new Vector2f(1, 1), Anchor.CENTER));
        popup.addComponent(new RenderComponent(TextureAtlas.get().getRegion("menu_select.png"), 0.5f));
        pauseMenuEntities.add(popup);

        // Options
        optionPositions = new Vector2f[] {
            new Vector2f(centerX, centerY - 15),
            new Vector2f(centerX, centerY + 35),
            new Vector2f(centerX, centerY + 85)
        };

        Entity<Context> backToGameText = engine.createEntity();
        backToGameText.addComponent(new TransformComponent(optionPositions[0], new Vector2f(1, 1), Anchor.CENTER));
        backToGameText.addComponent(new TextComponent(pauseFont, "back to game", new Vector4f(1, 1, 1, 1), 1.0f, 0.7f));
        pauseMenuEntities.add(backToGameText);

        Entity<Context> restartText = engine.createEntity();
        restartText.addComponent(new TransformComponent(optionPositions[1], new Vector2f(1, 1), Anchor.CENTER));
        restartText.addComponent(new TextComponent(pauseFont, "restart", new Vector4f(1, 1, 1, 1), 1.0f, 0.7f));
        pauseMenuEntities.add(restartText);

        Entity<Context> backText = engine.createEntity();
        backText.addComponent(new TransformComponent(optionPositions[2], new Vector2f(1, 1), Anchor.CENTER));
        backText.addComponent(new TextComponent(pauseFont, "back to title", new Vector4f(1, 1, 1, 1), 1.0f, 0.7f));
        pauseMenuEntities.add(backText);

        // Selector
        selectorEntity = engine.createEntity();
        selectorEntity.addComponent(new TransformComponent(new Vector2f(optionPositions[selectedOption].x - 120, optionPositions[selectedOption].y), new Vector2f(1, 1), Anchor.CENTER));
        selectorEntity.addComponent(new RenderComponent(TextureAtlas.get().getRegion("menu_selector.png"), 0.8f));
        pauseMenuEntities.add(selectorEntity);
        
        updateSelector();
    }

    private void hidePauseMenu() {
        for (Entity<Context> entity : pauseMenuEntities) {
            engine.destroyEntity(entity.getId());
        }
        pauseMenuEntities.clear();
        selectorEntity = null;
    }

    private void handlePauseMenuInput(InputHandler input) {
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
                // Back to Game
                toggleMenu();
            } else if (selectedOption == 1) {
                // Restart
                SceneManager.setScene(new LevelScene(), engine);
            } else {
                // Back to Title
                SceneManager.setScene(new MenuScene(Window.getWindow()), engine);
            }
        }
    }

    private void updateSelector() {
        if (selectorEntity != null && optionPositions != null) {
            TransformComponent tc = selectorEntity.getComponent(TransformComponent.class);
            if (tc != null) {
                // Adjusting X offset based on text length to position selector correctly
                float xOffset = (selectedOption == 0) ? -120 : (selectedOption == 1) ? -80 : -140; 
                // Using Math.round to avoid subpixel rendering artifacts (the 'little line')
                tc.position.set(Math.round(optionPositions[selectedOption].x + xOffset), Math.round(optionPositions[selectedOption].y));
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
        hidePauseMenu();
    }
}
