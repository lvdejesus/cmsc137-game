package client.systems.client;

import client.components.MovementComponent;
import client.components.TransformComponent;

import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.IteratingEntitySystem;

public class PhysicsSystem extends IteratingEntitySystem<Context> {
    private ComponentMapper<TransformComponent> tm;
    private ComponentMapper<MovementComponent> mm;

    public PhysicsSystem() {
        super(TransformComponent.class, MovementComponent.class);
    }

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);

        this.tm = engine.getMapper(TransformComponent.class);
        this.mm = engine.getMapper(MovementComponent.class);
    }

    @Override
    public void processEntity(int id, Context ctx) {
        TransformComponent pos = tm.get(id);
        MovementComponent vel = mm.get(id);

        pos.position.fma(ctx.deltaTime, vel.velocity);
    }
}
