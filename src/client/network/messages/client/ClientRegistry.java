package client.network.messages.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import client.network.messages.Message;

@FunctionalInterface
interface Deserializer {
    Message deserialize(DataInputStream in) throws IOException;
}

public class ClientRegistry {
    private static final ClientRegistry instance = new ClientRegistry();
    private final Map<Integer, Deserializer> deserializers = new LinkedHashMap<>();

    private ClientRegistry() {
        register(0, C_PlayerPosition::deserialize);
    }

    public static ClientRegistry getInstance() { return instance; }

    private void register(int type, Deserializer deserializer) {
        deserializers.put(type, deserializer);
    }

    public void serializeAndSend(DataOutputStream out, int type, Message msg) throws IOException {
        out.writeInt(type);
        msg.serialize(out);
        out.flush();
    }

    public Message deserialize(DataInputStream in) throws IOException {
        int type = in.readInt();
        Deserializer d = deserializers.get(type);
        if (d == null) throw new IOException("Unknown client message type: " + type);
        return d.deserialize(in);
    }
}