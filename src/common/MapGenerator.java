package common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class MapGenerator {

    public static final int GRID_SIZE = 300;
    public static final int START_X = 150;
    public static final int START_Y = 150;

    public static class RoomData {
        public String filename;
        public boolean isHallway;
        public int[][] grid;
        public int width, height;
        public double weight = 1.0;
        public List<DoorBlock> doorBlocks = new ArrayList<>();

        public RoomData(String filename, boolean isHallway, int[][] grid, int width, int height) {
            this.filename = filename;
            this.isHallway = isHallway;
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

    public static class OpenDoor {
        public DoorBlock block;
        public PlacedRoom parentRoom;

        public OpenDoor(DoorBlock block, PlacedRoom parentRoom) {
            this.block = block;
            this.parentRoom = parentRoom;
        }
    }

    public static int[][] generateMap(long seed) throws IOException {
        return generateMap(seed, 30, 100, true);
    }

    public static int[][] generateMap(long seed, int minRooms, int maxRooms) throws IOException {
        return generateMap(seed, minRooms, maxRooms, false);
    }

    public static int[][] generateMap(long seed, int minRooms, int maxRooms, boolean allowFallbackCapping) throws IOException {
        List<RoomData> rooms = loadAllRooms();

        for (RoomData room : rooms) {
            room.doorBlocks = findDoorBlocks(room.grid, room.width, room.height);
            room.weight = 1.0 + ((room.width * room.height) / 50.0) + (room.doorBlocks.size() * 2.0);
            if (room.doorBlocks.size() == 1) {
                room.weight += 10.0;
            }
        }

        return placeRoomsBacktracking(rooms, seed, minRooms, maxRooms, allowFallbackCapping);
    }

    private static int[][] placeRoomsBacktracking(List<RoomData> rooms, long seed, int minRooms, int maxRooms, boolean allowFallbackCapping) {
        Random rng = new Random(seed);

        List<RoomData> startRooms = new ArrayList<>();
        for (RoomData r : rooms) {
            if (r.doorBlocks.size() == 4) {
                startRooms.add(r);
            }
        }

        if (startRooms.isEmpty()) {
            return new int[0][0];
        }

        RoomData startRoom = startRooms.get(rng.nextInt(startRooms.size()));

        List<PlacedRoom> placedRooms = new ArrayList<>();
        List<OpenDoor> openDoors = new ArrayList<>();
        List<OpenDoor> cappedDoors = new ArrayList<>();

        PlacedRoom initialPlacement = new PlacedRoom(startRoom, START_X, START_Y);

        if (initialPlacement.offsetX < 0 || initialPlacement.offsetY < 0 ||
            initialPlacement.offsetX + startRoom.width > GRID_SIZE ||
            initialPlacement.offsetY + startRoom.height > GRID_SIZE) {
            return new int[0][0];
        }

        placedRooms.add(initialPlacement);
        for (DoorBlock db : startRoom.doorBlocks) {
            openDoors.add(new OpenDoor(db, initialPlacement));
        }

        boolean success = backtrack(placedRooms, openDoors, cappedDoors, rooms, rng, minRooms, maxRooms, allowFallbackCapping);

        if (success) {
            int[][] mapGrid = new int[GRID_SIZE][GRID_SIZE];

            // render into final grid
            for (PlacedRoom pr : placedRooms) {
                for (int ry = 0; ry < pr.room.height; ry++) {
                    for (int rx = 0; rx < pr.room.width; rx++) {
                        int tile = pr.room.grid[ry][rx];
                        if (tile != 0) {
                            mapGrid[pr.offsetY + ry][pr.offsetX + rx] = tile;
                        }
                    }
                }
            }

            // cap doors
            List<OpenDoor> allCapped = new ArrayList<>();
            allCapped.addAll(openDoors);
            allCapped.addAll(cappedDoors);

            for (OpenDoor od : allCapped) {
                for (DoorTile dt : od.block.tiles) {
                    int gx = od.parentRoom.offsetX + od.block.originX + dt.dx;
                    int gy = od.parentRoom.offsetY + od.block.originY + dt.dy;
                    if (gx >= 0 && gx < GRID_SIZE && gy >= 0 && gy < GRID_SIZE) {
                        mapGrid[gy][gx] = 1;
                    }
                }
            }

            // remove hallway doors
            cleanUpHallwayDoors(mapGrid, placedRooms);

            return mapGrid;
        }

        return new int[0][0];
    }

    private static void cleanUpHallwayDoors(int[][] mapGrid, List<PlacedRoom> placedRooms) {
        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                if (isDoorTile(mapGrid[y][x])) {
                    boolean allHallways = true;
                    int coverCount = 0;

                    for (PlacedRoom pr : placedRooms) {
                        int lx = x - pr.offsetX;
                        int ly = y - pr.offsetY;
                        if (lx >= 0 && lx < pr.room.width && ly >= 0 && ly < pr.room.height) {
                            if (isDoorTile(pr.room.grid[ly][lx])) {
                                coverCount++;
                                if (!pr.room.isHallway) {
                                    allHallways = false;
                                }
                            }
                        }
                    }

                    if (coverCount >= 2 && allHallways) {
                        mapGrid[y][x] = 0;
                    }
                }
            }
        }
    }

    private static boolean backtrack(List<PlacedRoom> placedRooms, List<OpenDoor> openDoors,
                                     List<OpenDoor> cappedDoors, List<RoomData> rooms,
                                     Random rng, int minRooms, int maxRooms, boolean allowFallbackCapping) {
        if (openDoors.isEmpty()) {
            return placedRooms.size() >= minRooms;
        }

        if (placedRooms.size() >= maxRooms) {
            return true;
        }

        OpenDoor targetDoor = openDoors.removeFirst();

        Map<RoomData, Double> scores = new HashMap<>();
        for (RoomData r : rooms) {
            double randomVal = rng.nextDouble();
            if (randomVal <= 0.0001) randomVal = 0.0001;
            scores.put(r, -Math.log(randomVal) / r.weight);
        }

        List<RoomData> randomizedRooms = new ArrayList<>(rooms);
        randomizedRooms.sort(Comparator.comparingDouble(scores::get));

        for (RoomData candidateRoom : randomizedRooms) {
            if (candidateRoom.filename.equals(targetDoor.parentRoom.room.filename)) {
                continue;
            }

            for (DoorBlock candidateDoor : candidateRoom.doorBlocks) {
                if (doorBlocksMatch(targetDoor.block, candidateDoor)) {

                    int[] offset = calculateOffset(targetDoor.block, targetDoor.parentRoom, candidateDoor);
                    PlacedRoom newPlacement = new PlacedRoom(candidateRoom, offset[0], offset[1]);

                    if (canPlaceRoom(newPlacement, placedRooms)) {
                        placedRooms.add(newPlacement);

                        List<OpenDoor> newOpenDoors = new ArrayList<>();
                        for (DoorBlock db : candidateRoom.doorBlocks) {
                            if (db != candidateDoor) {
                                newOpenDoors.add(new OpenDoor(db, newPlacement));
                            }
                        }

                        openDoors.addAll(newOpenDoors);

                        if (!hasBlockedDoors(placedRooms, openDoors)) {
                            if (backtrack(placedRooms, openDoors, cappedDoors, rooms, rng, minRooms, maxRooms, allowFallbackCapping)) {
                                return true;
                            }
                        }

                        openDoors.removeAll(newOpenDoors);
                        placedRooms.remove(placedRooms.size() - 1);
                    }
                }
            }
        }

        if (allowFallbackCapping) {
            cappedDoors.add(targetDoor);
            if (backtrack(placedRooms, openDoors, cappedDoors, rooms, rng, minRooms, maxRooms, allowFallbackCapping)) {
                return true;
            }
            cappedDoors.removeLast();
        }

        openDoors.addFirst(targetDoor);
        return false;
    }

    private static boolean canPlaceRoom(PlacedRoom pr, List<PlacedRoom> placedRooms) {
        RoomData room = pr.room;
        if (pr.offsetX < 0 || pr.offsetY < 0 ||
            pr.offsetX + room.width > GRID_SIZE || pr.offsetY + room.height > GRID_SIZE) {
            return false;
        }

        int x1 = pr.offsetX;
        int y1 = pr.offsetY;
        int w1 = room.width;
        int h1 = room.height;

        for (PlacedRoom other : placedRooms) {
            int x2 = other.offsetX;
            int y2 = other.offsetY;
            int w2 = other.room.width;
            int h2 = other.room.height;

            int overlapX = Math.max(0, Math.min(x1 + w1, x2 + w2) - Math.max(x1, x2));
            int overlapY = Math.max(0, Math.min(y1 + h1, y2 + h2) - Math.max(y1, y2));

            // only share door row/col
            if (overlapX > 1 && overlapY > 1) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasBlockedDoors(List<PlacedRoom> placedRooms, List<OpenDoor> openDoors) {
        for (OpenDoor od : openDoors) {
            for (DoorTile dt : od.block.tiles) {
                int gx = od.parentRoom.offsetX + od.block.originX + dt.dx;
                int gy = od.parentRoom.offsetY + od.block.originY + dt.dy;

                if (gx < 0 || gx >= GRID_SIZE || gy < 0 || gy >= GRID_SIZE) return true;

                // door tile in another room's bounds
                for (PlacedRoom pr : placedRooms) {
                    if (pr == od.parentRoom) continue;
                    if (isInside(gx, gy, pr)) return true;
                }

                // outside tile in another room's bounds
                int[][] dirs = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
                for (int[] d : dirs) {
                    int lx = od.block.originX + dt.dx;
                    int ly = od.block.originY + dt.dy;
                    int nx = lx + d[0];
                    int ny = ly + d[1];

                    // if this direction points outside the parent room
                    if (nx < 0 || nx >= od.parentRoom.room.width || ny < 0 || ny >= od.parentRoom.room.height) {
                        int mapGx = od.parentRoom.offsetX + nx;
                        int mapGy = od.parentRoom.offsetY + ny;

                        if (mapGx < 0 || mapGx >= GRID_SIZE || mapGy < 0 || mapGy >= GRID_SIZE) return true;

                        for (PlacedRoom pr : placedRooms) {
                            if (pr == od.parentRoom) continue;
                            if (isInside(mapGx, mapGy, pr)) return true; // Facing a solid wall!
                        }
                    }
                }
            }
        }
        return false;
    }

    private static boolean isInside(int x, int y, PlacedRoom pr) {
        return x >= pr.offsetX && x < pr.offsetX + pr.room.width &&
            y >= pr.offsetY && y < pr.offsetY + pr.room.height;
    }

    private static boolean doorBlocksMatch(DoorBlock a, DoorBlock b) {
        if (a.tiles.size() != b.tiles.size()) return false;
        Map<String, Integer> aMap = new HashMap<>();
        Map<String, Integer> bMap = new HashMap<>();
        for (DoorTile dt : a.tiles) aMap.put(dt.dx + "," + dt.dy, dt.tileId);
        for (DoorTile dt : b.tiles) bMap.put(dt.dx + "," + dt.dy, dt.tileId);
        return aMap.equals(bMap);
    }

    private static int[] calculateOffset(DoorBlock placedBlock, PlacedRoom placedRoom, DoorBlock newBlock) {
        int placedDoorWorldX = placedRoom.offsetX + placedBlock.originX;
        int placedDoorWorldY = placedRoom.offsetY + placedBlock.originY;
        int offsetX = placedDoorWorldX - newBlock.originX;
        int offsetY = placedDoorWorldY - newBlock.originY;
        return new int[]{offsetX, offsetY};
    }

    private static List<RoomData> loadAllRooms() throws IOException {
        List<RoomData> rooms = new ArrayList<>();
        Path roomsDir = Path.of("res/rooms");

        if (!Files.exists(roomsDir)) return rooms;

        List<String> filenames = new ArrayList<>();
        try (var stream = Files.list(roomsDir)) {
            stream.map(p -> p.getFileName().toString())
                .filter(f -> f.endsWith(".json"))
                .filter(f -> !f.startsWith("generated"))
                .forEach(filenames::add);
        }
        Collections.sort(filenames);

        for (String filename : filenames) {
            RoomLoader.Room room = RoomLoader.loadGrid(filename);
            int[][] grid = room.grid;

            boolean isHallway = room.isHallway;

            if (grid != null && grid.length > 0 && grid[0].length > 0) {
                rooms.add(new RoomData(filename, isHallway, grid, grid[0].length, grid.length));
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
        // TODO: use actual values
        return tileId >= 2 && tileId <= 5;
    }
}
