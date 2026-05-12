package client.entities;

import framework.engine.Entity;
import framework.engine.Engine;
import client.systems.client.Context;
import client.components.*;
import client.components.player.PlayerStateComponent;
import client.components.player.PlayerTagComponent;
import client.components.CollisionComponent;
import client.rendering.Animation;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.primitives.AABBf;
import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class Player {
    private final Entity<Context> entity;

    public Player(Engine<Context> engine, int playerIndex) {
        // Initialize Player Base Stats 
        int maxHealth = 6;
        int health = 6;
        float movement_speed = 1000.0f;
        float friction = 200.0f;
        float acceleration = 500.0f;
        double currentTime = glfwGetTime();

        // Create a blank entity from the engine
        this.entity = engine.createEntity();

        // Add components to the internal entity
        entity.addComponent(new TransformComponent(new Vector2f(400.0f, 300.0f), new Vector2f(2.0f, 2.0f)));
        entity.addComponent(new MovementComponent(movement_speed, acceleration, friction, new Vector2f(0.0f, 0.0f)));
        entity.addComponent(new RenderComponent());        
        
        String spritePath = "players/player" + playerIndex + ".png";
        entity.addComponent(new AnimationComponent(Animation.fromFile(spritePath, 22, 0.1f), (float) currentTime));
        entity.addComponent(new PlayerStateComponent());
        entity.addComponent(new PlayerTagComponent());
        entity.addComponent(new CollisionComponent(new AABBf(
            new Vector3f(-16.0f, -16.0f, 0.0f),
            new Vector3f(16.0f, 16.0f, 0.1f)
        )));
        entity.addComponent(new HealthComponent(100.0f));
    }

    public Entity<Context> getEntity() {
        return entity;
    }
}