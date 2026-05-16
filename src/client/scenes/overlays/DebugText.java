package client.scenes.overlays;

import client.components.TextComponent;
import client.components.TransformComponent;
import client.rendering.Anchor;
import client.rendering.Font;
import client.systems.client.Context;
import framework.engine.Engine;
import framework.engine.Entity;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import framework.engine.Window;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;

public class DebugText {
    private final Entity<Context> entity;
    private final TextComponent textComponent;
    private static Font font;
    
    public DebugText(Engine<Context> engine, Font font, String initialText) {
        this.entity = engine.createEntity();
        
        this.entity.addComponent(new TransformComponent(
            new Vector2f(20, Window.getWindow().getHeight() - 20.0f),
            new Vector2f(1, 1), 
            Anchor.BOTTOM_LEFT
        ));

        this.textComponent = new TextComponent(
            font, 
            initialText, 
            new Vector4f(1, 1, 1, 1), 
            1.0f, 
            1.0f,
            "fixed"
        );
        
        this.entity.addComponent(this.textComponent);
    }

    public static DebugText create(Engine<Context> engine, String initialText) {
        try {
            if (font == null) {
                byte[] bytes = Files.readAllBytes(Paths.get("res/fonts/KiwiSoda.ttf"));
                ByteBuffer buffer = BufferUtils.createByteBuffer(bytes.length);
                buffer.put(bytes);
                buffer.flip();
                font = new Font(buffer, 32);
            }
            return new DebugText(engine, font, initialText);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setText(String newText) {
        this.textComponent.text = newText;
    }
    
    public void destroy(Engine<Context> engine) {
        engine.destroyEntity(entity.getId());
    }
}
