package client.systems.client;

import client.components.NetworkDuplicateComponent;
import client.entities.ClientPrefabRegistry;
import client.entities.Prefab;
import client.entities.PrefabRegistry;
import client.network.messages.Message;
import client.network.messages.server.S_Snapshot;
import framework.engine.EntitySystem;
import framework.engine.SyncComponent;

import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClientSnapshotSystem extends EntitySystem<Context> {
    private final ConcurrentLinkedQueue<Message> snapshotQueue;
    private final Map<Integer, Prefab> networkPrefabMap;

    private final PrefabRegistry prefabRegistry = new ClientPrefabRegistry();

    public ClientSnapshotSystem(ConcurrentLinkedQueue<Message> snapshotQueue, Map<Integer, Prefab> networkPrefabMap) {
        this.snapshotQueue = snapshotQueue;
        this.networkPrefabMap = networkPrefabMap;
    }

    @Override
    public void update(Context ctx) {
        Message msg;
        while ((msg = snapshotQueue.poll()) != null) {
            if (msg instanceof S_Snapshot m) {

                for (var entitySnapshot : m.getEntitySnapshots()) {
                    Prefab entity = networkPrefabMap.get(entitySnapshot.getNetworkId());
                    if (entity == null) {
                        continue;
                    }

                    var keySet = engine.getMapper(NetworkDuplicateComponent.class).get(entity.getEntity().getId()).components.keySet();
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
