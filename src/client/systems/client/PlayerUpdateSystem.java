package client.systems.client;

import client.components.MovementComponent;
import client.components.TransformComponent;
import client.components.player.MovementInputComponent;
import client.components.player.PlayerStateComponent;
import client.components.player.PlayerTagComponent;
import client.network.messages.Message;
import client.network.messages.client.C_PlayerState;
import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.IteratingEntitySystem;

import java.util.concurrent.ConcurrentLinkedQueue;

public class PlayerUpdateSystem extends IteratingEntitySystem<Context> {
    private ConcurrentLinkedQueue<Message> outQueue;

    private ComponentMapper<TransformComponent> tm;
    private ComponentMapper<PlayerStateComponent> psm;
    private ComponentMapper<MovementComponent> mm;
    private ComponentMapper<MovementInputComponent> mim;

    public PlayerUpdateSystem(ConcurrentLinkedQueue<Message> outQueue) {
        super(PlayerTagComponent.class);

        this.outQueue = outQueue;
    }

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);

        tm = engine.getMapper(TransformComponent.class);
        psm = engine.getMapper(PlayerStateComponent.class);
        mim = engine.getMapper(MovementInputComponent.class);
        mm = engine.getMapper(MovementComponent.class);
    }

    @Override
    protected void processEntity(int entityId, Context ctx) {
        TransformComponent tc = tm.get(entityId);
        PlayerStateComponent psc = psm.get(entityId);
        MovementInputComponent mic = mim.get(entityId);
        MovementComponent mc = mm.get(entityId);

        outQueue.offer(new C_PlayerState(tc.position.x, tc.position.y, tc.rotation, psc.previous, psc.current, mic.x, mic.y, mc.velocity.x, mc.velocity.y));
    }
}
