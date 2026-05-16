package client.components;

import framework.engine.Component;

public class BossDoorComponent implements Component {
    public int bx;
    public int by;

    public BossDoorComponent(int bx, int by) {
        this.bx = bx;
        this.by = by;
    }
}
