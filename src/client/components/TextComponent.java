package client.components;

import client.rendering.Font;
import framework.engine.Component;
import org.joml.Vector4f;

public class TextComponent implements Component {
    public Font font;
    public String text;
    public Vector4f color;
    public float scale;
    public float z;
    public String layer;

    public TextComponent(Font font, String text, Vector4f color, float scale, float z, String layer) {
        this.font = font;
        this.text = text;
        this.color = color;
        this.scale = scale;
        this.z = z;
        this.layer = layer;
    }

    public TextComponent(Font font, String text, Vector4f color, float scale, float z) {
        this(font, text, color,  scale, z, "default");
    }

    public TextComponent(Font font, String text, Vector4f color, float scale) {
        this(font, text, color, scale, 0.0f);
    }

    public TextComponent(Font font, String text, Vector4f color) {
        this(font, text, color, 1.0f);
    }

    public TextComponent(Font font, String text) {
        this(font, text, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f));
    }
}