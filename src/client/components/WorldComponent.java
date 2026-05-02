package client.components;

import framework.engine.Component;

public class WorldComponent implements Component {
    public int[][] map;
    public int tileWidth = 32;
    public int tileHeight = 32;

    public WorldComponent(int[][] map) {
        this.map = map;
    }

    public boolean isWalkable(float x, float y) {
        int tx = (int) (x / tileWidth);
        int ty = (int) (y / tileHeight);

        if (tx < 0 || ty < 0 || tx >= map.length || ty >= map[0].length) {
            return false;
        }

        return map[tx][ty] == 0; // 0 is TILE_FLOOR
    }
}
