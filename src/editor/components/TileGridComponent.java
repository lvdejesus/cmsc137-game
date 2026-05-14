package editor.components;

import client.rendering.Texture;
import client.systems.client.Context;
import editor.util.TileRegistry;
import framework.engine.Component;
import framework.engine.Entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TileGridComponent implements Component {
    public static final int GRID_WIDTH = 20;
    public static final int GRID_HEIGHT = 15;

    public static class TileTexture {
        public TileRegistry.TileTextureType type;
        public List<Texture> textures;

        public TileTexture(TileRegistry.TileTextureType type, List<Texture> textures) {
            this.type = type;
            this.textures = textures;
        }
    }

    public List<TileTexture> tiles = new ArrayList<>();
    public Integer currentTile = null;
    private final Map<IntPair, Entity<Context>> entities = new HashMap<>();
    private final Map<IntPair, Integer> tileIndices = new HashMap<>();

    public Entity<Context> getEntity(int x, int y) {
        return entities.get(new IntPair(x, y));
    }

    public void setEntity(int x, int y, Entity<Context> entity, int tileIndex) {
        IntPair key = new IntPair(x, y);
        entities.put(key, entity);
        tileIndices.put(key, tileIndex);
    }

    public int[][] toGridArray() {
        int[][] grid = new int[GRID_HEIGHT][GRID_WIDTH];
        for (Map.Entry<IntPair, Integer> entry : tileIndices.entrySet()) {
            IntPair key = entry.getKey();
            grid[key.y()][key.x()] = entry.getValue() + 1;
        }
        return grid;
    }

    public void clearGrid() {
        for (Entity<Context> e : entities.values()) {
            e.destroy();
        }
        entities.clear();
        tileIndices.clear();
    }

    public record IntPair(int x, int y) {
    }
}