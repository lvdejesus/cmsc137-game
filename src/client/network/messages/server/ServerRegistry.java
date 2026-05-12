package client.network.messages.server;

import client.network.messages.Registry;

public class ServerRegistry extends Registry {
    public ServerRegistry() {
        register(S_AssignId.class, S_AssignId::deserialize);
        register(S_PlayerCount.class, S_PlayerCount::deserialize);
        register(S_StartGame.class, S_StartGame::deserialize);
        register(S_PlayerPosition.class, S_PlayerPosition::deserialize);
        register(S_Snapshot.class, S_Snapshot::deserialize);
        register(S_Spawn.class, S_Spawn::deserialize);
    }
}