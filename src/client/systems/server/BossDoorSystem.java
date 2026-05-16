package client.systems.server;

import client.components.BossDoorComponent;
import client.components.PlayerKeysComponent;
import client.components.TransformComponent;
import client.components.player.PlayerStateComponent;
import client.network.NetworkSpawnManager;
import client.systems.client.Context;
import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.IteratingEntitySystem;

import java.util.HashMap;
import java.util.Map;

public class BossDoorSystem extends IteratingEntitySystem<Context> {
    private ComponentMapper<TransformComponent> tm;
    private ComponentMapper<PlayerKeysComponent> psm;
    private ComponentMapper<BossDoorComponent> bsm;

    private final Map<Long, Integer> keyCounts = new HashMap<>();

    private final static float TAKE_RANGE = 100.0f;
    private NetworkSpawnManager nsm;

    public BossDoorSystem(NetworkSpawnManager nsm) {
        super(BossDoorComponent.class);

        this.nsm = nsm;
    }

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);

        tm = engine.getMapper(TransformComponent.class);
        psm = engine.getMapper(PlayerKeysComponent.class);
        bsm = engine.getMapper(BossDoorComponent.class);
    }

    @Override
    protected void processEntity(int entityId, Context ctx) {
        TransformComponent tc = tm.get(entityId);
        BossDoorComponent bsc = bsm.get(entityId);

        long hash = ((long) bsc.bx << 32) | (bsc.by & 0xFFFFFFFFL);
        int numKeys = keyCounts.computeIfAbsent(hash, (i) -> 0);

        Iterable<Integer> playerEntityIds = engine.getFamily(PlayerStateComponent.class)::iterator;
        for (int playerEntityId : playerEntityIds) {
            float distance = tm.get(playerEntityId).position.distance(tc.position);
            if (distance < TAKE_RANGE) {
                // TODO: replace 3 with the required number of keys
                PlayerKeysComponent pkc = psm.get(playerEntityId);
                int keysConsumed = Math.min(3 - numKeys, pkc.keyCount);
                pkc.consumeKeys(keysConsumed);
                int newKeyCount = keyCounts.get(hash) + keysConsumed;
                keyCounts.put(hash, newKeyCount);
            }
        }

        if (numKeys == 3) {
            nsm.despawn(entityId);
        }
    }
}
