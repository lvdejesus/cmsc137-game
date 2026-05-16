package client.systems.client;

import client.network.NetworkManager;
import client.network.messages.Message;
import framework.engine.EntitySystem;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ClientNetworkOutputSystem extends EntitySystem<Context> {
    private final ConcurrentLinkedQueue<Message> outQueue;

    public ClientNetworkOutputSystem(ConcurrentLinkedQueue<Message> outQueue) {
        this.outQueue = outQueue;
    }

    @Override
    public void update(Context ctx) {
        Message msg;
        while ((msg = outQueue.poll()) != null) {
            NetworkManager.getInstance().sendMessage(msg);
        }
    }
}
