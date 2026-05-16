package common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class MapGenerator {

    public static final int GRID_SIZE = 300;
    public static final int START_X = 150;
    public static final int START_Y = 150;

    private static final int INF = 100000000;

    public static class MapResult {
        public int[][] grid;
        public int[][][] closedAreas; // Array of closed areas, each area is an array of [x, y] coordinates
        public int[][] areaLookup;    // [y][x] mapping to the index in closedAreas (-1 if not in a closed area)
        public Map<Integer, Set<Integer>> adjacencyList; // Maps area index to a set of connected area indices
        public int bossRoomIndex = -1; // Index of the boss room
        public PlacedRoom bossRoom;
        public int startRoom;

        public MapResult(int[][] grid, int[][][] closedAreas, List<PlacedRoom> placedRooms) {
            this.grid = grid;
            this.closedAreas = closedAreas;
            this.areaLookup = new int[GRID_SIZE][GRID_SIZE];
            this.adjacencyList = new HashMap<>();

            // Initialize with -1 to indicate no closed area
            for (int i = 0; i < GRID_SIZE; i++) {
                Arrays.fill(this.areaLookup[i], -1);
            }

            // Map each coordinate back to its area index
            for (int areaIdx = 0; areaIdx < closedAreas.length; areaIdx++) {
                this.adjacencyList.put(areaIdx, new HashSet<>());
                for (int[] coord : closedAreas[areaIdx]) {
                    int x = coord[0];
                    int y = coord[1];
                    this.areaLookup[y][x] = areaIdx;
                }
            }

            // Find boss room from placed rooms
            if (placedRooms != null) {
                for (PlacedRoom pr : placedRooms) {
                    if (pr.room.isBoss) {
                        // Find the center of the boss room
                        int bossCenterX = pr.offsetX + pr.room.width / 2;
                        int bossCenterY = pr.offsetY + pr.room.height / 2;
                        this.bossRoomIndex = getAreaIndex(bossCenterX, bossCenterY);
                        this.bossRoom = pr;
                        break;
                    }
                }
            }

            startRoom = this.areaLookup[155][155];

            computeAdjacency();
        }

        public int[] findMaxShortestPath() {
            int v = closedAreas.length;

            // initialize distance matrix
            int[][] dist = new int[v][v];

            for (int[] row : dist) {
                Arrays.fill(row, INF);
            }
            for (int i = 0; i < v; i++) {
                dist[i][i] = 0;
            }

            for (Map.Entry<Integer, Set<Integer>> entry : adjacencyList.entrySet()) {
                int u = entry.getKey();
                for (int w : entry.getValue()) {
                    dist[u][w] = 1;
                }
            }

            for (int k = 0; k < v; k++) {
                for (int i = 0; i < v; i++) {
                    for (int j = 0; j < v; j++) {
                        if (dist[i][k] < INF && dist[k][j] < INF) {
                            dist[i][j] = Math.min(dist[i][j], dist[i][k] + dist[k][j]);
                        }
                    }
                }
            }

            List<Integer> candidates = new ArrayList<>();
            for (int i = 0; i < v; i++) {
                if (i == startRoom || i == bossRoomIndex) continue;
                candidates.add(i);
            }
            return getPath(candidates, dist);
        }

        private int[] getPath(List<Integer> candidates, int[][] dist) {
            int n = candidates.size();

            int startIndex = startRoom;
            int maxPathLength = -1;
            int[] path  = null;

            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    for (int k = j + 1; k < n; k++) {
                        int a = candidates.get(i);
                        int b = candidates.get(j);
                        int c = candidates.get(k);

                        int[][] permutations = {
                            {a, b, c}, {a, c, b},
                            {b, a, c}, {b, c, a},
                            {c, a, b}, {c, b, a}
                        };

                        int minDistanceForTriplet = INF;

                        int[] minTriplet = null;

                        for (int[] p : permutations) {
                            int currentDist = dist[startIndex][p[0]] + dist[p[0]][p[1]] + dist[p[1]][p[2]];
                            if (currentDist < minDistanceForTriplet) {
                                minTriplet = p;
                                minDistanceForTriplet = currentDist;
                            }
                        }

                        if (minDistanceForTriplet < INF) {
                            if (minDistanceForTriplet > maxPathLength) {
                                maxPathLength = minDistanceForTriplet;
                                path = minTriplet;
                            }
                        }
                    }
                }
            }
            return path;
        }

        private void computeAdjacency() {
            int[][] dirs = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};

            for (int y = 0; y < GRID_SIZE; y++) {
                for (int x = 0; x < GRID_SIZE; x++) {
                    // If we find a door tile
                    if (isDoorTile(grid[y][x])) {
                        Set<Integer> neighboringAreas = new HashSet<>();

                        // Check neighbors of the door
                        for (int[] d : dirs) {
                            int nx = x + d[0];
                            int ny = y + d[1];

                            int areaIdx = getAreaIndex(nx, ny);
                            if (areaIdx != -1) {
                                neighboringAreas.add(areaIdx);
                            }
                        }

                        // If the door touches 2 or more different areas, they are connected
                        if (neighboringAreas.size() >= 2) {
                            List<Integer> areas = new ArrayList<>(neighboringAreas);
                            for (int i = 0; i < areas.size(); i++) {
                                for (int j = i + 1; j < areas.size(); j++) {
                                    int a = areas.get(i);
                                    int b = areas.get(j);
                                    adjacencyList.get(a).add(b);
                                    adjacencyList.get(b).add(a);
                                }
                            }
                        }
                    }
                }
            }
        }

        /**
         * Returns the index of the closed room at the given coordinates.
         * @return The index in closedAreas, or -1 if the coordinate is not inside a closed area.
         */
        public int getAreaIndex(int x, int y) {
            if (x < 0 || x >= GRID_SIZE || y < 0 || y >= GRID_SIZE) {
                return -1;
            }
            return areaLookup[y][x];
        }

        /**
         * Returns a set of area indices that are separated from the given area only by a door.
         */
        public Set<Integer> getNeighboringAreas(int areaIndex) {
            return adjacencyList.getOrDefault(areaIndex, Collections.emptySet());
        }
    }

    public static class RoomData {
        public String filename;
        public boolean isHallway;
        public boolean isBoss;
        public int[][] grid;
        public int width, height;
        public double weight = 1.0;
        public List<DoorBlock> doorBlocks = new ArrayList<>();

        public RoomData(String filename, boolean isHallway, boolean isBoss, int[][] grid, int width, int height) {
            this.filename = filename;
            this.isHallway = isHallway;
            this.isBoss = isBoss;
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

    public static MapResult generateMap(long seed) throws IOException {
        return generateMap(seed, 30, 50, true);
    }

    public static MapResult generateMap(long seed, int minRooms, int maxRooms) throws IOException {
        return generateMap(seed, minRooms, maxRooms, false);
    }

    public static MapResult generateMap(long seed, int minRooms, int maxRooms, boolean allowFallbackCapping) throws IOException {
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

    private static MapResult placeRoomsBacktracking(List<RoomData> rooms, long seed, int minRooms, int maxRooms, boolean allowFallbackCapping) {
        Random rng = new Random(seed);

        List<RoomData> startRooms = new ArrayList<>();
        for (RoomData r : rooms) {
            if (r.doorBlocks.size() == 4 && !r.isBoss) { // Don't start with a boss room
                startRooms.add(r);
            }
        }

        if (startRooms.isEmpty()) {
            return new MapResult(new int[0][0], new int[0][0][], null);
        }

        RoomData startRoom = startRooms.get(rng.nextInt(startRooms.size()));

        List<PlacedRoom> placedRooms = new ArrayList<>();
        List<OpenDoor> openDoors = new ArrayList<>();
        List<OpenDoor> cappedDoors = new ArrayList<>();

        PlacedRoom initialPlacement = new PlacedRoom(startRoom, START_X, START_Y);

        if (initialPlacement.offsetX < 0 || initialPlacement.offsetY < 0 ||
            initialPlacement.offsetX + startRoom.width > GRID_SIZE ||
            initialPlacement.offsetY + startRoom.height > GRID_SIZE) {
            return new MapResult(new int[0][0], new int[0][0][], null);
        }

        placedRooms.add(initialPlacement);
        for (DoorBlock db : startRoom.doorBlocks) {
            openDoors.add(new OpenDoor(db, initialPlacement));
        }

        // Initialize boss state tracking
        boolean bossPlaced = false;

        boolean success = backtrack(placedRooms, openDoors, cappedDoors, rooms, rng, minRooms, maxRooms, allowFallbackCapping, bossPlaced);

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

            // --- Find Closed Areas (Flood Fill Air) ---
            List<int[][]> closedAreasList = new ArrayList<>();
            boolean[][] visited = new boolean[GRID_SIZE][GRID_SIZE];

            for (int y = 0; y < GRID_SIZE; y++) {
                for (int x = 0; x < GRID_SIZE; x++) {
                    if (mapGrid[y][x] == 0 && !visited[y][x]) {
                        List<int[]> currentArea = new ArrayList<>();
                        Queue<int[]> queue = new LinkedList<>();
                        queue.add(new int[]{x, y});
                        visited[y][x] = true;

                        boolean touchesEdge = false;

                        while (!queue.isEmpty()) {
                            int[] curr = queue.poll();
                            int cx = curr[0];
                            int cy = curr[1];
                            currentArea.add(curr);

                            if (cx == 0 || cx == GRID_SIZE - 1 || cy == 0 || cy == GRID_SIZE - 1) {
                                touchesEdge = true;
                            }

                            int[][] dirs = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
                            for (int[] d : dirs) {
                                int nx = cx + d[0];
                                int ny = cy + d[1];
                                if (nx >= 0 && nx < GRID_SIZE && ny >= 0 && ny < GRID_SIZE) {
                                    if (mapGrid[ny][nx] == 0 && !visited[ny][nx]) {
                                        visited[ny][nx] = true;
                                        queue.add(new int[]{nx, ny});
                                    }
                                }
                            }
                        }

                        // If it doesn't touch the edge, it's a closed pocket
                        if (!touchesEdge && !currentArea.isEmpty()) {
                            closedAreasList.add(currentArea.toArray(new int[0][]));
                        }
                    }
                }
            }

            int[][][] closedAreasArray = closedAreasList.toArray(new int[0][][]);

            return new MapResult(mapGrid, closedAreasArray, placedRooms);
        }

        return new MapResult(new int[0][0], new int[0][0][], null);
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
                                     Random rng, int minRooms, int maxRooms, boolean allowFallbackCapping, boolean bossPlaced) {

        // Base case 1: Out of open doors
        if (openDoors.isEmpty()) {
            // We only consider the layout successful if we have enough rooms AND we've placed the boss.
            return placedRooms.size() >= minRooms && bossPlaced;
        }

        // Base case 2: Reached maximum room limit
        if (placedRooms.size() >= maxRooms) {
            return bossPlaced;
        }

        OpenDoor targetDoor = openDoors.removeFirst();

        Map<RoomData, Double> scores = new HashMap<>();
        for (RoomData r : rooms) {
            // Logic to handle boss rooms
            if (r.isBoss) {
                if (bossPlaced) {
                    continue; // Skip if a boss is already placed
                }

                // Only consider placing a boss room if we're getting close to minRooms or maxRooms
                // to prevent it from spawning right next to start.
                if (placedRooms.size() < minRooms / 2) {
                    continue;
                }

                // If we are getting close to maxRooms and no boss is placed, dramatically increase weight
                if (placedRooms.size() > maxRooms - 5) {
                    scores.put(r, 0.0); // Highest priority
                    continue;
                }
            } else {
                // Try to force boss placement if we are out of open doors and it's still missing.
                if (!bossPlaced && openDoors.isEmpty() && placedRooms.size() >= minRooms) {
                    continue; // skip normal rooms, force a boss (or backtrack)
                }
            }

            double randomVal = rng.nextDouble();
            if (randomVal <= 0.0001) randomVal = 0.0001;
            scores.put(r, -Math.log(randomVal) / r.weight);
        }

        List<RoomData> randomizedRooms = new ArrayList<>(scores.keySet());
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

                        boolean currentBossPlaced = bossPlaced || candidateRoom.isBoss;

                        if (!hasBlockedDoors(placedRooms, openDoors)) {
                            if (backtrack(placedRooms, openDoors, cappedDoors, rooms, rng, minRooms, maxRooms, allowFallbackCapping, currentBossPlaced)) {
                                return true;
                            }
                        }

                        openDoors.removeAll(newOpenDoors);
                        placedRooms.removeLast();
                    }
                }
            }
        }

        if (allowFallbackCapping) {
            cappedDoors.add(targetDoor);
            if (backtrack(placedRooms, openDoors, cappedDoors, rooms, rng, minRooms, maxRooms, allowFallbackCapping, bossPlaced)) {
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
            assert room != null;
            int[][] grid = room.grid;

            boolean isHallway = room.isHallway;
            boolean isBoss = room.isBoss;

            if (grid != null && grid.length > 0 && grid[0].length > 0) {
                rooms.add(new RoomData(filename, isHallway, isBoss, grid, grid[0].length, grid.length));
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
                        block.originX = block.tiles.getFirst().dx;
                        block.originY = block.tiles.getFirst().dy;
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

    public static boolean isDoorTile(int tileId) {
        // TODO: use actual values
        return tileId >= 2 && tileId <= 5;
    }
}
