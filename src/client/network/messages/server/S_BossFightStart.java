package client.network.messages.server;

import client.network.messages.Message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class S_BossFightStart implements Message {
    public S_BossFightStart() {
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
    }

    public static S_BossFightStart deserialize(DataInputStream in) throws IOException {
        return new S_BossFightStart();
    }
}
