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

public record Enemy(Entity<Context> entity) {
    public Enemy(Engine<Context> entity) {
        this.entity = entity.createEntity();

        TextureAtlas atlas = TextureAtlas.get();

        Texture enemyTexture;
        Random rand = new Random();
        if (rand.nextBoolean()) {
            enemyTexture = atlas.getRegion("tiles/enemy_1.png");
        } else {
            enemyTexture = atlas.getRegion("tiles/enemy_2.png");
        }

        float x = rand.nextFloat() * 800;
        float y = rand.nextFloat() * 600;

        this.entity.addComponent(new TransformComponent(new Vector2f(x, y), new Vector2f(1.5f, 1.5f)));
        this.entity.addComponent(new MovementComponent(50.0f, 20.0f, 10.0f, new Vector2f(0.0f, 0.0f)));
        this.entity.addComponent(new RenderComponent(enemyTexture));
        this.entity.addComponent(new EnemyComponent());
        this.entity.addComponent(new CollisionComponent(new AABBf(
            new Vector3f(-12.0f, -12.0f, 0.0f),
            new Vector3f(12.0f, 12.0f, 0.1f)
        )));
        this.entity.addComponent(new HealthComponent(50.0f)); // Enemy health
    }
}