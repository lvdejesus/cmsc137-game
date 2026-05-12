package client.network;

import client.network.messages.Message;

public class MessagePair {
    private int playerId;
    private Message message;

    public MessagePair(int playerId, Message message) {
        this.playerId = playerId;
        this.message = message;
    }

    public int getPlayerId() {
        return playerId;
    }

    public Message getMessage() {
        return message;
    }
}
