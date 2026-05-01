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
    private ComponentMapper<client.components.MovementComponent> movm;

    public AnimationSystem() {
        super(AnimationComponent.class, RenderComponent.class);
    }

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);

        this.tm = engine.getMapper(AnimationComponent.class);
        this.mm = engine.getMapper(RenderComponent.class);
        this.movm = engine.getMapper(client.components.MovementComponent.class);
    }

    @Override
    public void processEntity(int id, Context ctx) {
        AnimationComponent ac = tm.get(id);
        RenderComponent rc = mm.get(id);

        boolean moving = false;
        if (movm.get(id) != null) {
            client.components.MovementComponent mc = movm.get(id);
            if (mc.velocity.x != 0 || mc.velocity.y != 0) {
                moving = true;
                // y-down camera: velocity.y < 0 = moving UP, velocity.y > 0 = moving DOWN
                if      (mc.velocity.y < -10) ac.currentAnim = 3; // Up
                else if (mc.velocity.y >  10) ac.currentAnim = 0; // Down
                else if (mc.velocity.x < -10) ac.currentAnim = 1; // Left
                else if (mc.velocity.x >  10) ac.currentAnim = 2; // Right
            }
        } else {
            moving = true;
        }

        Animation animation = ac.animations[ac.currentAnim];
        if (animation.frameDuration == null) {
            rc.texture = animation.frames[0];
            return;
        }

        if (moving) {
            ac.offset += ctx.deltaTime;
        } else {
            ac.offset = 0; // Reset to the first frame (idle)
        }

        int index = (int) Math.floor(ac.offset / animation.frameDuration) % animation.frames.length;
        rc.texture = animation.frames[index];
    }
}
