package client.network.messages.server;

import client.network.messages.Message;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class S_AssignId implements Message {
    private final int playerId;

    public S_AssignId(int playerId) {
        this.playerId = playerId;
    }

    public int getPlayerId() { return playerId; }

    public void serialize(DataOutputStream out) throws IOException {
        out.writeInt(playerId);
    }

    public static S_AssignId deserialize(DataInputStream in) throws IOException {
        int playerId = in.readInt();
        System.out.println("S_AssignId{playerId=" + playerId + "}");
        return new S_AssignId(playerId);
    }
}