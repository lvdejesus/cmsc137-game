package client.network.messages.client;

import client.network.messages.Registry;

public class ClientRegistry extends Registry {
    public ClientRegistry() {
        register(C_PlayerState.class, C_PlayerState::deserialize);
        register(C_Shoot.class, C_Shoot::deserialize);
        register(C_RequestStartGame.class, C_RequestStartGame::deserialize);
        register(C_Disconnect.class, C_Disconnect::deserialize);
    }
}
