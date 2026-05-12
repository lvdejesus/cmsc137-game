package client.network.messages.server;

import client.network.messages.Message;
import framework.engine.Entity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class S_Snapshot implements Message {
    private List<EntitySnapshot> entitySnapshots;

    public S_Snapshot(List<EntitySnapshot> entitySnapshots) {
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        out.writeInt(entitySnapshots.size());
        for (var entitySnapshot : entitySnapshots) {
            entitySnapshot.serialize(out);
        }
    }

    public static S_Snapshot deserialize(DataInputStream in) throws IOException {
        int length = in.readInt();
        List<EntitySnapshot> entitySnapshots = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            entitySnapshots.add(EntitySnapshot.deserialize(in));
        }

        System.out.println("S_Snapshot{...}");
        return new S_Snapshot(entitySnapshots);
    }
}