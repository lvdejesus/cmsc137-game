package client.network.messages.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EntitySnapshot {
    private final int networkId;
    private final List<ComponentSnapshot> components;

    public EntitySnapshot(int networkId, List<ComponentSnapshot> components) {
        this.networkId = networkId;
        this.components = components;
    }

    public void serialize(DataOutputStream out) throws IOException {
        out.writeInt(networkId);
        out.writeInt(components.size());
        for (var component : components) {
            component.serialize(out);
        }
    }

    public static EntitySnapshot deserialize(DataInputStream in) throws IOException {
        int networkId = in.readInt();
        int length = in.readInt();
        List<ComponentSnapshot> components = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            components.add(ComponentSnapshot.deserialize(in));
        }

        return new EntitySnapshot(networkId, components);
    }

    public int getNetworkId() {
        return networkId;
    }

    public List<ComponentSnapshot> getComponents() {
        return components;
    }

}
