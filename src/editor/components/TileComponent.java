package editor.components;

import framework.engine.Component;

public class TileComponent implements Component {
    public int tileIndex;
    public boolean solid;

    public TileComponent(int tileIndex, boolean solid) {
        this.tileIndex = tileIndex;
        this.solid = solid;
    }
}
