
package client.scenes.overlays;

import client.components.RenderComponent;
import client.components.TextComponent;
import client.components.TransformComponent;
import client.entities.*;
import client.network.NetworkManager;
import client.rendering.*;
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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


import client.scenes.*; 

public class Menu {
    private final Engine<Context> engine;
    private final List<Entity<Context>> pauseMenuEntities = new ArrayList<>();

    private int selectedOption = 0;
    private Vector2f[] optionPositions;
    private Entity<Context> selectorEntity;
    private Font pauseFont;
    private boolean isPaused = false;

    // Constructor
    public Menu(Engine<Context> engine, Font font) {
        this.engine = engine;
        this.pauseFont = font;
    }

    public void toggleMenu() {
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
        popup.addComponent(new RenderComponent(TextureAtlas.get().getRegion("menu_select.png"), 0.5f, new org.joml.Vector4f(1, 1, 1, 1), "fixed"));
        pauseMenuEntities.add(popup);

        // Options
        optionPositions = new Vector2f[]{new Vector2f(centerX, centerY - 15), new Vector2f(centerX, centerY + 35), new Vector2f(centerX, centerY + 85)};

        Entity<Context> backToGameText = engine.createEntity();
        backToGameText.addComponent(new TransformComponent(optionPositions[0], new Vector2f(1, 1), Anchor.CENTER));
        backToGameText.addComponent(new TextComponent(pauseFont, "back to game", new Vector4f(1, 1, 1, 1), 1.0f, 0.7f, "fixed"));
        pauseMenuEntities.add(backToGameText);

        Entity<Context> restartText = engine.createEntity();
        restartText.addComponent(new TransformComponent(optionPositions[1], new Vector2f(1, 1), Anchor.CENTER));
        restartText.addComponent(new TextComponent(pauseFont, "restart", new Vector4f(1, 1, 1, 1), 1.0f, 0.7f, "fixed"));
        pauseMenuEntities.add(restartText);

        Entity<Context> backText = engine.createEntity();
        backText.addComponent(new TransformComponent(optionPositions[2], new Vector2f(1, 1), Anchor.CENTER));
        backText.addComponent(new TextComponent(pauseFont, "back to title", new Vector4f(1, 1, 1, 1), 1.0f, 0.7f, "fixed"));
        pauseMenuEntities.add(backText);

        // Selector
        selectorEntity = engine.createEntity();
        selectorEntity.addComponent(new TransformComponent(new Vector2f(optionPositions[selectedOption].x - 120, optionPositions[selectedOption].y), new Vector2f(1, 1), Anchor.CENTER));
        selectorEntity.addComponent(new RenderComponent(TextureAtlas.get().getRegion("menu_selector.png"), 0.8f, new org.joml.Vector4f(1, 1, 1, 1), "fixed"));
        pauseMenuEntities.add(selectorEntity);

        updateSelector();
    }

    public void hidePauseMenu() {
        for (Entity<Context> entity : pauseMenuEntities) {
            engine.destroyEntity(entity.getId());
        }
        pauseMenuEntities.clear();
        selectorEntity = null;
        isPaused = false;
    }

    public void handlePauseMenuInput(InputHandler input) {
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
                NetworkManager.getInstance().stop();
                SceneManager.setScene(new MenuScene(Window.getWindow()), engine);
            } else {
                // Back to Title
                NetworkManager.getInstance().stop();
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

    public boolean isVisible(){
        return isPaused;
    }
}
