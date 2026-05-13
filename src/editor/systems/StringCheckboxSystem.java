package editor.systems;

import client.components.TextComponent;
import client.systems.client.Context;
import editor.components.BooleanComponent;
import editor.components.EditorComponent;
import editor.util.TileRegistry;
import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.IteratingEntitySystem;

public class StringCheckboxSystem extends IteratingEntitySystem<Context> {
    private ComponentMapper<BooleanComponent> bfm;
    private ComponentMapper<TextComponent> tcm;
    private final EditorComponent editor;

    public StringCheckboxSystem(EditorComponent editor) {
        super(BooleanComponent.class, TextComponent.class);
        this.editor = editor;
    }

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);
        this.bfm = engine.getMapper(BooleanComponent.class);
        this.tcm = engine.getMapper(TextComponent.class);
    }

    @Override
    protected void processEntity(int entityId, Context ctx) {
        BooleanComponent bc = bfm.get(entityId);
        TextComponent tc = tcm.get(entityId);

        if (editor.currentTile == null || editor.currentTile < 0 || editor.currentTile >= TileRegistry.getTileCount()) {
            return;
        }

        boolean solid = TileRegistry.getTile(editor.currentTile).solid;
        bc.value = solid;
        tc.text = solid ? "Solid: ON" : "Solid: OFF";
    }
}