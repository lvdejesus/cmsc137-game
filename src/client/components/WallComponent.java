package client.components;

import framework.engine.Component;

public class WallComponent implements Component {
    private boolean active = true;
    public boolean isStatic;

    public WallComponent() {}

    public WallComponent(boolean isStatic) {
        this.isStatic = isStatic;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
