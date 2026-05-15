package common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class MapGenerator {
    public static class RoomData {
        public String filename;
        public int[][] grid;
        public int width, height;
        public List<DoorBlock> doorBlocks = new ArrayList<>();

        public RoomData(String filename, int[][] grid, int width, int height) {
            this.filename = filename;
            this.grid = grid;
            this.width = width;
            this.height = height;
        }
    }

    public static class DoorBlock {
        public List<DoorTile> tiles = new ArrayList<>();
        public int originX, originY;
    }

    public static class DoorTile {
        public int dx, dy;
        public int tileId;

        public DoorTile(int dx, int dy, int tileId) {
            this.dx = dx;
            this.dy = dy;
            this.tileId = tileId;
        }
    }

    public static class PlacedRoom {
        public RoomData room;
        public int offsetX, offsetY;

        public PlacedRoom(RoomData room, int offsetX, int offsetY) {
            this.room = room;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }
    }

    public static int[][] generateMap(long seed) throws IOException {
        return generateMap(seed, -1);
    }

    public static int[][] generateMap(long seed, int maxRooms) throws IOException {
        List<TileDefinition> tiles = TileLoader.loadTiles();
        List<RoomData> rooms = loadAllRooms();

        for (RoomData room : rooms) {
            room.doorBlocks = findDoorBlocks(room.grid, room.width, room.height);
        }

        return placeRoomsDeterministic(rooms, seed, maxRooms);
    }

    private static List<RoomData> loadAllRooms() throws IOException {
        List<RoomData> rooms = new ArrayList<>();
        Path roomsDir = Path.of("res/rooms");

        List<String> filenames = new ArrayList<>();
        try (var stream = Files.list(roomsDir)) {
            stream.map(p -> p.getFileName().toString())
                .filter(f -> f.endsWith(".json"))
                .filter(f -> !f.startsWith("generated"))
                .forEach(filenames::add);
        }
        Collections.sort(filenames);

        for (String filename : filenames) {
            int[][] grid = RoomLoader.loadGrid(filename);
            if (grid != null && grid.length > 0 && grid[0].length > 0) {
                rooms.add(new RoomData(filename, grid, grid[0].length, grid.length));
            }
        }
        return rooms;
    }

    private static List<DoorBlock> findDoorBlocks(int[][] grid, int width, int height) {
        boolean[][] visited = new boolean[height][width];
        List<DoorBlock> blocks = new ArrayList<>();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (!visited[y][x] && isDoorTile(grid[y][x])) {
                    DoorBlock block = new DoorBlock();
                    floodFillDoors(grid, width, height, x, y, visited, block);
                    if (!block.tiles.isEmpty()) {
                        block.originX = block.tiles.get(0).dx;
                        block.originY = block.tiles.get(0).dy;
                        for (DoorTile dt : block.tiles) {
                            dt.dx -= block.originX;
                            dt.dy -= block.originY;
                        }
                        blocks.add(block);
                    }
                }
            }
        }
        return blocks;
    }

    private static void floodFillDoors(int[][] grid, int width, int height,
                                       int x, int y, boolean[][] visited, DoorBlock block) {
        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{x, y});
        visited[y][x] = true;

        while (!queue.isEmpty()) {
            int[] curr = queue.poll();
            int cx = curr[0], cy = curr[1];

            block.tiles.add(new DoorTile(cx, cy, grid[cy][cx]));

            int[][] dirs = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
            for (int[] d : dirs) {
                int nx = cx + d[0];
                int ny = cy + d[1];
                if (nx >= 0 && nx < width && ny >= 0 && ny < height &&
                    !visited[ny][nx] && isDoorTile(grid[ny][nx])) {
                    visited[ny][nx] = true;
                    queue.add(new int[]{nx, ny});
                }
            }
        }
    }

    private static boolean isDoorTile(int tileId) {
        return tileId >= 2 && tileId <= 5;
    }

    private static void markOccupiedTiles(RoomData room, int offsetX, int offsetY, Set<String> occupied) {
        for (int ry = 0; ry < room.height; ry++) {
            for (int rx = 0; rx < room.width; rx++) {
                int tileId = room.grid[ry][rx];
                if (tileId == 1) {
                    occupied.add((offsetX + rx) + "," + (offsetY + ry));
                }
            }
        }
    }

    private static int[][] placeRoomsDeterministic(List<RoomData> rooms, long seed, int maxRooms) {
        Random rng = new Random(seed);

        List<RoomData> placeableRooms = new ArrayList<>();
        for (RoomData r : rooms) {
            if (!r.doorBlocks.isEmpty()) {
                placeableRooms.add(r);
            }
        }

        if (placeableRooms.isEmpty()) {
            return new int[0][0];
        }

        Collections.shuffle(placeableRooms, rng);

        List<PlacedRoom> placed = new ArrayList<>();
        Set<String> occupied = new HashSet<>();

        int startIndex = rng.nextInt(placeableRooms.size());
        RoomData first = placeableRooms.get(startIndex);

        placed.add(new PlacedRoom(first, 0, 0));
        markOccupiedTiles(first, 0, 0, occupied);

        List<RoomData> toPlace = new ArrayList<>();
        int targetCount = (maxRooms > 0) ? maxRooms : 20;

        for (int i = 0; i < targetCount; i++) {
            toPlace.add(placeableRooms.get(rng.nextInt(placeableRooms.size())));
        }

        int attempts = 0;
        int maxAttempts = targetCount * 10;

        while (attempts < maxAttempts && !toPlace.isEmpty()) {
            int roomIdx = rng.nextInt(toPlace.size());
            RoomData room = toPlace.remove(roomIdx);

            PlacedRoom placedRoom = tryPlaceRoom(room, placed, occupied, rng);

            if (placedRoom != null) {
                placed.add(placedRoom);
                markOccupiedTiles(room, placedRoom.offsetX, placedRoom.offsetY, occupied);
            } else {
                attempts++;
            }
        }

        if (placed.isEmpty()) {
            return new int[0][0];
        }

        int minX = 0, maxX = 0, minY = 0, maxY = 0;
        for (PlacedRoom pr : placed) {
            minX = Math.min(minX, pr.offsetX);
            maxX = Math.max(maxX, pr.offsetX + pr.room.width);
            minY = Math.min(minY, pr.offsetY);
            maxY = Math.max(maxY, pr.offsetY + pr.room.height);
        }

        int gridWidth = maxX - minX;
        int gridHeight = maxY - minY;
        int[][] result = new int[gridHeight][gridWidth];

        for (PlacedRoom pr : placed) {
            for (int ry = 0; ry < pr.room.height; ry++) {
                for (int rx = 0; rx < pr.room.width; rx++) {
                    int gx = pr.offsetX + rx - minX;
                    int gy = pr.offsetY + ry - minY;
                    if (result[gy][gx] == 0) {
                        result[gy][gx] = pr.room.grid[ry][rx];
                    }
                }
            }
        }

        return result;
    }

    private static PlacedRoom tryPlaceRoom(RoomData room, List<PlacedRoom> placed,
                                           Set<String> occupied, Random rng) {

        List<DoorBlockPair> validPairs = new ArrayList<>();

        for (PlacedRoom pr : placed) {
            if (pr.room.filename.equals(room.filename)) continue;

            for (DoorBlock blockA : room.doorBlocks) {
                for (DoorBlock blockB : pr.room.doorBlocks) {
                    if (doorBlocksMatch(blockA, blockB)) {
                        validPairs.add(new DoorBlockPair(blockA, blockB, pr));
                    }
                }
            }
        }

        Collections.shuffle(validPairs, rng);

        for (DoorBlockPair pair : validPairs) {
            int[] offset = calculateOffset(pair.placedBlock, pair.placedRoom, pair.newBlock);
            if (offset != null && !hasSolidCollision(room, offset[0], offset[1], occupied)) {
                return new PlacedRoom(room, offset[0], offset[1]);
            }
        }

        return null;
    }

    private static boolean doorBlocksMatch(DoorBlock a, DoorBlock b) {
        if (a.tiles.size() != b.tiles.size()) return false;

        Map<String, Integer> aMap = new HashMap<>();
        Map<String, Integer> bMap = new HashMap<>();

        for (DoorTile dt : a.tiles) {
            aMap.put(dt.dx + "," + dt.dy, dt.tileId);
        }
        for (DoorTile dt : b.tiles) {
            bMap.put(dt.dx + "," + dt.dy, dt.tileId);
        }

        return aMap.equals(bMap);
    }

    private static int[] calculateOffset(DoorBlock placedBlock, PlacedRoom placedRoom, DoorBlock newBlock) {
        int placedDoorWorldX = placedRoom.offsetX + placedBlock.originX;
        int placedDoorWorldY = placedRoom.offsetY + placedBlock.originY;

        int offsetX = placedDoorWorldX - newBlock.originX;
        int offsetY = placedDoorWorldY - newBlock.originY;

        return new int[]{offsetX, offsetY};
    }

    private static boolean hasSolidCollision(RoomData room, int offsetX, int offsetY, Set<String> occupied) {
        for (int ry = 0; ry < room.height; ry++) {
            for (int rx = 0; rx < room.width; rx++) {
                if (room.grid[ry][rx] == 1) {
                    String key = (offsetX + rx) + "," + (offsetY + ry);
                    if (occupied.contains(key)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static class DoorBlockPair {
        DoorBlock newBlock;
        DoorBlock placedBlock;
        PlacedRoom placedRoom;

        DoorBlockPair(DoorBlock newBlock, DoorBlock placedBlock, PlacedRoom placedRoom) {
            this.newBlock = newBlock;
            this.placedBlock = placedBlock;
            this.placedRoom = placedRoom;
        }
    }
}