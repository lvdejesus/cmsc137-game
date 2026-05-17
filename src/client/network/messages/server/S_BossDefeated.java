package client.network.messages.server;

import client.network.messages.Message;
import client.util.Statistics;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class S_BossDefeated implements Message {
    public final Map<Integer, Integer> kills;
    public final int bossKills;

    public S_BossDefeated(Map<Integer, Integer> kills, int bossKills) {
        this.kills = kills;
        this.bossKills = bossKills;
    }

    public S_BossDefeated(Statistics stats) {
        this.kills = new HashMap<>(stats.kills);
        this.bossKills = stats.bossKills;
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        out.writeInt(kills.size());
        for (Map.Entry<Integer, Integer> entry : kills.entrySet()) {
            out.writeInt(entry.getKey());
            out.writeInt(entry.getValue());
        }
        out.writeInt(bossKills);
    }

    public static S_BossDefeated deserialize(DataInputStream in) throws IOException {
        int mapSize = in.readInt();
        Map<Integer, Integer> kills = new HashMap<>();
        for (int i = 0; i < mapSize; i++) {
            kills.put(in.readInt(), in.readInt());
        }
        int bossKills = in.readInt();
        return new S_BossDefeated(kills, bossKills);
    }
}
