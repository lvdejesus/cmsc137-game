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

        Animation animation = ac.animation;
        if (animation.frameDuration == null) {
            rc.texture = animation.frames[0];
        }

        float time = ctx.currentTime - ac.offset;
        if (time < 0) time = 0;
        
        int index = (int) Math.floor(time / animation.frameDuration) % animation.frames.length;
        rc.texture = animation.frames[index];
    }
}
