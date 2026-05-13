package client.systems.client;

import client.components.RenderComponent;

import client.components.AnimationComponent;

import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.IteratingEntitySystem;
import client.rendering.Animation;

public class AnimationSystem extends IteratingEntitySystem<Context> {
    private ComponentMapper<AnimationComponent> tm;
    private ComponentMapper<RenderComponent> mm;

    public AnimationSystem() {
        super(AnimationComponent.class, RenderComponent.class);
    }

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);

        this.tm = engine.getMapper(AnimationComponent.class);
        this.mm = engine.getMapper(RenderComponent.class);
    }

    @Override
    public void processEntity(int id, Context ctx) {
        AnimationComponent ac = tm.get(id);
        RenderComponent rc = mm.get(id);
        int frameOffset;
        int finalOffset;
        Animation animation = ac.animation;
        
        //Update current frame index
        
        if (animation.frameDuration == null || animation.frames.length <= 1) {
            rc.texture = animation.frames[0];
            ac.currentFrameIndex = 0;
            return;
        }

        int start = ac.startFrame;
        int end = ac.endFrame >= 0 ? ac.endFrame : animation.frames.length - 1;
        int totalFrames = Math.abs(end - start)+1;


            float time = ctx.currentTime - ac.offset;
            if (time < 0) time = 0;
            if(ac.loop){
                frameOffset = (int) Math.floor(time / animation.frameDuration) % totalFrames;
            }
            else{
                frameOffset = (int) Math.floor(time / animation.frameDuration);
                if(frameOffset >= totalFrames){
                    frameOffset = totalFrames - 1;
                }
            }

            
            finalOffset = (ac.reverse) ? (start - frameOffset) : (start + frameOffset);
            ac.currentFrameIndex = finalOffset;
            rc.texture = animation.frames[Math.min(finalOffset, animation.frames.length - 1)];
        }
}
