package client.components;

import framework.engine.Component;

public class NetworkIdComponent implements Component {
    public int networkId;

    public NetworkIdComponent(int networkId) {
        this.networkId = networkId;
    }
}
