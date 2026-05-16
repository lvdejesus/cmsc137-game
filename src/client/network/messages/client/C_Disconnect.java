package client.network.messages.client;

import client.network.messages.Message;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class C_Disconnect implements Message {
    public static final C_Disconnect INSTANCE = new C_Disconnect();

    public C_Disconnect() {}

    @Override
    public void serialize(DataOutputStream out) throws IOException {
    }

    public static C_Disconnect deserialize(DataInputStream in) {
        return INSTANCE;
    }
}
