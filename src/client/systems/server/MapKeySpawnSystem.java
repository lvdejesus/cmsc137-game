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
    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);
    }

    public MapKeySpawnSystem(MapGenerator.MapResult grid, NetworkSpawnManager nsm) {
        Random random = new Random();

        int[] path = grid.findMaxShortestPath();

        for (int roomIdx: path) {
            int[][] area = grid.closedAreas[roomIdx];

            if (area.length > 0) {
                int[] pos = area[random.nextInt(area.length)];
                float x = pos[0] * 64.0f + 32.0f;
                float y = pos[1] * 64.0f + 32.0f;

                nsm.spawn(Key.class, Key.serialize(x, y));
            }
        }
    }

    @Override
    public void update(Context ctx) {
    }
}