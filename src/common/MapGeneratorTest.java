package common;

import java.io.IOException;

public class MapGeneratorTest {
    public static void main(String[] args) {
        try {
            long seed = args.length > 0 ? Long.parseLong(args[0]) : 42;
            int[][] map = MapGenerator.generateMap(seed).grid;

            System.out.println("Map size: " + map[0].length + "x" + map.length);
            for (var row : map) {
                for (var i : row) {
                    System.out.print(i);
                }
                System.out.print("\n");
            }

            int nonZero = 0;
            for (int y = 0; y < map.length; y++) {
                for (int x = 0; x < map[0].length; x++) {
                    if (map[y][x] != 0) nonZero++;
                }
            }
            System.out.println("Non-zero tiles: " + nonZero);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}