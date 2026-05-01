package client.entities;

import framework.engine.Entity;
import framework.engine.Engine;
import client.systems.Context;
import client.components.*;
import client.components.player.PlayerStateComponent;
import client.components.player.PlayerTagComponent;
import client.rendering.Animation;
import org.joml.Vector2f;
import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class Player {
    private final Entity<Context> entity;

    public Player(Engine<Context> engine) {
        // Initialize Player Base Stats 
        int maxHealth = 6;
        int health = 6;
        float movement_speed = 200.0f;
        float friction = 800.0f;
        float acceleration = 1200.0f;
        double currentTime = glfwGetTime();

        // Create a blank entity from the engine
        this.entity = engine.createEntity();

        // 4-direction animations — each file is a 500x122px strip of 4 frames
        Animation[] anims = new Animation[4];
        anims[0] = Animation.fromFile("players/row_down.png",  4, 0.15f); // Down
        anims[1] = Animation.fromFile("players/row_left.png",  4, 0.15f); // Left
        anims[2] = Animation.fromFile("players/row_right.png", 4, 0.15f); // Right
        anims[3] = Animation.fromFile("players/row_up.png",    4, 0.15f); // Up

        // Add components to the internal entity
        entity.addComponent(new TransformComponent(new Vector2f(400.0f, 300.0f), new Vector2f(0.6f, 0.6f)));
        entity.addComponent(new MovementComponent(movement_speed, acceleration, friction, new Vector2f(0.0f, 0.0f)));
        entity.addComponent(new RenderComponent());
        entity.addComponent(new AnimationComponent(anims, (float) currentTime));
        entity.addComponent(new PlayerStateComponent());
        entity.addComponent(new PlayerTagComponent());
    }

    public Entity<Context> getEntity() {
        return entity;
    }
}