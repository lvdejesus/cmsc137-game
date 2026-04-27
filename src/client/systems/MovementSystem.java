package client.systems;

import client.components.MovementComponent;

import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.EntitySystem;

import static org.lwjgl.glfw.GLFW.*;

public class MovementSystem extends EntitySystem<Context> {
    private ComponentMapper<MovementComponent> mm;
    
    private float approach(float current, float target, float delta) {
        if (current < target) {
            return Math.min(target - current, delta);
        }
        else if (current > target) {
            return Math.max(target - current, -delta);
        }
        else {
            return target;
        }
    }

    public MovementSystem() {
        super(MovementComponent.class);
    }
    
    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);

        this.mm = engine.getMapper(MovementComponent.class);
    }
    
    @Override
    public void processEntity(int id, Context ctx) {
        MovementComponent mc = mm.get(id);
        // To get time from last frame 
        float deltaTime = ctx.deltaTime;

        float x = 0;
        float y = 0;

        if (InputHandler.getInstance().key(GLFW_KEY_W)) y -= 1;
        if (InputHandler.getInstance().key(GLFW_KEY_S)) y += 1;
        if (InputHandler.getInstance().key(GLFW_KEY_A)) x -= 1;
        if (InputHandler.getInstance().key(GLFW_KEY_D)) x += 1;

        // Handle acceleration
        if (x != 0 || y != 0) {
            // Accelerate when key is pressed
            mc.velocity.x += approach(mc.velocity.x, x * mc.speed, mc.acceleration * deltaTime);
            mc.velocity.y += approach(mc.velocity.y, y * mc.speed, mc.acceleration * deltaTime);
        }
        else {
            // Apply friction when no key is pressed
            mc.velocity.x += approach(mc.velocity.x, 0, mc.friction * deltaTime);
            mc.velocity.y += approach(mc.velocity.y, 0, mc.friction * deltaTime);
        }
    }
}
