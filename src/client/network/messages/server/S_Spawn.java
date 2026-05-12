package client.network.messages.server;

import client.network.messages.Message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class S_Spawn implements Message {
    private final int prefabId;
    private final int networkId;
    private final byte[] bytes;

    public S_Spawn(int prefabId, int networkId, byte[] bytes) {
        this.prefabId = prefabId;
        this.networkId = networkId;
        this.bytes = bytes;
    }

    public int getPrefabId() {
        return prefabId;
    }

    public int getNetworkId() {
        return networkId;
    }

    public byte[] getBytes() {
        return bytes;
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        out.writeInt(prefabId);
        out.writeInt(networkId);
        out.writeInt(bytes.length);
        out.write(bytes);
    }

    public static S_Spawn deserialize(DataInputStream in) throws IOException {
        int prefabId = in.readInt();
        int networkId = in.readInt();
        int length = in.readInt();
        byte[] bytes = in.readNBytes(length);

        System.out.printf("S_Spawn{prefabId = %d, networkId = %d}%n", prefabId, networkId);
        return new S_Spawn(prefabId, networkId, bytes);
    }
}
