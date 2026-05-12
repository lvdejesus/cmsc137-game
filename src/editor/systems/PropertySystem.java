package editor.systems;

import client.components.ClickEvent;
import client.components.TextComponent;
import client.components.TransformComponent;
import client.systems.Context;
import editor.components.EditorComponent;
import editor.util.TileRegistry;
import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.EntitySystem;
import org.joml.Vector4f;
import client.rendering.Font;

public class PropertySystem extends EntitySystem<Context> {
    private ComponentMapper<ClickEvent> cem;
    private ComponentMapper<TextComponent> tcm;
    private ComponentMapper<TransformComponent> tfm;
    private final EditorComponent editor;
    private final Font font;
    private Integer lastTile = null;

    public PropertySystem(EditorComponent editor, Font font) {
        super(ClickEvent.class);
        this.editor = editor;
        this.font = font;
    }

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);
        this.cem = engine.getMapper(ClickEvent.class);
        this.tcm = engine.getMapper(TextComponent.class);
        this.tfm = engine.getMapper(TransformComponent.class);
    }

    @Override
    public void update(Context ctx) {
        super.update(ctx);
        
        // Update solid text when tile selection changes
        if (editor.currentTile != null && !editor.currentTile.equals(lastTile)) {
            lastTile = editor.currentTile;
            updateSolidText();
        }
    }

    @Override
    protected void processEntity(int entityId, Context ctx) {
        ClickEvent ce = cem.get(entityId);
        if (ce == null) return;

        TransformComponent tc = tfm.get(entityId);

        // Solid toggle at (300, 12)
        if (tc.position.y == 12 && ce.x >= 300 && ce.x <= 400 && ce.y >= 12 && ce.y <= 42) {
            if (editor.currentTile != null && editor.currentTile >= 0 && editor.currentTile < TileRegistry.getTileCount()) {
                boolean currentSolid = TileRegistry.getTile(editor.currentTile).solid;
                TileRegistry.setSolid(editor.currentTile, !currentSolid);
                
                // Update the text
                updateSolidText();
            }
            engine.removeComponent(entityId, ClickEvent.class);
            return;
        }

        // Save button at (300, 52)
        if (tc.position.y == 52 && ce.x >= 300 && ce.x <= 360 && ce.y >= 52 && ce.y <= 82) {
            try {
                TileRegistry.saveTiles();
            } catch (Exception e) {
                e.printStackTrace();
            }
            engine.removeComponent(entityId, ClickEvent.class);
        }
    }

    private void updateSolidText() {
        if (editor.currentTile == null) return;
        
        int tileIndex = editor.currentTile;
        if (tileIndex < 0 || tileIndex >= TileRegistry.getTileCount()) return;
        
        boolean solid = TileRegistry.getTile(tileIndex).solid;
        
        // Find and update the solid text entity
        long[] bitsets = getBitsets();
        int entityMax = getEntityMax();
        int textIndex = getComponentIndex(TextComponent.class);
        long textMask = 1L << textIndex;
        
        for (int i = 0; i < entityMax; i++) {
            if ((bitsets[i] & textMask) == textMask) {
                TextComponent tc = tcm.get(i);
                if (tc.text != null && tc.text.startsWith("Solid:")) {
                    tc.text = solid ? "Solid: ON" : "Solid: OFF";
                    break;
                }
            }
        }
    }
}