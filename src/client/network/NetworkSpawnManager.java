package client.network;

import client.entities.Prefab;
import client.entities.PrefabRegistry;
import client.network.messages.Message;
import client.network.messages.server.S_Despawn;
import client.network.messages.server.S_Spawn;
import client.systems.client.Context;
import framework.engine.Engine;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class NetworkSpawnManager {
    AtomicInteger networkId = new AtomicInteger(0);

    private final Engine<Context> engine;
    private final PrefabRegistry prefabRegistry;
    private final ConcurrentLinkedQueue<MessagePair> outQueue;

    public class SpawnResult {
        public Prefab prefab;
        public int networkId;

        public SpawnResult(Prefab prefab, int networkId) {
            this.prefab = prefab;
            this.networkId = networkId;
        }
    }

    public NetworkSpawnManager(Engine<Context> engine, PrefabRegistry prefabRegistry, ConcurrentLinkedQueue<MessagePair> outQueue) {
        this.engine = engine;
        this.prefabRegistry = prefabRegistry;
        this.outQueue = outQueue;
    }

    public SpawnResult spawn(Class<? extends Prefab> clazz, byte[] bytes) {
        int prefabId = prefabRegistry.get(clazz);
        int id = networkId.getAndIncrement();
        Message msg = new S_Spawn(prefabId, id, bytes);
        outQueue.add(new MessagePair(-1, msg));
        return new SpawnResult(prefabRegistry.spawnServer(engine, prefabId, id, bytes), id);
    }

    public void despawn(int entityId, int networkId) {
        Message msg = new S_Despawn(networkId);
        outQueue.add(new MessagePair(-1, msg));
        engine.destroyEntity(entityId);
    }
}
