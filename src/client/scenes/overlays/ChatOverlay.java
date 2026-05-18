package client.scenes.overlays;

import client.components.TextComponent;
import client.components.TransformComponent;
import client.network.NetworkManager;
import client.network.messages.client.C_ChatMessage;
import client.rendering.*;
import client.systems.client.Context;
import client.systems.client.InputHandler;
import framework.engine.*;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class ChatOverlay {
    private final Engine<Context> engine;
    private final Font font;
    private boolean active = false;
    private final StringBuilder inputBuffer = new StringBuilder();
    private Entity<Context> inputTextEntity;
    private final List<Entity<Context>> messageEntities = new ArrayList<>();
    private int lastMessageCount = 0;
    private static final int MAX_MESSAGES = 10;
    private static final float FONT_SCALE = 0.4f;
    private static final int LINE_HEIGHT = 20;

    public ChatOverlay(Engine<Context> engine, Font font) {
        this.engine = engine;
        this.font = font;
    }

    public boolean isActive() {
        return active;
    }

    public void open() {
        active = true;
        inputBuffer.setLength(0);
        rebuildInputDisplay();
    }

    public void close() {
        active = false;
        inputBuffer.setLength(0);
        clearInputDisplay();
    }

    public void update() {
        InputHandler input = InputHandler.getInstance();

        if (!active) {
            if (input.keyDown(GLFW_KEY_T)) {
                open();
                return;
            }
            rebuildMessageDisplay();
            return;
        }

        String chars = input.consumeTypedChars();
        inputBuffer.append(chars);
        if (inputBuffer.length() > 100) {
            inputBuffer.setLength(100);
        }

        if (input.keyDown(GLFW_KEY_BACKSPACE) && inputBuffer.length() > 0) {
            inputBuffer.deleteCharAt(inputBuffer.length() - 1);
        }

        if (input.keyDown(GLFW_KEY_ENTER)) {
            if (!inputBuffer.isEmpty()) {
                NetworkManager.getInstance().sendMessage(new C_ChatMessage(inputBuffer.toString()));
            }
            close();
            return;
        }

        if (input.keyDown(GLFW_KEY_ESCAPE)) {
            close();
            return;
        }

        rebuildInputDisplay();
    }

    private void clearInputDisplay() {
        if (inputTextEntity != null) {
            engine.destroyEntity(inputTextEntity.getId());
            inputTextEntity = null;
        }
    }

    private void rebuildInputDisplay() {
        clearInputDisplay();

        String text = "> " + inputBuffer + (System.currentTimeMillis() / 500 % 2 == 0 ? "|" : " ");
        inputTextEntity = engine.createEntity();
        TransformComponent tc = new TransformComponent(new Vector2f(10, -10), new Vector2f(1, 1), Anchor.BOTTOM_LEFT);
        tc.globalAnchor = true;
        inputTextEntity.addComponent(tc);
        inputTextEntity.addComponent(new TextComponent(font, text, new Vector4f(1, 1, 1, 1), FONT_SCALE, 0.5f, "fixed"));
    }

    private void rebuildMessageDisplay() {
        var messages = NetworkManager.getInstance().chatMessages;
        int count = messages.size();
        if (count == lastMessageCount) return;
        lastMessageCount = count;

        for (var e : messageEntities) {
            engine.destroyEntity(e.getId());
        }
        messageEntities.clear();

        int start = Math.max(0, count - MAX_MESSAGES);
        for (int i = start; i < count; i++) {
            int idx = i - start;
            float y = -(40 + idx * LINE_HEIGHT);
            Entity<Context> e = engine.createEntity();
            TransformComponent tc = new TransformComponent(new Vector2f(10, y), new Vector2f(1, 1), Anchor.BOTTOM_LEFT);
            tc.globalAnchor = true;
            e.addComponent(tc);
            e.addComponent(new TextComponent(font, messages.get(i), new Vector4f(1, 1, 1, 1), FONT_SCALE, 0.5f, "fixed"));
            messageEntities.add(e);
        }
    }

    public void destroy() {
        clearInputDisplay();
        for (var e : messageEntities) {
            engine.destroyEntity(e.getId());
        }
        messageEntities.clear();
    }
}
