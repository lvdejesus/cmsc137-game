package client.systems.server;

import client.components.NetworkIdComponent;
import client.components.TransformComponent;
import client.network.MessagePair;
import client.network.messages.server.ComponentSnapshot;
import client.network.messages.server.EntitySnapshot;
import client.network.messages.server.S_Snapshot;
import client.systems.client.Context;
import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.IteratingEntitySystem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SnapshotSystem extends IteratingEntitySystem<Context> {
    private final ConcurrentLinkedQueue<EntitySnapshot> entityQueue;
    private final ConcurrentLinkedQueue<MessagePair> queue;

    private ComponentMapper<NetworkIdComponent> nicm;
    private ComponentMapper<TransformComponent> tm;

    public SnapshotSystem(ConcurrentLinkedQueue<EntitySnapshot> entityQueue, ConcurrentLinkedQueue<MessagePair> queue) {
        super(NetworkIdComponent.class, TransformComponent.class);

        this.entityQueue = entityQueue;
        this.queue = queue;
    }

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);

        nicm = engine.getMapper(NetworkIdComponent.class);
        tm = engine.getMapper(TransformComponent.class);
    }

    @Override
    protected void processEntity(int entityId, Context ctx) {
        NetworkIdComponent nic = nicm.get(entityId);
        TransformComponent tc = tm.get(entityId);

        TransformComponent.Sync sync = TransformComponent.Sync.extract(tc);
        List<ComponentSnapshot> components = new ArrayList<>();

        components.add(new ComponentSnapshot(0, sync.toBytes()));

        entityQueue.add(new EntitySnapshot(nic.networkId, components));
    }

    @Override
    public void update(Context ctx) {
        super.update(ctx);

        this.queue.add(new MessagePair(-1, new S_Snapshot(entityQueue.stream().toList())));
        entityQueue.clear();
    }
}
