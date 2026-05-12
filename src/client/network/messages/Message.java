package client.network.messages;

import client.network.messages.server.S_PlayerCount;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface Message {
    void serialize(DataOutputStream out) throws IOException;
}