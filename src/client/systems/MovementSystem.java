package client.systems;

import client.components.AnimationComponent;
import client.components.MovementComponent;
import client.components.TransformComponent;
import client.components.player.PlayerStateComponent;
import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.EntitySystem;

import static org.lwjgl.glfw.GLFW.*;

public class MovementSystem extends EntitySystem<Context> {
    private ComponentMapper<MovementComponent> mm;
    private ComponentMapper<PlayerStateComponent> sm;
    private ComponentMapper<TransformComponent> tm;
    private ComponentMapper<AnimationComponent> am;

    private float approach(float current, float target, float max) {
        if (current < target) {
            return Math.min(current + max, target);
        } else if (current > target) {
            return Math.max(current - max, target);
        } else {
            return target;
        }
    }

    public MovementSystem() {
        super(MovementComponent.class, PlayerStateComponent.class);
    }

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);
        this.sm = engine.getMapper(PlayerStateComponent.class);
        this.mm = engine.getMapper(MovementComponent.class);
        this.tm = engine.getMapper(TransformComponent.class);
        this.am = engine.getMapper(AnimationComponent.class);
    }

    @Override
    public void processEntity(int id, Context ctx) {
        MovementComponent mc = mm.get(id);
        PlayerStateComponent state = sm.get(id);
        TransformComponent tc = tm.get(id);
        AnimationComponent ac = am.get(id);

        // To get time from last frame 
        float deltaTime = ctx.deltaTime;

        float x = 0;
        float y = 0;

        if (InputHandler.getInstance().key(GLFW_KEY_W)) y -= 1;
        if (InputHandler.getInstance().key(GLFW_KEY_S)) y += 1;
        if (InputHandler.getInstance().key(GLFW_KEY_A)) x -= 1;
        if (InputHandler.getInstance().key(GLFW_KEY_D)) x += 1;


        PlayerStateComponent.State nextState = PlayerStateComponent.State.IDLE;


        // Tilting
        int max = ac.animation.frames.length - 1;
        float animDuration = ctx.currentTime - ac.offset;
        float tiltThreshold = 0.3f; // Time threshold to switch to hard tilt

        if (x > 0) {
            if((state.current == PlayerStateComponent.State.TILTR || state.current == PlayerStateComponent.State.HARDTILTR) && animDuration >= tiltThreshold){
                nextState = PlayerStateComponent.State.HARDTILTR;
            }
            else if (state.current != PlayerStateComponent.State.HARDTILTR) {
                nextState = PlayerStateComponent.State.TILTR;
            }
        } else if (x < 0) {
            if((state.current == PlayerStateComponent.State.TILTL || state.current == PlayerStateComponent.State.HARDTILTL) && animDuration >= tiltThreshold){
                nextState = PlayerStateComponent.State.HARDTILTL;
            }
            else if (state.current != PlayerStateComponent.State.HARDTILTL) {
                nextState = PlayerStateComponent.State.TILTL;
            }
        } else if (y != 0) {
            nextState = PlayerStateComponent.State.MOVING;
        } else {
            nextState = PlayerStateComponent.State.IDLE;
        }



        if (state.current != nextState) {
            state.set(nextState);
            if(state.current != PlayerStateComponent.State.HARDTILTL && state.current != PlayerStateComponent.State.HARDTILTR){
                ac.offset = ctx.currentTime; // Used to track how long we've been in the current animation state
            }
        }
        
        switch (nextState) {
            case TILTR:
                ac.startFrame = 0;
                ac.endFrame = 2;
                ac.loop = false;
                ac.reverse = false;
                break;
            case TILTL:
                ac.startFrame = max; 
                ac.endFrame = max-3;
                ac.loop = false;
                ac.reverse = true;
                break;
            case HARDTILTR:
                ac.startFrame = 2;
                ac.endFrame = 3; 
                ac.loop = false;
                ac.reverse = false;
                break;
            case HARDTILTL:
                ac.startFrame = max - 3;
                ac.endFrame = max - 4;
                ac.loop = false;
                ac.reverse = true;
                break;
            default:
                ac.startFrame = 0;
                ac.endFrame = 0;
                ac.loop = true;
                break;
        }
        

        if (x != 0 || y != 0) {

            // Change State to moving


            // Normalize diagonal movement to prevent diagonal speedup
            if (x != 0 && y != 0) {
                float len = (float) Math.sqrt((x * x) + (y * y));
                x /= len;
                y /= len;
            }
        }

        // When fully stopped
        else if (y == 0 && x == 0) {
            state.set(PlayerStateComponent.State.IDLE);
        }

        float targetx = mc.speed * x;
        float targety = mc.speed * y;

        // Handle Horizontal acceleration
        float xAccel;
        if (x == 0) {
            xAccel = mc.friction;
        }
        // When switching directions
        else if (Math.signum(x) != Math.signum(mc.velocity.x) && mc.velocity.x != 0) {
            xAccel = mc.acceleration * 4.0f;
        } else {
            xAccel = mc.acceleration;
        }

        // Handle Vertical acceleration
        float yAccel;
        if (y == 0) {
            yAccel = mc.friction;
        }
        // When switching directions
        else if (Math.signum(y) != Math.signum(mc.velocity.y) && mc.velocity.y != 0) {
            yAccel = mc.acceleration * 4.0f;
        } else {
            yAccel = mc.acceleration;
        }

        // Apply acceleration
        mc.velocity.x = approach(mc.velocity.x, targetx, xAccel * deltaTime);
        mc.velocity.y = approach(mc.velocity.y, targety, yAccel * deltaTime);
    }
}
