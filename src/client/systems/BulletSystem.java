package client.systems;

import client.components.BulletComponent;
import client.components.BulletTagComponent;
import client.components.TransformComponent;
import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.EntitySystem;

public class BulletSystem extends EntitySystem<Context> {
    private static final float BOUNDS = 2000f;

    private ComponentMapper<BulletComponent>   bm;
    private ComponentMapper<TransformComponent> tm;

    public BulletSystem() {
        super(BulletComponent.class, BulletTagComponent.class, TransformComponent.class);
    }

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);
        this.bm = engine.getMapper(BulletComponent.class);
        this.tm = engine.getMapper(TransformComponent.class);
    }

    @Override
    public void processEntity(int id, Context ctx) {
        BulletComponent  bc = bm.get(id);
        TransformComponent tc = tm.get(id);

        // Move bullet
        tc.position.x += bc.velocity.x * ctx.deltaTime;
        tc.position.y += bc.velocity.y * ctx.deltaTime;

        // Destroy when out of bounds
        if (tc.position.x < -BOUNDS || tc.position.x > BOUNDS
         || tc.position.y < -BOUNDS || tc.position.y > BOUNDS) {
            engine.destroyEntity(id);
        }
    }
}
