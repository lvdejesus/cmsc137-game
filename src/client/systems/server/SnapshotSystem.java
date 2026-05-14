package client.systems.server;

import client.components.NetworkDuplicateComponent;
import client.components.NetworkIdComponent;
import client.components.TransformComponent;
import client.network.MessagePair;
import client.network.messages.server.ComponentSnapshot;
import client.network.messages.server.EntitySnapshot;
import client.network.messages.server.S_Snapshot;
import client.systems.client.Context;
import framework.engine.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SnapshotSystem extends IteratingEntitySystem<Context> {
    private final ConcurrentLinkedQueue<EntitySnapshot> entityQueue;
    private final ConcurrentLinkedQueue<MessagePair> queue;

    private ComponentMapper<NetworkIdComponent> nicm;
    private ComponentMapper<NetworkDuplicateComponent> ndm;

    private List<ComponentMapper<? extends Component>> mappers = new ArrayList<>();

    public SnapshotSystem(ConcurrentLinkedQueue<EntitySnapshot> entityQueue, ConcurrentLinkedQueue<MessagePair> queue) {
        super(NetworkIdComponent.class);

        this.entityQueue = entityQueue;
        this.queue = queue;
    }

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);

        nicm = engine.getMapper(NetworkIdComponent.class);
        ndm = engine.getMapper(NetworkDuplicateComponent.class);

        for (var cc : engine.getComponentClasses()) {
            if (!SyncComponent.class.isAssignableFrom(cc)) continue;

            mappers.add(engine.getMapper(cc));
        }
    }

    @Override
    protected void processEntity(int entityId, Context ctx) {
        NetworkIdComponent nic = nicm.get(entityId);
        NetworkDuplicateComponent ndc = ndm.get(entityId);

        if (ndc == null) {
            // no sync required
            return;
        };

        List<ComponentSnapshot> components = new ArrayList<>();

        for (var entry : ndc.components.entrySet()) {
            var mapper = engine.getMapper(entry.getKey());
            var component = mapper.get(entityId);
            if (component == null) continue;
            if (!(component instanceof SyncComponent sc)) continue;
            var existingComponent = entry.getValue();
            if (sc.isEqual(existingComponent)) continue;
            if (existingComponent == null) {
                ndc.components.put(entry.getKey(), sc.clone());
            } else {
                existingComponent.copyFrom(sc);
            }

            components.add(new ComponentSnapshot(mapper.getIndex(), sc.toBytes()));
        }

        if (!components.isEmpty()) {
            entityQueue.add(new EntitySnapshot(nic.networkId, components));
        }
    }

    @Override
    public void update(Context ctx) {
        super.update(ctx);

        this.queue.add(new MessagePair(-1, new S_Snapshot(entityQueue.stream().toList())));
        entityQueue.clear();
    }
}
