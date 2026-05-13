package client.systems.client.player;

import client.components.AnimationComponent;
import client.components.player.MovementInputComponent;
import client.components.player.PlayerStateComponent;
import client.systems.client.Context;
import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.IteratingEntitySystem;

public class PlayerTiltSystem extends IteratingEntitySystem<Context> {
    private ComponentMapper<MovementInputComponent> mim;
    private ComponentMapper<PlayerStateComponent> sm;
    private ComponentMapper<AnimationComponent> am;

    public PlayerTiltSystem() {
        super(MovementInputComponent.class, PlayerStateComponent.class);
    }

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);
        this.sm = engine.getMapper(PlayerStateComponent.class);
        this.mim = engine.getMapper(MovementInputComponent.class);
        this.am = engine.getMapper(AnimationComponent.class);
    }

    @Override
    public void processEntity(int id, Context ctx) {
        MovementInputComponent mic = mim.get(id);
        PlayerStateComponent state = sm.get(id);
        AnimationComponent ac = am.get(id);

        float x = mic.x;
        float y = mic.y;

        PlayerStateComponent.State nextState = PlayerStateComponent.State.IDLE;

        // Tilting
        int max = ac.animation.frames.length - 1;
        float animDuration = ctx.currentTime - ac.offset;
        float tiltThreshold = 0.3f; // Time threshold to switch to hard tilt

        if (x > 0) {
            if ((state.current == PlayerStateComponent.State.TILTR || state.current == PlayerStateComponent.State.HARDTILTR) && animDuration >= tiltThreshold) {
                nextState = PlayerStateComponent.State.HARDTILTR;
            } else if (state.current != PlayerStateComponent.State.HARDTILTR) {
                nextState = PlayerStateComponent.State.TILTR;
            }
        } else if (x < 0) {
            if ((state.current == PlayerStateComponent.State.TILTL || state.current == PlayerStateComponent.State.HARDTILTL) && animDuration >= tiltThreshold) {
                nextState = PlayerStateComponent.State.HARDTILTL;
            } else if (state.current != PlayerStateComponent.State.HARDTILTL) {
                nextState = PlayerStateComponent.State.TILTL;
            }
        } else if (y != 0) {
            nextState = PlayerStateComponent.State.MOVING;
        }
        else{
            if(state.current != PlayerStateComponent.State.IDLE){
                nextState = PlayerStateComponent.State.IDLE;
            }   
        }


        // For playing animation to return to idle
        boolean returnFromRight = (state.current == PlayerStateComponent.State.TILTR || state.current == PlayerStateComponent.State.HARDTILTR);
        boolean returnFromLeft = (state.current == PlayerStateComponent.State.TILTL || state.current == PlayerStateComponent.State.HARDTILTL);
        int currentFrame = ac.currentFrameIndex;
        
        if (state.current != nextState) {
            state.set(nextState);
            if (state.current != PlayerStateComponent.State.HARDTILTL && state.current != PlayerStateComponent.State.HARDTILTR) {
                ac.offset = ctx.currentTime; // Used to track how long we've been in the current animation state
                int releaseFrame = ac.currentFrameIndex; // Frame of animation before changing state
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
                ac.endFrame = max - 1;
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
                ac.startFrame = max - 1;
                ac.endFrame = max - 2;
                ac.loop = false;
                ac.reverse = true;
                break;
            case IDLE:
                if (returnFromRight){
                    ac.startFrame = currentFrame;
                    ac.endFrame = 1;
                    ac.loop = false;
                    ac.reverse = true;
                }
                else if (returnFromLeft) {
                    ac.startFrame = currentFrame;
                    ac.endFrame = max;
                    ac.loop = false;
                    ac.reverse = false;
                }
                // playes after returning from hard tilt, so just snap back to idle
                else {
                    ac.startFrame = 0;
                    ac.endFrame = 0;
                    ac.loop = false;
                    ac.reverse = false;
                }
                break;
            }
        }
        // snap back to idle
        if (state.current == PlayerStateComponent.State.IDLE && !ac.loop) {
            if (
                (!ac.reverse && ac.currentFrameIndex >= ac.endFrame) ||
                (ac.reverse && ac.currentFrameIndex <= ac.endFrame)
            ) {
                ac.startFrame = 0;
                ac.endFrame = 0;
                ac.loop = true; // Lock into a looping idle state
                ac.reverse = false;
            }
        }

    }
}
