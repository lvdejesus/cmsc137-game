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
    private ComponentMapper<MovementInputComponent> mim;
    private ComponentMapper<PlayerStateComponent> sm;

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
        mim = engine.getMapper(MovementInputComponent.class);
        sm = engine.getMapper(PlayerStateComponent.class);
    }

    @Override
    protected void processEntity(int entityId, Context ctx) {
        NetworkIdComponent nic = nicm.get(entityId);

        TransformComponent tc = tm.get(entityId);
        MovementInputComponent mic = mim.get(entityId);
        PlayerStateComponent sc = sm.get(entityId);

        List<ComponentSnapshot> components = new ArrayList<>();

        if (tc != null) {
            TransformComponent.Sync tSync = TransformComponent.Sync.extract(tc);
            components.add(new ComponentSnapshot(tm.getIndex(), tSync.toBytes()));
        }

        if (mic != null) {
            MovementInputComponent.Sync miSync = MovementInputComponent.Sync.extract(mic);
            components.add(new ComponentSnapshot(mim.getIndex(), miSync.toBytes()));
        }

        if (sc != null) {
            PlayerStateComponent.Sync sSync = PlayerStateComponent.Sync.extract(sc);
            components.add(new ComponentSnapshot(sm.getIndex(), sSync.toBytes()));
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
