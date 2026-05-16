package client.systems.client;

import client.entities.ClientPrefabRegistry;
import client.entities.Prefab;
import client.entities.PrefabRegistry;
import client.network.NetworkManager;
import client.network.messages.Message;
import client.network.messages.server.S_Despawn;
import client.network.messages.server.S_Disconnect;
import client.network.messages.server.S_Spawn;
import framework.engine.EntitySystem;

import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClientSpawnSystem extends EntitySystem<Context> {
    private final ConcurrentLinkedQueue<Message> spawnQueue;
    private final Map<Integer, Prefab> networkPrefabMap;
    private final Prefab player;

    private final PrefabRegistry prefabRegistry = new ClientPrefabRegistry();

    public ClientSpawnSystem(ConcurrentLinkedQueue<Message> spawnQueue, Map<Integer, Prefab> networkPrefabMap, Prefab player) {
        this.spawnQueue = spawnQueue;
        this.networkPrefabMap = networkPrefabMap;
        this.player = player;
    }

    @Override
    public void update(Context ctx) {
        NetworkManager nm = NetworkManager.getInstance();

        Message msg;
        while ((msg = spawnQueue.poll()) != null) {
            if (msg instanceof S_Spawn m) {
                // if self, skip
                if (nm.networkId == m.getNetworkId()) {
                    networkPrefabMap.put(m.getNetworkId(), player);
                    continue;
                }

                Prefab prefab = prefabRegistry.spawn(engine, m.getPrefabId(), m.getNetworkId(), m.getBytes());
                networkPrefabMap.put(m.getNetworkId(), prefab);
            } else if (msg instanceof S_Despawn m) {
                Prefab prefab = networkPrefabMap.remove(m.getNetworkId());
                if (prefab.onDespawn()) {
                    engine.destroyEntity(prefab.getEntity().getId());
                }
            } else if (msg instanceof S_Disconnect m) {

            }
        }
    }
}
