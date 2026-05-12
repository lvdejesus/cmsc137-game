package client.components.player;
import framework.engine.Component;

public class PlayerNetworkComponent implements Component{
    public int playerIndex;

    public PlayerNetworkComponent(int playerIndex) {
        this.playerIndex = playerIndex;
    }
}
