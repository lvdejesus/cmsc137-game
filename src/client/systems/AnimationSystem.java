package client.systems;

import client.components.RenderComponent;
import client.components.AnimationComponent;

import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.EntitySystem;
import client.rendering.Animation;

public class AnimationSystem extends EntitySystem<Context> {
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

        int index = (int) Math.floor((ctx.currentTime - ac.offset) / animation.frameDuration) % animation.frames.length;
        rc.texture = animation.frames[index];
    }
}
