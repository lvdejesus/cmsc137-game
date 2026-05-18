package client.network.messages.server;

import client.network.messages.Message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class S_ChatMessage implements Message {
    private final int playerId;
    private final String text;

    public S_ChatMessage(int playerId, String text) {
        this.playerId = playerId;
        this.text = text;
    }

    public int getPlayerId() {
        return playerId;
    }

    public String getText() {
        return text;
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        out.writeInt(playerId);
        out.writeUTF(text);
    }

    public static S_ChatMessage deserialize(DataInputStream in) throws IOException {
        return new S_ChatMessage(in.readInt(), in.readUTF());
    }
}
