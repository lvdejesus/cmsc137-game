package game.components;

import game.rendering.Font;
import framework.engine.Component;
import org.joml.Vector4f;

public class TextComponent implements Component {
    public Font font;
    public String text;
    public Vector4f color;
    public float scale;

    public TextComponent(Font font, String text, Vector4f color, float scale) {
        this.font = font;
        this.text = text;
        this.color = color;
        this.scale = scale;
    }

    public TextComponent(Font font, String text, Vector4f color) {
        this(font, text, color, 1.0f);
    }

    public TextComponent(Font font, String text) {
        this(font, text, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f));
    }
}