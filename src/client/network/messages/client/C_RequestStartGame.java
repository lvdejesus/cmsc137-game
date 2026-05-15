package client.network.messages.client;

import client.network.messages.Message;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class C_RequestStartGame implements Message {
    public static final C_RequestStartGame INSTANCE = new C_RequestStartGame();

    public C_RequestStartGame() {}

    @Override
    public void serialize(DataOutputStream out) throws IOException {
    }

    public static C_RequestStartGame deserialize(DataInputStream in) {
        return INSTANCE;
    }
}