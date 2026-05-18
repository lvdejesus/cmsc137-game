package client.network.messages.server;

import client.network.messages.Message;
import client.util.Statistics;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class S_GameResult implements Message {
    public final Map<Integer, Integer> kills;
    public final int bossKills;
    public final boolean victory;

    public S_GameResult(Map<Integer, Integer> kills, int bossKills, boolean victory) {
        this.kills = kills;
        this.bossKills = bossKills;
        this.victory = victory;
    }

    public S_GameResult(Statistics stats, boolean victory) {
        this.kills = new HashMap<>(stats.kills);
        this.bossKills = stats.bossKills;
        this.victory = victory;
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        out.writeInt(kills.size());
        for (Map.Entry<Integer, Integer> entry : kills.entrySet()) {
            out.writeInt(entry.getKey());
            out.writeInt(entry.getValue());
        }
        out.writeInt(bossKills);
        out.writeBoolean(victory);
    }

    public static S_GameResult deserialize(DataInputStream in) throws IOException {
        int mapSize = in.readInt();
        Map<Integer, Integer> kills = new HashMap<>();
        for (int i = 0; i < mapSize; i++) {
            kills.put(in.readInt(), in.readInt());
        }
        int bossKills = in.readInt();
        boolean victory = in.readBoolean();
        return new S_GameResult(kills, bossKills, victory);
    }
}
