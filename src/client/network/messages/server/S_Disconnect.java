package client.network.messages.server;

import client.network.messages.Message;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class S_Disconnect implements Message {
    private final int networkId;

    public S_Disconnect(int networkId) {
        this.networkId = networkId;
    }

    public int getNetworkId() { return networkId; }

    public void serialize(DataOutputStream out) throws IOException {
        out.writeInt(networkId);
    }

    public static S_Disconnect deserialize(DataInputStream in) throws IOException {
        int playerId = in.readInt();
        return new S_Disconnect(playerId);
    }
}