package client.entities;

import framework.engine.Entity;
import framework.engine.Engine;
import client.systems.Context;
import client.components.*;
import client.components.enemy.EnemyComponent;
import client.components.CollisionComponent;
import client.rendering.Texture;
import client.rendering.TextureAtlas;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.primitives.AABBf;
import java.util.Random;

public class Enemy {
    private final Entity<Context> entity;

    public Enemy(Engine<Context> engine) {
        this.entity = engine.createEntity();

        TextureAtlas atlas = TextureAtlas.get();

        Texture enemyTexture;
        Random rand = new Random();
        if (rand.nextBoolean()) {
            enemyTexture = atlas.getRegion("tiles/grass.png");
        } else {
            enemyTexture = atlas.getRegion("tiles/floor.png");
        }

        float x = rand.nextFloat() * 800;
        float y = rand.nextFloat() * 600;

        entity.addComponent(new TransformComponent(new Vector2f(x, y), new Vector2f(1.5f, 1.5f)));
        entity.addComponent(new MovementComponent(50.0f, 20.0f, 10.0f, new Vector2f(0.0f, 0.0f)));
        entity.addComponent(new RenderComponent(enemyTexture));
        entity.addComponent(new EnemyComponent());
        entity.addComponent(new CollisionComponent(new AABBf(
            new Vector3f(-12.0f, -12.0f, 0.0f),
            new Vector3f(12.0f, 12.0f, 0.1f)
        )));
        entity.addComponent(new HealthComponent(50.0f)); // Enemy health
    }

    public Entity<Context> getEntity() {
        return entity;
    }
}