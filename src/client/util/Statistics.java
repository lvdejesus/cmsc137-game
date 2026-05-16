package client.util;

import java.util.HashMap;
import java.util.Map;

public class Statistics {
    public Map<Integer, Integer> kills = new HashMap<>();

    public void addKill(int playerId) {
        if (!kills.containsKey(playerId)) {
            kills.put(playerId, 0);
        }
        kills.put(playerId, kills.get(playerId) + 1);
    }
}
