package client.network.messages.client;

import client.components.player.PlayerStateComponent;
import client.network.messages.Message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class C_ApplyUpgrade implements Message {
    public final int index;

    public C_ApplyUpgrade(int index) {
        this.index = index;
    }

    public void serialize(DataOutputStream out) throws IOException {
        out.writeInt(index);
    }

    public static C_ApplyUpgrade deserialize(DataInputStream in) throws IOException {
        int index = in.readInt();

        return new C_ApplyUpgrade(index);
    }
}