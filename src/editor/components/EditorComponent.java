package editor.components;

import client.rendering.Texture;
import client.systems.Context;
import framework.engine.Component;
import framework.engine.Entity;

import java.util.ArrayList;
import java.util.HashMap;

public class EditorComponent implements Component {
    public ArrayList<Texture> tiles = new ArrayList<>();
    public Integer currentTile = null;
    private final HashMap<IntPair, Entity<Context>> grid = new HashMap<>();

    public Entity<Context> getEntity(int x, int y) {
        return grid.get(new IntPair(x, y));
    }

    public void setEntity(int x, int y, Entity<Context> entity) {
        grid.put(new IntPair(x, y), entity);
    }

    record IntPair(int x, int y) {
    }
}