package client.systems.client;

import client.network.messages.Message;
import client.network.messages.server.S_Despawn;
import client.network.messages.server.S_Disconnect;
import client.network.messages.server.S_Snapshot;
import client.network.messages.server.S_Spawn;
import framework.engine.EntitySystem;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ClientNetworkInputSystem extends EntitySystem<Context> {
    private ConcurrentLinkedQueue<Message> inQueue;
    private ConcurrentLinkedQueue<Message> spawnQueue;
    private ConcurrentLinkedQueue<Message> snapshotQueue;

    public ClientNetworkInputSystem(ConcurrentLinkedQueue<Message> inQueue, ConcurrentLinkedQueue<Message> spawnQueue, ConcurrentLinkedQueue<Message> snapshotQueue) {
        this.inQueue = inQueue;
        this.spawnQueue = spawnQueue;
        this.snapshotQueue = snapshotQueue;
    }

    @Override
    public void update(Context ctx) {
        Message msg;
        while ((msg = inQueue.poll()) != null) {
            if (msg instanceof S_Spawn || msg instanceof S_Despawn || msg instanceof S_Disconnect) {
                spawnQueue.offer(msg);
            } else if (msg instanceof S_Snapshot m) {
                snapshotQueue.offer(msg);
            }
        }
    }
}
