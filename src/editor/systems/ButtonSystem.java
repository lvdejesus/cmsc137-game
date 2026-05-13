package editor.systems;

import client.components.ClickEvent;
import client.systems.client.Context;
import editor.components.ButtonComponent;
import editor.components.EditorComponent;
import editor.util.TileRegistry;
import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.IteratingEntitySystem;

public class ButtonSystem extends IteratingEntitySystem<Context> {
    private ComponentMapper<ButtonComponent> bcm;
    private ComponentMapper<ClickEvent> cem;
    private final EditorComponent editor;

    public ButtonSystem(EditorComponent editor) {
        super(ButtonComponent.class, ClickEvent.class);
        this.editor = editor;
    }

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);
        this.bcm = engine.getMapper(ButtonComponent.class);
        this.cem = engine.getMapper(ClickEvent.class);
    }

    @Override
    protected void processEntity(int entityId, Context ctx) {
        ButtonComponent bc = bcm.get(entityId);
        ClickEvent ce = cem.get(entityId);

        if (ce == null) return;

        switch (bc.action) {
            case "toggle_solid" -> {
                if (editor.currentTile != null && editor.currentTile >= 0 && editor.currentTile < TileRegistry.getTileCount()) {
                    boolean currentSolid = TileRegistry.getTile(editor.currentTile).solid;
                    TileRegistry.setSolid(editor.currentTile, !currentSolid);
                }
            }
            case "save_tiles" -> {
                try {
                    TileRegistry.saveTiles();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        engine.removeComponent(entityId, ClickEvent.class);
    }
}