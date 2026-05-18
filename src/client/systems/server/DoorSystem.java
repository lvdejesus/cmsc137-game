package client.systems.server;

import client.components.*;
import client.components.enemy.EnemyComponent;
import client.components.player.PlayerStateComponent;
import client.network.NetworkSpawnManager;
import client.systems.client.Context;
import common.MapGenerator;
import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.EntitySystem;

import java.util.*;

public class DoorSystem extends EntitySystem<Context> {
    private ComponentMapper<DoorComponent> doorM;
    private ComponentMapper<TransformComponent> tm;
    private ComponentMapper<HealthComponent> hm;
    private ComponentMapper<WallComponent> wm;
    private MapGenerator.MapResult grid;

    public DoorSystem(MapGenerator.MapResult grid, NetworkSpawnManager nsm) {
        this.grid = grid;
    }

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);
        doorM = engine.getMapper(DoorComponent.class);
        tm = engine.getMapper(TransformComponent.class);
        hm = engine.getMapper(HealthComponent.class);
        wm = engine.getMapper(WallComponent.class);
    }

    @Override
    public void update(Context ctx) {
        Set<Integer> roomsWithEnemies = new HashSet<>();
        Iterable<Integer> enemies = engine.getFamily(EnemyComponent.class, HealthComponent.class)::iterator;
        for (int id : enemies) {
            HealthComponent hc = hm.get(id);
            if (!hc.isAlive()) continue;
            var pos = tm.get(id).position;
            int areaIdx = grid.getAreaIndex((int) (pos.x / 64.0f), (int) (pos.y / 64.0f));
            if (areaIdx != -1) roomsWithEnemies.add(areaIdx);
        }

        Iterable<Integer> doors = engine.getFamily(DoorComponent.class)::iterator;
        for (int doorId : doors) {
            DoorComponent dc = doorM.get(doorId);

            if (dc.state == DoorComponent.State.CLOSED) {
                Set<Integer> adjacent = getAdjacentRooms(dc.gridX, dc.gridY);
                boolean anyCleared = false;
                for (int area : adjacent) {
                    if (!roomsWithEnemies.contains(area)) {
                        anyCleared = true;
                        break;
                    }
                }
                if (!anyCleared) continue;

                boolean playerNear = false;
                var doorPos = tm.get(doorId).position;
                Iterable<Integer> players = engine.getFamily(PlayerStateComponent.class, HealthComponent.class)::iterator;
                for (int pId : players) {
                    HealthComponent phc = hm.get(pId);
                    if (!phc.isAlive()) continue;
                    if (tm.get(pId).position.distance(doorPos) < 64.0f * 3.0f) {
                        playerNear = true;
                        break;
                    }
                }


                if (playerNear) {
                    dc.state = DoorComponent.State.OPENING;
                    dc.animTimer = 0;
                }
            }

            if (dc.state == DoorComponent.State.OPENING) {
                dc.animTimer += ctx.deltaTime;
                if (dc.animTimer >= 0.7f) {
                    dc.state = DoorComponent.State.OPEN;
                    WallComponent wc = wm.get(doorId);
                    if (wc != null) wc.setActive(false);
                }
            }
        }
    }

    private Set<Integer> getAdjacentRooms(int gx, int gy) {
        Set<Integer> rooms = new HashSet<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if ((dx == 0) == (dy == 0)) continue;
                int area = grid.getAreaIndex(gx + dx, gy + dy);
                if (area != -1) rooms.add(area);
            }
        }
        return rooms;
    }
}
