package client.network.messages.server;

import client.network.messages.Message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class S_Despawn implements Message {
    private int networkId;

    public S_Despawn(int networkId) {
        this.networkId = networkId;
    }

    public int getNetworkId() {
        return networkId;
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        out.writeInt(networkId);
    }

    public static S_Despawn deserialize(DataInputStream in) throws IOException {
        int networkId = in.readInt();
        return new S_Despawn(networkId);
    }
}
