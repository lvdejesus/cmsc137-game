package client.entities;

import framework.engine.Entity;
import framework.engine.Engine;
import client.systems.Context;
import client.components.*;
import client.rendering.Animation;
import org.joml.Vector2f;
import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class Player {
    private final Entity<Context> entity;

    public Player(Engine<Context> engine) {
        // Initialize Player Stats
        int maxHealth = 6;
        int health = 6;
        float movement_speed = 300.0f;
        float friction = 800.0f;
        float acceleration = 2000.0f;

        // Create a blank entity from the engine
        this.entity = engine.createEntity();

        // Add components to the internal entity
        entity.addComponent(new TransformComponent(new Vector2f(400.0f, 300.0f), new Vector2f(2.0f, 2.0f)));
        entity.addComponent(new MovementComponent(movement_speed, acceleration, friction, new Vector2f(0.0f, 0.0f)));
        entity.addComponent(new RenderComponent());
        
        double currentTime = glfwGetTime();
        entity.addComponent(new AnimationComponent(Animation.fromFile("test-Sheet.png", 22, 0.1f), (float) currentTime));
    }

    public Entity<Context> getEntity() {
        return entity;
    }
}