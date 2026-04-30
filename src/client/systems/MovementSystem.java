package client.systems;

import client.components.MovementComponent;
import client.components.player.PlayerStateComponent;
import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.EntitySystem;

import static org.lwjgl.glfw.GLFW.*;

public class MovementSystem extends EntitySystem<Context> {
    private ComponentMapper<MovementComponent> mm;
    private ComponentMapper<PlayerStateComponent> sm;

    private float approach(float current, float target, float max) {
        if (current < target) {
            return Math.min(current + max, target);
        }
        else if (current > target) {
            return Math.max(current - max, target);
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
        this.sm = engine.getMapper(PlayerStateComponent.class);
        this.mm = engine.getMapper(MovementComponent.class);
    }
    
    @Override
    public void processEntity(int id, Context ctx) {
        MovementComponent mc = mm.get(id);
        PlayerStateComponent state = sm.get(id);

        // To get time from last frame 
        float deltaTime = ctx.deltaTime;

        float x = 0;
        float y = 0;

        if (InputHandler.getInstance().key(GLFW_KEY_W)) y -= 1;
        if (InputHandler.getInstance().key(GLFW_KEY_S)) y += 1;
        if (InputHandler.getInstance().key(GLFW_KEY_A)) x -= 1;
        if (InputHandler.getInstance().key(GLFW_KEY_D)) x += 1;

        if(x != 0 || y !=0){

            // Change State to moving
            state.set(PlayerStateComponent.State.MOVING);
            
            // Normalize diagonal movement to prevent diagonal speedup
            if(x != 0 && y!=0){
                float len = (float) Math.sqrt((x*x) + (y*y));
                x /= len;
                y /= len;
            }
            
        }
        
        // When fully stopped
        else if (mc.velocity.lengthSquared() < 0.01f)
        {
            state.set(PlayerStateComponent.State.IDLE);
        }
        
        float targetx = mc.speed * x;
        float targety = mc.speed * y;

        // Handle Horizontal acceleration
        float xAccel;
        if (x==0){
            xAccel = mc.friction;
        }
        // When switching directions
        else if (Math.signum(x) != Math.signum(mc.velocity.x) && mc.velocity.x !=0){
            xAccel = mc.acceleration * 4.0f;
        }
        else{
            xAccel = mc.acceleration;
        }

        // Handle Vertical acceleration
        float yAccel;
        if (y==0){
            yAccel = mc.friction;
        }
        // When switching directions
        else if (Math.signum(y) != Math.signum(mc.velocity.y) && mc.velocity.y !=0){
            yAccel = mc.acceleration * 4.0f;
        }
        else{
            yAccel = mc.acceleration;
        }

        // Apply acceleration
        mc.velocity.x = approach(mc.velocity.x, targetx, xAccel * deltaTime);
        mc.velocity.y = approach(mc.velocity.y, targety, yAccel * deltaTime);
    }
}
