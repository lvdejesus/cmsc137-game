package client.network.messages.client;

import client.network.messages.Registry;

public class ClientRegistry extends Registry {
    public ClientRegistry() {
        register(C_PlayerPosition.class, C_PlayerPosition::deserialize);
    }
}
