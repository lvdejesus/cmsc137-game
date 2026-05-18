package client.network.messages.client;

import client.network.messages.Message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class C_ChatMessage implements Message {
    private final String text;

    public C_ChatMessage(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        out.writeUTF(text);
    }

    public static C_ChatMessage deserialize(DataInputStream in) throws IOException {
        return new C_ChatMessage(in.readUTF());
    }
}
