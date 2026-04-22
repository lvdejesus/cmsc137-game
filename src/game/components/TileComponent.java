package game.components;

import framework.engine.Component;

public class TileComponent implements Component {
    public int tile;

    public TileComponent(int tile) {
        this.tile = tile;
    }
}
