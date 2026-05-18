package client.scenes.overlays;

import client.components.AnimationComponent;
import client.components.RenderComponent;
import client.components.TextComponent;
import client.components.TransformComponent;
import client.rendering.Animation;
import client.network.NetworkManager;
import client.rendering.Anchor;
import client.rendering.Font;
import client.rendering.Texture;
import client.rendering.TextureAtlas;
import client.scenes.MenuScene;
import client.scenes.SceneManager;
import client.systems.client.Context;
import client.systems.client.InputHandler;
import framework.engine.Engine;
import framework.engine.Entity;
import framework.engine.Window;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class VictoryOverlay {
    private final Engine<Context> engine;
    private final List<Entity<Context>> entities = new ArrayList<>();
    private final Font font;

    private int selectedOption = 0;
    private Vector2f[] optionPositions;
    private Entity<Context> selectorEntity;
    private boolean isVisible = false;

    public VictoryOverlay(Engine<Context> engine, Font font) {
        this.engine = engine;
        this.font = font;
    }

    public void show() {
        if (isVisible) return;
        isVisible = true;

        Window window = Window.getWindow();
        float centerX = window.getWidth() / 2.0f;
        float centerY = window.getHeight() / 2.0f;

        Texture bgTex = TextureAtlas.get().getRegion("menu_bg.png");
        float scaleX = (float) window.getWidth() / bgTex.width;
        float scaleY = (float) window.getHeight() / bgTex.height;

        Entity<Context> bg = engine.createEntity();
        bg.addComponent(new TransformComponent(new Vector2f(centerX, centerY), new Vector2f(scaleX, scaleY)));
        bg.addComponent(new RenderComponent(bgTex, 0.6f, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), "fixed"));
        entities.add(bg);

        Entity<Context> title = engine.createEntity();
        title.addComponent(new TransformComponent(new Vector2f(centerX, centerY - 200), new Vector2f(1, 1), Anchor.CENTER));
        Texture winTex = TextureAtlas.get().getRegion("you_win.png");
        title.addComponent(new RenderComponent(winTex, 0.7f, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), "fixed"));
        entities.add(title);

        Entity<Context> statsBorder = engine.createEntity();
        statsBorder.addComponent(new TransformComponent(new Vector2f(centerX, centerY), new Vector2f(1, 1), Anchor.CENTER));
        statsBorder.addComponent(new RenderComponent(TextureAtlas.get().getRegion("stats_border.png"), 0.7f, new Vector4f(1, 1, 1, 1), "fixed"));
        entities.add(statsBorder);

        // Player stats table
        var stats = NetworkManager.getInstance().getGameResult();
        int maxPlayerId = NetworkManager.getInstance().getPlayerCount();
        if (stats != null) {
            for (int id : stats.kills.keySet()) {
                if (id > maxPlayerId) maxPlayerId = id;
            }
        }

        if (maxPlayerId > 0 && stats != null) {
            float rowSpacing = 55;
            float totalHeight = maxPlayerId * rowSpacing;
            float startY = centerY - totalHeight / 2 + rowSpacing / 2;
            float currentTime = (float) glfwGetTime();

            for (int playerId = 1; playerId <= maxPlayerId; playerId++) {
                int kills = stats.kills.getOrDefault(playerId, 0);
                float rowY = startY + (playerId - 1) * rowSpacing;

                String spritePath = "players/player" + playerId + ".png";
                Animation anim = Animation.fromFile(spritePath, 22, 0.1f);

                Entity<Context> sprite = engine.createEntity();
                sprite.addComponent(new TransformComponent(new Vector2f(centerX - 100, rowY), new Vector2f(1.5f, 1.5f), Anchor.CENTER));
                sprite.addComponent(new RenderComponent(anim.frames[0], 0.8f, new Vector4f(1, 1, 1, 1), "fixed"));
                sprite.addComponent(new AnimationComponent(anim, currentTime, 0, anim.frames.length - 1, true));
                entities.add(sprite);

                Entity<Context> killText = engine.createEntity();
                killText.addComponent(new TransformComponent(new Vector2f(centerX + 80, rowY), new Vector2f(1, 1), Anchor.CENTER));
                killText.addComponent(new TextComponent(font, String.valueOf(kills), new Vector4f(1, 1, 1, 1), 1.0f, 0.8f, "fixed"));
                entities.add(killText);
            }
        }

        optionPositions = new Vector2f[]{
            new Vector2f(centerX - 150, centerY + 150),
            new Vector2f(centerX + 150, centerY + 150)
        };

        Entity<Context> retryText = engine.createEntity();
        retryText.addComponent(new TransformComponent(optionPositions[0], new Vector2f(1, 1), Anchor.CENTER));
        retryText.addComponent(new TextComponent(font, "play again", new Vector4f(1, 1, 1, 1), 1.0f, 0.8f, "fixed"));
        entities.add(retryText);

        Entity<Context> exitText = engine.createEntity();
        exitText.addComponent(new TransformComponent(optionPositions[1], new Vector2f(1, 1), Anchor.CENTER));
        exitText.addComponent(new TextComponent(font, "exit", new Vector4f(1, 1, 1, 1), 1.0f, 0.8f, "fixed"));
        entities.add(exitText);

        selectorEntity = engine.createEntity();
        selectorEntity.addComponent(new TransformComponent(new Vector2f(optionPositions[selectedOption].x - 100, optionPositions[selectedOption].y), new Vector2f(1, 1), Anchor.CENTER));
        selectorEntity.addComponent(new RenderComponent(TextureAtlas.get().getRegion("menu_selector.png"), 0.9f, new Vector4f(1, 1, 1, 1), "fixed"));
        entities.add(selectorEntity);

        updateSelector();
    }

    public void hide() {
        if (!isVisible) return;
        for (Entity<Context> entity : entities) {
            engine.destroyEntity(entity.getId());
        }
        entities.clear();
        selectorEntity = null;
        isVisible = false;
    }

    public void handleInput(InputHandler input) {
        if (input.keyDown(GLFW_KEY_LEFT) || input.keyDown(GLFW_KEY_A)) {
            selectedOption = (selectedOption - 1 + 2) % 2;
            updateSelector();
        }
        if (input.keyDown(GLFW_KEY_RIGHT) || input.keyDown(GLFW_KEY_D)) {
            selectedOption = (selectedOption + 1) % 2;
            updateSelector();
        }

        if (input.keyDown(GLFW_KEY_ENTER) || input.keyDown(GLFW_KEY_SPACE)) {
            if (selectedOption == 0) {
                hide();
                NetworkManager.getInstance().stop();
                SceneManager.setScene(new MenuScene(Window.getWindow()), engine);
            } else {
                hide();
                NetworkManager.getInstance().stop();
                SceneManager.setScene(new MenuScene(Window.getWindow()), engine);
            }
        }
    }

    private void updateSelector() {
        if (selectorEntity != null && optionPositions != null) {
            var tc = selectorEntity.getComponent(TransformComponent.class);
            if (tc != null) {
                float xOffset = (selectedOption == 0) ? -100 : -80;
                tc.position.set(Math.round(optionPositions[selectedOption].x + xOffset), Math.round(optionPositions[selectedOption].y));
            }
        }
    }

    public boolean isVisible() {
        return isVisible;
    }
}
