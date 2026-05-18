package client.scenes.overlays;

import client.components.BossComponent;
import client.components.HealthComponent;
import client.components.RenderComponent;
import client.components.TransformComponent;
import client.network.NetworkManager;
import client.rendering.*;
import client.systems.client.Context;
import framework.engine.*;

import org.joml.Vector2f;
import org.joml.Vector4f;

public class BossHealthBar {
    private final Engine<Context> engine;
    private Entity<Context> bgEntity;
    private Entity<Context> fillEntity;
    private float fullU2;
    private int fillW, fillH;
    private boolean visible = true;

    private static final float SCALE = 3;

    public BossHealthBar(Engine<Context> engine) {
        this.engine = engine;
    }

    public void create() {
        Window window = Window.getWindow();
        Texture bgTex = TextureAtlas.get().getRegion("healthbar/boss-healthbar.png");
        Texture fillTex = TextureAtlas.get().getRegion("healthbar/boss-health.png").copy();

        float bgW = bgTex.width * SCALE;
        float bgH = bgTex.height * SCALE;
        float cx = window.getWidth() / 2.0f;
        float cy = 30.0f;

        bgEntity = engine.createEntity();
        bgEntity.addComponent(new TransformComponent(new Vector2f(cx - bgW / 2, cy), new Vector2f(SCALE, SCALE), Anchor.TOP_LEFT));
        bgEntity.addComponent(new RenderComponent(bgTex, 0.3f, new Vector4f(1, 1, 1, 1), "fixed"));

        fillW = fillTex.width;
        fillH = fillTex.height;
        fullU2 = fillTex.u2;

        fillEntity = engine.createEntity();
        fillEntity.addComponent(new TransformComponent(new Vector2f(cx - bgW / 2 + 17 * SCALE, cy + 14 * SCALE), new Vector2f(SCALE, SCALE), Anchor.TOP_LEFT));
        fillEntity.addComponent(new RenderComponent(fillTex, 0.35f, new Vector4f(1, 1, 1, 1), "fixed"));

        setVisible(false);
    }

    public void update() {
        if (!NetworkManager.getInstance().isBossFightStarted()) {
            setVisible(false);
            return;
        }

        Integer bossId = null;
        Iterable<Integer> bossEntities = engine.getFamily(BossComponent.class, HealthComponent.class)::iterator;
        for (int id : bossEntities) {
            bossId = id;
            break;
        }

        if (bossId == null) {
            setVisible(false);
            return;
        }

        setVisible(true);
        HealthComponent hc = engine.getMapper(HealthComponent.class).get(bossId);
        float p = hc.currentHealth / hc.maxHealth;
        if (p < 0) p = 0;
        if (p > 1) p = 1;

        TransformComponent tc = fillEntity.getComponent(TransformComponent.class);
        if (tc != null) {
            tc.scale.x = SCALE * p;
        }
        RenderComponent rc = fillEntity.getComponent(RenderComponent.class);
        if (rc != null && rc.texture != null) {
            rc.texture.u2 = rc.texture.u1 + (fullU2 - rc.texture.u1) * p;
        }
    }

    private void setVisible(boolean v) {
        if (visible == v) return;
        visible = v;
        if (bgEntity != null) {
            TransformComponent tc = bgEntity.getComponent(TransformComponent.class);
            if (tc != null) tc.scale.set(v ? SCALE : 0, v ? SCALE : 0);
        }
        if (fillEntity != null) {
            TransformComponent tc = fillEntity.getComponent(TransformComponent.class);
            if (tc != null) tc.scale.x = tc.scale.y = v ? SCALE : 0;
        }
    }

    public void destroy() {
        if (bgEntity != null) { engine.destroyEntity(bgEntity.getId()); bgEntity = null; }
        if (fillEntity != null) { engine.destroyEntity(fillEntity.getId()); fillEntity = null; }
    }
}
