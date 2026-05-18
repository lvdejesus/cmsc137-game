package client.systems.client;

import client.components.DoorComponent;
import client.components.RenderComponent;
import client.systems.client.Context;
import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.IteratingEntitySystem;

public class DoorAnimatorSystem extends IteratingEntitySystem<Context> {
    private ComponentMapper<DoorComponent> doorM;
    private ComponentMapper<RenderComponent> rcM;

    public DoorAnimatorSystem() {
        super(DoorComponent.class, RenderComponent.class);
    }

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);
        doorM = engine.getMapper(DoorComponent.class);
        rcM = engine.getMapper(RenderComponent.class);
    }

    @Override
    protected void processEntity(int id, Context ctx) {
        DoorComponent dc = doorM.get(id);
        RenderComponent rc = rcM.get(id);
        if (dc.frames == null) return;

        int frame;
        if (dc.state == DoorComponent.State.CLOSED) {
            frame = 0;
        } else if (dc.state == DoorComponent.State.OPEN) {
            frame = dc.frames.length - 1;
        } else {
            dc.animTimer += ctx.deltaTime;
            frame = Math.min((int) (dc.animTimer / 0.1f), dc.frames.length - 1);
        }

        if (frame >= 0 && frame < dc.frames.length) {
            rc.texture = dc.frames[frame];
        }
    }
}
