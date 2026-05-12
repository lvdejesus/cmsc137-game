package client.network.messages.server;

import client.network.messages.Message;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class S_PlayerCount implements Message {
    private final int count;

    public S_PlayerCount(int count) {
        this.count = count;
    }

    public int getCount() { return count; }

    public void serialize(DataOutputStream out) throws IOException {
        out.writeInt(count);
    }

    public static S_PlayerCount deserialize(DataInputStream in) throws IOException {
        int count = in.readInt();
        System.out.println("S_PlayerCount{count=" + count + "}");
        return new S_PlayerCount(count);
    }
}