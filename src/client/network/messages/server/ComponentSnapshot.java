package client.network.messages.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class ComponentSnapshot {
    private static final ThreadLocal<byte[]> bufferPool = ThreadLocal.withInitial(() -> new byte[8192]);

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
        byte[] buf = bufferPool.get();
        if (buf.length < length) {
            buf = new byte[length];
            bufferPool.set(buf);
        }
        in.readFully(buf, 0, length);
        byte[] data = Arrays.copyOf(buf, length);
        return new ComponentSnapshot(componentId, data);
    }

    public int getComponentId() {
        return componentId;
    }

    public byte[] getData() {
        return data;
    }
}
