package client.scenes.overlays;

import client.components.RenderComponent;
import client.components.TransformComponent;
import client.rendering.*;
import client.systems.client.Context;
import framework.engine.*;

import org.joml.Vector2f;
import org.joml.Vector4f;

public class XpBar {
    private final Engine<Context> engine;
    private Entity<Context> barEntity;
    private float fullU2;
    private int texWidth, texHeight;

    private static final float SCREEN_X = 96, SCREEN_Y = 113;
    private static final float SCALE = 3;

    public XpBar(Engine<Context> engine) {
        this.engine = engine;
    }

    public void create() {
        Texture tex = TextureAtlas.get().getRegion("healthbar/xpbar.png").copy();
        texWidth = tex.width;
        texHeight = tex.height;
        fullU2 = tex.u2;

        barEntity = engine.createEntity();
        barEntity.addComponent(new TransformComponent(
            new Vector2f(SCREEN_X, SCREEN_Y),
            new Vector2f(SCALE, SCALE),
            Anchor.TOP_LEFT
        ));
        barEntity.addComponent(new RenderComponent(tex, 0.35f, new Vector4f(1, 1, 1, 1), "fixed"));
    }

    public void updateXp(int exp) {
        int level = (int) Math.floor(Math.log1p(exp / 100.0) / Math.log(2));
        int prevThreshold = (int) (100 * (Math.pow(2, level) - 1));
        int nextThreshold = (int) (100 * (Math.pow(2, level + 1) - 1));
        float p = (exp - prevThreshold) / (float) (nextThreshold - prevThreshold);
        if (p < 0) p = 0;
        if (p > 1) p = 1;
        updateProgress(p);
    }

    public void updateProgress(float p) {
        if (barEntity == null) return;
        TransformComponent tc = barEntity.getComponent(TransformComponent.class);
        if (tc != null) {
            tc.scale.x = SCALE * p;
        }
        RenderComponent rc = barEntity.getComponent(RenderComponent.class);
        if (rc != null && rc.texture != null) {
            rc.texture.u2 = rc.texture.u1 + (fullU2 - rc.texture.u1) * p;
        }
    }

    public void destroy() {
        if (barEntity != null) {
            engine.destroyEntity(barEntity.getId());
            barEntity = null;
        }
    }
}
