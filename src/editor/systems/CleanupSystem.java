package editor.systems;

import client.components.ClickEvent;
import client.systems.client.Context;
import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.IteratingEntitySystem;

public class CleanupSystem extends IteratingEntitySystem<Context> {
    private ComponentMapper<ClickEvent> cem;

    public CleanupSystem() {
        super(ClickEvent.class);
    }

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);

        this.cem = engine.getMapper(ClickEvent.class);
    }

    @Override
    protected void processEntity(int entityId, Context ctx) {
        engine.removeComponent(entityId, ClickEvent.class);
    }
}
