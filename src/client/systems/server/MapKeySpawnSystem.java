package client.systems.server;

import client.entities.Key;
import client.network.NetworkSpawnManager;
import client.systems.client.Context;
import common.MapGenerator;
import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.EntitySystem;
import org.joml.Vector2f;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class MapKeySpawnSystem extends EntitySystem<Context> {
    private MapGenerator.MapResult grid;
    private NetworkSpawnManager nsm;
    private Set<Integer> spawned = new HashSet<>();
    private Random random = new Random();

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);
    }

    public MapKeySpawnSystem(MapGenerator.MapResult grid, NetworkSpawnManager nsm) {
        this.grid = grid;
        this.nsm = nsm;

        // Spawn 3 keys in rooms from maxDepth-2 to maxDepth
        int maxDepth = grid.getMaxDepth();
        int minDepth = Math.max(0, maxDepth - 2);
        
        List<Integer> roomsInRange = grid.getRoomsAtDepthRange(minDepth, maxDepth);
        
        // Spawn 3 keys randomly in those rooms
        for (int i = 0; i < 3 && !roomsInRange.isEmpty(); i++) {
            int roomIdx = roomsInRange.get(random.nextInt(roomsInRange.size()));
            int[][] area = grid.closedAreas[roomIdx];
            
            if (area.length > 0) {
                int[] pos = area[random.nextInt(area.length)];
                float x = pos[0] * 64.0f + 32.0f; // Center in tile
                float y = pos[1] * 64.0f + 32.0f;
                
                int networkId = Math.abs(random.nextInt());
                nsm.spawn(Key.class, Key.serialize(x, y));
                spawned.add(networkId);
            }
        }
    }

    @Override
    public void update(Context ctx) {
        // Keys are pre-spawned, no need to update every frame
    }
}