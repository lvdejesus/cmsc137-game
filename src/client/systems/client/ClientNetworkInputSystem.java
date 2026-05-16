package client.systems.client;

import client.components.NetworkDuplicateComponent;
import client.components.NetworkIdComponent;
import client.entities.ClientPrefabRegistry;
import client.entities.Player;
import client.entities.Prefab;
import client.entities.PrefabRegistry;
import client.network.NetworkManager;
import client.network.messages.Message;
import client.network.messages.server.S_Despawn;
import client.network.messages.server.S_Disconnect;
import client.network.messages.server.S_Snapshot;
import client.network.messages.server.S_Spawn;
import framework.engine.EntitySystem;
import framework.engine.SyncComponent;

import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClientNetworkInputSystem extends EntitySystem<Context> {
    private final ConcurrentLinkedQueue<Message> inQueue;
    private final Map<Integer, Prefab> networkPrefabMap;
    private final Player player;

    private final PrefabRegistry prefabRegistry = new ClientPrefabRegistry();

    public ClientNetworkInputSystem(ConcurrentLinkedQueue<Message> inQueue, Map<Integer, Prefab> networkPrefabMap, Player player) {
        this.inQueue = inQueue;
        this.networkPrefabMap = networkPrefabMap;
        this.player = player;
    }

    @Override
    public void update(Context ctx) {
        NetworkManager nm = NetworkManager.getInstance();

        Message msg;
        while ((msg = inQueue.poll()) != null) {
            if (msg instanceof S_Spawn m) {
                // if self, skip
                if (nm.networkId == m.getNetworkId()) {
                    networkPrefabMap.put(m.getNetworkId(), player);
                    continue;
                }

                Prefab prefab = prefabRegistry.spawn(engine, m.getPrefabId(), m.getNetworkId(), m.getBytes());
                System.out.printf("Spawn for %s %d (%d).%n", prefab.getClass().getName(), prefab.getEntity().getId(), m.getNetworkId());
                networkPrefabMap.put(m.getNetworkId(), prefab);
            } else if (msg instanceof S_Despawn m) {
                Prefab prefab = networkPrefabMap.remove(m.getNetworkId());
                if (prefab.onDespawn()) {
                    System.out.printf("Immediate despawn for %s %d (%d).%n", prefab.getClass().getName(), prefab.getEntity().getId(), m.getNetworkId());
                    engine.destroyEntity(prefab.getEntity().getId());
                } else {
                    System.out.printf("Delayed despawn for %s %d (%d).%n", prefab.getClass().getName(), prefab.getEntity().getId(), m.getNetworkId());
                }
            } else if (msg instanceof S_Disconnect m) {

            } else if (msg instanceof S_Snapshot m) {
                for (var entitySnapshot : m.getEntitySnapshots()) {
                    Prefab entity = networkPrefabMap.get(entitySnapshot.getNetworkId());
                    if (entity == null) {
                        throw new RuntimeException("Received snapshot of a despawned entity.");
                    }

                    var ndc = engine.getMapper(NetworkDuplicateComponent.class).get(entity.getEntity().getId());
                    if (ndc == null) {
                        throw new RuntimeException(String.format("Cannot find components of %s %d (%d).", entity.getClass().getName(), entity.getEntity().getId(), entitySnapshot.getNetworkId()));
                    }
                    var keySet = ndc.components.keySet();
                    for (var component : entitySnapshot.getComponents()) {
                        var cc = engine.getComponentClass(component.getComponentId());
                        if (!keySet.contains(cc)) continue;

                        var syncComponent = (SyncComponent) engine.getMapper(cc).get(entity.getEntity().getId());
                        if (syncComponent == null) continue;

                        syncComponent.fromBytes(component.getData());
                    }
                }
            }
        }
    }
}
