package client.systems;

import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.EntitySystem;
import client.components.bullet.BulletComponent;
import client.systems.Context;

public class BulletSystem extends EntitySystem<Context> {
    private ComponentMapper<BulletComponent> bm;
    private Engine<Context> engine;

    public BulletSystem() {
        super(BulletComponent.class);
    }

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);
        this.engine = engine;
        this.bm = engine.getMapper(BulletComponent.class);
    }

    @Override
    public void processEntity(int id, Context ctx) {
        BulletComponent bullet = bm.get(id);

        bullet.age += ctx.deltaTime;

        if (bullet.age >= bullet.lifetime) {
            engine.destroyEntity(id);
        }
    }
}