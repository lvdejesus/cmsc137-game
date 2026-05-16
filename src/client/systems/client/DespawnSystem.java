package client.systems.client;

import client.components.DespawnTimerComponent;
import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.IteratingEntitySystem;

public class DespawnSystem extends IteratingEntitySystem<Context> {
    private ComponentMapper<DespawnTimerComponent> dtm;

    public DespawnSystem() {
        super(DespawnTimerComponent.class);
    }

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);

        dtm = engine.getMapper(DespawnTimerComponent.class);
    }

    @Override
    protected void processEntity(int entityId, Context ctx) {
        if (ctx.currentTime > dtm.get(entityId).time) {
            engine.destroyEntity(entityId);
        }
    }
}
