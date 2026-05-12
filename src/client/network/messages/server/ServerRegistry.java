package client.network.messages.server;

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

public class ServerRegistry {
    private static final ServerRegistry instance = new ServerRegistry();
    private final Map<Integer, Deserializer> deserializers = new LinkedHashMap<>();
    private final Map<Class<? extends Message>, Integer> classes = new LinkedHashMap<>();
    private int currentId = 0;

    private ServerRegistry() {
        register(S_AssignId.class, S_AssignId::deserialize);
        register(S_PlayerCount.class, S_PlayerCount::deserialize);
        register(S_StartGame.class, S_StartGame::deserialize);
        register(S_PlayerPosition.class, S_PlayerPosition::deserialize);
    }

    public static ServerRegistry getInstance() { return instance; }

    private void register(Class<? extends Message> clazz, Deserializer deserializer) {
        int id = currentId++;
        classes.put(clazz, id);
        deserializers.put(id, deserializer);
    }

    public Message deserialize(DataInputStream in) throws IOException {
        int type = in.readInt();
        Deserializer d = deserializers.get(type);
        if (d == null) throw new IOException("Unknown server message type: " + type);
        return d.deserialize(in);
    }

    public void serialize(DataOutputStream out, Message msg) throws IOException {
        int id = classes.get(msg.getClass());
        out.writeInt(id);
        msg.serialize(out);
        out.flush();
    }
}