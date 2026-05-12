package client.systems.server;

import client.network.GameServer;
import client.network.MessagePair;
import client.network.messages.Message;
import client.systems.client.Context;
import framework.engine.EntitySystem;

import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ServerNetworkOutputSystem extends EntitySystem<Context> {
    private ConcurrentLinkedQueue<MessagePair> queue;
    private Map<Integer, GameServer.ClientConnection> connectedClients;

    public ServerNetworkOutputSystem(ConcurrentLinkedQueue<MessagePair> queue, Map<Integer, GameServer.ClientConnection> connectedClients) {
        this.queue = queue;
        this.connectedClients = connectedClients;
    }

    @Override
    public void update(Context ctx) {
        MessagePair message;
        while ((message = queue.poll()) != null) {
            int id = message.getPlayerId();
            Message msg = message.getMessage();
            if (id == -1) {
                for (var client : connectedClients.values()) {
                    client.send(msg);
                }
            } else {
                var client = connectedClients.get(id);
                client.send(msg);
            }
        }
    }
}
