package client.systems.client;

import client.components.MovementComponent;
import client.components.PlayerUpgradeComponent;
import client.components.TransformComponent;
import client.components.player.MovementInputComponent;
import client.components.player.PlayerStateComponent;
import client.components.player.PlayerTagComponent;
import client.network.messages.Message;
import client.network.messages.client.C_ApplyUpgrade;
import client.network.messages.client.C_PlayerState;
import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.IteratingEntitySystem;

import java.util.concurrent.ConcurrentLinkedQueue;

public class PlayerUpgradeUpdateSystem extends IteratingEntitySystem<Context> {
    private final ConcurrentLinkedQueue<Message> outQueue;

    public PlayerUpgradeUpdateSystem(ConcurrentLinkedQueue<Message> outQueue) {
        super(PlayerTagComponent.class);

        this.outQueue = outQueue;
    }

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);
    }

    @Override
    protected void processEntity(int entityId, Context ctx) {
        PlayerUpgradeComponent puc = engine.getMapper(PlayerUpgradeComponent.class).get(entityId);

        Integer index;
        while ((index = puc.applyQueue.poll()) != null) {
            outQueue.offer(new C_ApplyUpgrade(index));
        }
    }
}
