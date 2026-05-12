package client.network.messages.server;

import client.network.messages.Message;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class S_AssignId implements Message {
    private final int playerId;
    private final int networkId;

    public S_AssignId(int playerId, int networkId) {
        this.playerId = playerId;
        this.networkId = networkId;
    }

    public int getPlayerId() { return playerId; }
    public int getNetworkId() { return networkId; }

    public void serialize(DataOutputStream out) throws IOException {
        out.writeInt(playerId);
        out.writeInt(networkId);
    }

    public static S_AssignId deserialize(DataInputStream in) throws IOException {
        int playerId = in.readInt();
        int networkId = in.readInt();
        System.out.println("S_AssignId{playerId=" + playerId + ", networkId=" + networkId + "}");
        return new S_AssignId(playerId, networkId);
    }
}