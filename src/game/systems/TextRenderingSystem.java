package game.systems;

import game.components.TextComponent;
import game.components.TransformComponent;
import game.rendering.Batch;
import game.rendering.Font;
import game.rendering.Texture;
import game.rendering.Anchor;
import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.EntitySystem;

import static org.lwjgl.opengl.GL33.*;

public class TextRenderingSystem extends EntitySystem<Context> {
    private ComponentMapper<TextComponent> tm;
    private ComponentMapper<TransformComponent> trm;
    private Batch batch;

    public TextRenderingSystem() {
        super(TextComponent.class, TransformComponent.class);
        batch = new Batch();
    }

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);

        this.tm = engine.getMapper(TextComponent.class);
        this.trm = engine.getMapper(TransformComponent.class);
    }

    @Override
    public void processEntity(int id, Context ctx) {
        TextComponent textComp = tm.get(id);
        TransformComponent transform = trm.get(id);

        if (textComp == null || textComp.font == null || textComp.text == null || textComp.text.isEmpty()) {
            return;
        }

        glBindTexture(GL_TEXTURE_2D, textComp.font.getTextureID());

        float length = 0;
        for (char c : textComp.text.toCharArray()) {
            Font.Glyph glyph = textComp.font.getGlyph(c);
            length += glyph.xAdvance * transform.scale.x * textComp.scale;
        }

        float x = transform.position.x - length * (transform.anchor.getXOffset());
        float y = transform.position.y + textComp.font.getAscent() - (textComp.font.getAscent() - textComp.font.getDescent()) * transform.anchor.getYOffset();
        float z = 0;

        for (char c : textComp.text.toCharArray()) {
            Font.Glyph glyph = textComp.font.getGlyph(c);
            if (glyph == null) {
                continue;
            }

            float charX = x + glyph.xOffset * textComp.scale;
            float charY = y + glyph.yOffset * textComp.scale;

            Texture glyphTex = new Texture(glyph.u1, glyph.v1, glyph.u2, glyph.v2, glyph.width, glyph.height);

            batch.draw(
                glyphTex,
                charX,
                charY,
                z,
                transform.rotation,
                glyph.width * transform.scale.x * textComp.scale,
                glyph.height * transform.scale.y * textComp.scale,
                textComp.color.x,
                textComp.color.y,
                textComp.color.z,
                textComp.color.w,
                Anchor.TOP_LEFT
            );

            x += glyph.xAdvance * transform.scale.x * textComp.scale;
        }

        batch.flush();
    }
}