package client.entities;

import framework.engine.Entity;
import framework.engine.Engine;
import client.systems.Context;
import client.components.*;
import client.components.enemy.EnemyComponent;
import client.components.CollisionComponent;
import client.rendering.Texture;
import client.rendering.Animation;
import client.rendering.TextureAtlas;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.primitives.AABBf;
import java.util.Random;
import static org.lwjgl.glfw.GLFW.glfwGetTime;

public record Enemy(Entity<Context> entity) {
    public Enemy(Engine<Context> engine) {
        this(engine.createEntity());

        TextureAtlas atlas = TextureAtlas.get();
        Texture enemyTexture;
        double currentTime = glfwGetTime();
        
        Random rand = new Random();
        float x = rand.nextFloat() * 800;
        float y = rand.nextFloat() * 600;

        String spritePath = "enemy.png";
        this.entity.addComponent(new AnimationComponent(Animation.fromFile(spritePath, 8, 0.1f), (float) currentTime,true));
        this.entity.addComponent(new RenderComponent());

        this.entity.addComponent(new TransformComponent(new Vector2f(x, y), new Vector2f(1.5f, 1.5f)));
        this.entity.addComponent(new MovementComponent(50.0f, 20.0f, 10.0f, new Vector2f(0.0f, 0.0f)));
        this.entity.addComponent(new EnemyComponent());
        this.entity.addComponent(new CollisionComponent(new AABBf(
            new Vector3f(-12.0f, -12.0f, 0.0f),
            new Vector3f(12.0f, 12.0f, 0.1f)
        )));
        this.entity.addComponent(new HealthComponent(50.0f)); // Enemy health
    }
}