package client.util;

import java.util.Random;

public class DungeonGenerator {
    public static final int TILE_FLOOR = 0;
    public static final int TILE_WALL = 1;

    private int width;
    private int height;
    private int[][] map;
    private Random random = new Random();

    public DungeonGenerator(int width, int height) {
        this.width = width;
        this.height = height;
        this.map = new int[width][height];
    }

    public int[][] generate() {
        // 1. Fill with walls
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                map[x][y] = TILE_WALL;
            }
        }

        // 2. Carve out rooms
        int roomCount = 5 + random.nextInt(5);
        for (int i = 0; i < roomCount; i++) {
            int rw = 4 + random.nextInt(6);
            int rh = 4 + random.nextInt(6);
            int rx = 1 + random.nextInt(width - rw - 2);
            int ry = 1 + random.nextInt(height - rh - 2);

            carveRoom(rx, ry, rw, rh);
        }

        return map;
    }

    private void carveRoom(int x, int y, int w, int h) {
        for (int ix = x; ix < x + w; ix++) {
            for (int iy = y; iy < y + h; iy++) {
                map[ix][iy] = TILE_FLOOR;
            }
        }
    }
}
