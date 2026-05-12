package client.network.messages.server;

import client.network.messages.Message;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class S_StartGame implements Message {
    public void serialize(DataOutputStream out) throws IOException {
    }

    public static S_StartGame deserialize(DataInputStream in) {
        System.out.println("S_StartGame{}");
        return new S_StartGame();
    }
}