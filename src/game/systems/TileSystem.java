package game.systems;

import game.components.ClickEvent;
import game.components.EditorComponent;
import game.components.TileComponent;
import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.EntitySystem;

public class TileSystem extends EntitySystem<Context> {
    private ComponentMapper<TileComponent> tim;
    private ComponentMapper<ClickEvent> cem;

    private final EditorComponent editor;

    public TileSystem(EditorComponent editor) {
        super(TileComponent.class);

        this.editor = editor;
    }

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);

        this.tim = engine.getMapper(TileComponent.class);
        this.cem = engine.getMapper(ClickEvent.class);
    }

    @Override
    protected void processEntity(int entityId, Context ctx) {
        TileComponent tic = tim.get(entityId);
        ClickEvent ce = cem.get(entityId);

        if (ce != null) {
            editor.currentTile = tic.tile;
        }
    }
}
