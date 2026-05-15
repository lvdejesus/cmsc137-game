package client.components;

import framework.engine.Component;

public class WallComponent implements Component {
    private boolean active = true;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
