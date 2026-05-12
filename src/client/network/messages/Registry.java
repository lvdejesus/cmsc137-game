package client.network.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class Registry {
    private final Map<Integer, Deserializer> deserializers = new LinkedHashMap<>();
    private final Map<Class<? extends Message>, Integer> classes = new LinkedHashMap<>();
    private int currentId = 0;

    protected void register(Class<? extends Message> clazz, Deserializer deserializer) {
        int id = currentId++;
        classes.put(clazz, id);
        deserializers.put(id, deserializer);
    }

    public Message receive(DataInputStream in) throws IOException {
        int type = in.readInt();
        Deserializer d = deserializers.get(type);
        if (d == null) throw new IOException("Unknown message type: " + type);
        return d.deserialize(in);
    }

    public void send(DataOutputStream out, Message msg) throws IOException {
        int id = classes.get(msg.getClass());
        out.writeInt(id);
        msg.serialize(out);
        out.flush();
    }
}
