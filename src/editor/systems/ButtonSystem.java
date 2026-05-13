package editor.systems;

import client.components.ClickEvent;
import client.components.RenderComponent;
import client.components.TransformComponent;
import client.rendering.Anchor;
import client.systems.client.Context;
import editor.components.ButtonComponent;
import editor.components.EditorComponent;
import editor.util.LevelManager;
import editor.util.TileRegistry;
import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.Entity;
import framework.engine.IteratingEntitySystem;
import org.joml.Vector2f;

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
            case "save_grid" -> {
                String filename = LevelManager.showSaveDialog();
                if (filename != null) {
                    try {
                        int[][] grid = editor.toGridArray();
                        LevelManager.saveGrid(grid, filename);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            case "load_grid" -> {
                String filename = LevelManager.showLoadDialog();
                if (filename != null) {
                    int[][] grid = LevelManager.loadGrid(filename);
                    if (grid != null) {
                        editor.clearGrid();
                        for (int y = 0; y < grid.length; y++) {
                            for (int x = 0; x < grid[y].length; x++) {
                                int tileIndex = grid[y][x] - 1;
                                if (tileIndex >= 0 && tileIndex < editor.tiles.size()) {
                                    Entity<Context> tileEntity = engine.createEntity();
                                    TransformComponent tc = new TransformComponent(
                                        new Vector2f(x * 64.0f, y * 64.0f),
                                        new Vector2f(4.0f, 4.0f), Anchor.TOP_LEFT);
                                    RenderComponent rc = new RenderComponent(editor.tiles.get(tileIndex), 1);
                                    tileEntity.addComponent(rc);
                                    tileEntity.addComponent(tc);
                                    editor.setEntity(x, y, tileEntity, tileIndex);
                                }
                            }
                        }
                        editor.currentTile = null;
                    }
                }
            }
        }

        engine.removeComponent(entityId, ClickEvent.class);
    }
}