package editor.systems;

import client.components.ClickEvent;
import client.systems.client.Context;
import editor.components.TileGridComponent;
import editor.components.SidebarTileComponent;
import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.IteratingEntitySystem;

public class TileSystem extends IteratingEntitySystem<Context> {
    private ComponentMapper<SidebarTileComponent> tim;
    private ComponentMapper<ClickEvent> cem;

    private final TileGridComponent editor;

    public TileSystem(TileGridComponent editor) {
        super(SidebarTileComponent.class);

        this.editor = editor;
    }

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);

        this.tim = engine.getMapper(SidebarTileComponent.class);
        this.cem = engine.getMapper(ClickEvent.class);
    }

    @Override
    protected void processEntity(int entityId, Context ctx) {
        SidebarTileComponent tic = tim.get(entityId);
        ClickEvent ce = cem.get(entityId);

        if (ce != null) {
            editor.currentTile = tic.tileIndex;
        }
    }
}
