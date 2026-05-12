package client.network.messages.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import client.network.messages.Message;
import client.network.messages.Registry;

public class ServerRegistry extends Registry {
    public ServerRegistry() {
        register(S_AssignId.class, S_AssignId::deserialize);
        register(S_PlayerCount.class, S_PlayerCount::deserialize);
        register(S_StartGame.class, S_StartGame::deserialize);
        register(S_PlayerPosition.class, S_PlayerPosition::deserialize);
    }
}