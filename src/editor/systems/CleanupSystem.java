package editor.systems;

import client.components.ClickEvent;
import client.systems.Context;
import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.EntitySystem;

public class CleanupSystem extends EntitySystem<Context> {
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
