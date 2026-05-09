package client.entities;

import framework.engine.Entity;
import framework.engine.Engine;
import client.systems.Context;
import client.components.*;
import client.components.bullet.BulletComponent;
import client.rendering.Texture;
import client.rendering.TextureAtlas;
import org.joml.Vector2f;

public class Bullet {
    private final Entity<Context> entity;

    public Bullet(Engine<Context> engine, float x, float y, float angleDegrees) {
        this.entity = engine.createEntity();

        TextureAtlas atlas = TextureAtlas.get();
        Texture bulletTexture = atlas.getRegion("tiles/grass.png");

        float angleRadians = (float) Math.toRadians(angleDegrees);

        float speed = 400.0f;
        float vx = (float) Math.cos(angleRadians) * speed;
        float vy = (float) Math.sin(angleRadians) * speed;

        entity.addComponent(new TransformComponent(new Vector2f(x, y), new Vector2f(0.5f, 0.5f)));
        entity.addComponent(new MovementComponent(0, 0, 0, new Vector2f(vx, vy)));
        entity.addComponent(new RenderComponent(bulletTexture));
        entity.addComponent(new BulletComponent());
    }

    public Entity<Context> getEntity() {
        return entity;
    }
}