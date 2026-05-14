package editor.components;

import framework.engine.Component;

public class SidebarTileComponent implements Component {
    public int tileIndex;
    public boolean solid;

    public SidebarTileComponent(int tileIndex, boolean solid) {
        this.tileIndex = tileIndex;
        this.solid = solid;
    }
}
