package editor.systems;

import client.components.ClickEvent;
import client.systems.client.Context;
import editor.components.ButtonComponent;
import editor.components.EditorComponent;
import editor.util.LevelManager;
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
                if (filename == null)
                    break;

                int[][] grid = LevelManager.loadGrid(filename);
                if (grid == null)
                    break;

                editor.clearGrid();
                for (int y = 0; y < grid.length; y++) {
                    for (int x = 0; x < grid[y].length; x++) {
                        int tileIndex = grid[y][x] - 1;
                        if (tileIndex < 0 || tileIndex >= editor.tiles.size()) {
                            continue;
                        }

                        EditorSystem.placeTile(engine, editor, x, y, tileIndex);
                    }
                }
                editor.currentTile = null;
            }
        }

        engine.removeComponent(entityId, ClickEvent.class);
    }
}