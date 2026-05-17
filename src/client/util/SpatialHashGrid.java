package client.util;

import client.components.TransformComponent;
import org.joml.Vector2f;
import org.joml.primitives.AABBf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SpatialHashGrid {
    private final int cellSize;
    private final Map<Long, List<Integer>> grid = new HashMap<>();

    public SpatialHashGrid(int cellSize) {
        this.cellSize = cellSize;
    }

    public void clear() {
        grid.clear();
    }

    public void addEntity(int entityId, AABBf worldBounds) {
        int minX = (int) Math.floor(worldBounds.minX / cellSize);
        int minY = (int) Math.floor(worldBounds.minY / cellSize);
        int maxX = (int) Math.floor(worldBounds.maxX / cellSize);
        int maxY = (int) Math.floor(worldBounds.maxY / cellSize);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                long key = hash(x, y);
                grid.computeIfAbsent(key, k -> new ArrayList<>()).add(entityId);
            }
        }
    }

    public void removeEntity(int entityId, AABBf worldBounds) {
        int minX = (int) Math.floor(worldBounds.minX / cellSize);
        int minY = (int) Math.floor(worldBounds.minY / cellSize);
        int maxX = (int) Math.floor(worldBounds.maxX / cellSize);
        int maxY = (int) Math.floor(worldBounds.maxY / cellSize);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                long key = hash(x, y);
                grid.get(key).removeIf(i -> i == entityId);
            }
        }
    }

    public void getPotentialColliders(AABBf worldBounds, Set<Integer> result) {
        int minX = (int) Math.floor(worldBounds.minX / cellSize);
        int minY = (int) Math.floor(worldBounds.minY / cellSize);
        int maxX = (int) Math.floor(worldBounds.maxX / cellSize);
        int maxY = (int) Math.floor(worldBounds.maxY / cellSize);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                List<Integer> cell = grid.get(hash(x, y));
                if (cell != null) result.addAll(cell);
            }
        }
    }

    private long hash(int x, int y) {
        return ((long) x << 32) | (y & 0xFFFFFFFFL);
    }
}
