package client.entities;

import framework.engine.Entity;
import framework.engine.Engine;
import client.systems.Context;
import client.components.*;
import client.components.bullet.BulletComponent;
import client.components.CollisionComponent;
import client.rendering.Texture;
import client.rendering.TextureAtlas;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.primitives.AABBf;

public class Bullet {
    private final Entity<Context> entity;

    public Bullet(Engine<Context> engine, float x, float y, float angleDegrees) {
        this(engine, x, y, angleDegrees, false);
    }

    public Bullet(Engine<Context> engine, float x, float y, float angleDegrees, boolean isEnemy) {
        this.entity = engine.createEntity();

        TextureAtlas atlas = TextureAtlas.get();
        String textureName = isEnemy ? "tiles/floor.png" : "tiles/grass.png";
        Texture bulletTexture = atlas.getRegion(textureName);

        float angleRadians = (float) Math.toRadians(angleDegrees);

        float speed = isEnemy ? 300.0f : 400.0f;
        float vx = (float) Math.cos(angleRadians) * speed;
        float vy = (float) Math.sin(angleRadians) * speed;

        entity.addComponent(new TransformComponent(new Vector2f(x, y), new Vector2f(0.5f, 0.5f)));
        entity.addComponent(new MovementComponent(0, 0, 0, new Vector2f(vx, vy)));
        entity.addComponent(new RenderComponent(bulletTexture));
        
        BulletComponent bulletComp = new BulletComponent();
        bulletComp.isEnemy = isEnemy;
        bulletComp.lifetime = isEnemy ? 4.0f : 2.0f;
        entity.addComponent(bulletComp);
        
        entity.addComponent(new CollisionComponent(new AABBf(
            new Vector3f(-4.0f, -4.0f, 0.0f),
            new Vector3f(4.0f, 4.0f, 0.1f)
        )));
    }

    public Entity<Context> getEntity() {
        return entity;
    }
}