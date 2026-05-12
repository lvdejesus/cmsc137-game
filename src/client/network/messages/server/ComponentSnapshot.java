package client.network.messages.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ComponentSnapshot {
    private final int componentId;
    private final byte[] data;

    public ComponentSnapshot(int componentId, byte[] data) {
        this.componentId = componentId;
        this.data = data;
    }

    public void serialize(DataOutputStream out) throws IOException {
        out.writeInt(componentId);
        out.writeInt(data.length);
        out.write(data);
    }

    public static ComponentSnapshot deserialize(DataInputStream in) throws IOException {
        int componentId = in.readInt();
        int length = in.readInt();
        byte[] data = in.readNBytes(length);

        return new ComponentSnapshot(componentId, data);
    }

    public int getComponentId() {
        return componentId;
    }

    public byte[] getData() {
        return data;
    }
}
