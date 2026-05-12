package client.systems.server;

import client.network.MessagePair;
import client.network.messages.Message;
import client.systems.client.Context;
import framework.engine.EntitySystem;

import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ServerNetworkInputSystem extends EntitySystem<Context> {
    ConcurrentLinkedQueue<MessagePair> queue;
    Map<Class<? extends Message>, MessageHandler> handlers;

    public ServerNetworkInputSystem(ConcurrentLinkedQueue<MessagePair> queue, Map<Class<? extends Message>, MessageHandler> handlers) {
        this.queue = queue;
        this.handlers = handlers;
    }

    @Override
    public void update(Context ctx) {
        MessagePair message;
        while ((message = queue.poll()) != null) {
            Message msg = message.getMessage();
            int playerId = message.getPlayerId();

            handlers.get(msg.getClass()).handle(playerId, msg);
        }
    }

    @FunctionalInterface
    public interface MessageHandler {
        void handle(int playerId, Message msg);
    }
}
