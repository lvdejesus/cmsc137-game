package client.systems.server;

import client.components.NetworkIdComponent;
import client.components.TransformComponent;
import client.components.player.MovementInputComponent;
import client.components.player.PlayerStateComponent;
import client.entities.Player;
import client.network.MessagePair;
import client.network.messages.server.ComponentSnapshot;
import client.network.messages.server.EntitySnapshot;
import client.network.messages.server.S_Snapshot;
import client.systems.client.Context;
import client.systems.client.MovementInputSystem;
import framework.engine.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SnapshotSystem extends IteratingEntitySystem<Context> {
    private final ConcurrentLinkedQueue<EntitySnapshot> entityQueue;
    private final ConcurrentLinkedQueue<MessagePair> queue;

    private ComponentMapper<NetworkIdComponent> nicm;

    private List<ComponentMapper<? extends Component>> mappers = new ArrayList<>();

    public SnapshotSystem(ConcurrentLinkedQueue<EntitySnapshot> entityQueue, ConcurrentLinkedQueue<MessagePair> queue) {
        super(NetworkIdComponent.class, TransformComponent.class);

        this.entityQueue = entityQueue;
        this.queue = queue;
    }

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);

        nicm = engine.getMapper(NetworkIdComponent.class);

        for (var cc : engine.getComponentClasses()) {
            if (!SyncComponent.class.isAssignableFrom(cc)) continue;

            mappers.add(engine.getMapper(cc));
        }
    }

    @Override
    protected void processEntity(int entityId, Context ctx) {
        NetworkIdComponent nic = nicm.get(entityId);

        List<ComponentSnapshot> components = new ArrayList<>();

        for (var mapper : mappers) {
            var component = mapper.get(entityId);
            if (component == null) continue;
            if (!(component instanceof SyncComponent sc)) continue;
            components.add(new ComponentSnapshot(mapper.getIndex(), sc.toBytes()));
        }

        entityQueue.add(new EntitySnapshot(nic.networkId, components));
    }

    @Override
    public void update(Context ctx) {
        super.update(ctx);

        this.queue.add(new MessagePair(-1, new S_Snapshot(entityQueue.stream().toList())));
        entityQueue.clear();
    }
}
