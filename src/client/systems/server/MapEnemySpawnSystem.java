package client.systems.server;

import client.components.TransformComponent;
import client.components.player.PlayerStateComponent;
import client.entities.Enemy;
import client.network.NetworkSpawnManager;
import client.systems.client.Context;
import common.MapGenerator;
import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.EntitySystem;
import org.joml.Vector2f;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class MapEnemySpawnSystem  extends EntitySystem<Context> {
    private MapGenerator.MapResult grid;
    private NetworkSpawnManager nsm;
    private Set<Integer> visited = new HashSet<>();
    private Set<Integer> spawned = new HashSet<>();
    private Set<Integer> toSpawn = new HashSet<>();

    private ComponentMapper<TransformComponent> tm;
    private Random random = new Random();

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);

        tm = engine.getMapper(TransformComponent.class);
    }

    public MapEnemySpawnSystem(MapGenerator.MapResult grid, NetworkSpawnManager nsm) {
        this.grid = grid;
        this.nsm = nsm;

        int originArea = grid.getAreaIndex(155, 155);

        visit(originArea);
        spawned.add(originArea);
    }

    private void visit(int index) {
        if (visited.contains(index)) return;

        visited.add(index);
        for (var neighbor : grid.getNeighboringAreas(index)) {
            if (spawned.contains(neighbor)) continue;

            spawned.add(neighbor);
            toSpawn.add(neighbor);
        }
    }

    @Override
    public void update(Context ctx) {
        Iterable<Integer> playerIds = engine.getFamily(TransformComponent.class, PlayerStateComponent.class)::iterator;
        for (var playerId : playerIds) {
            Vector2f tileCoordinates = new Vector2f(tm.get(playerId).position).div(64.0f).floor();
            int x = (int) tileCoordinates.x;
            int y = (int) tileCoordinates.y;

            int index = grid.getAreaIndex(x, y);
            if (index == -1) continue;

            visit(index);
        }

        for (var v: toSpawn) {
            spawn(v);
        }

        toSpawn.clear();
    }

    private void spawn(int areaIndex) {
        if (areaIndex == grid.bossRoomIndex) {
            var closedAreas = grid.closedAreas[areaIndex];
            var pos = closedAreas[random.nextInt(closedAreas.length)];
            nsm.spawn(Enemy.class, Enemy.serialize(pos[0] * 64.0f, pos[1] * 64.0f, grid.bossRoom.offsetX + 11.0f, grid.bossRoom.offsetY + 8.5f));
        } else {
            var closedAreas = grid.closedAreas[areaIndex];
            int numEnemies = Math.max(5, (int) Math.floor(Math.sqrt(closedAreas.length)));
            for (int i = 0; i < numEnemies; i++) {
                var pos = closedAreas[random.nextInt(closedAreas.length)];
                nsm.spawn(Enemy.class, Enemy.serialize(pos[0] * 64.0f, pos[1] * 64.0f, Enemy.EnemyType.Regular));
            }
        }
    }
}
