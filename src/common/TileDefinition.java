package common;

public class TileDefinition {
    public String name;
    public String textureFile;
    public boolean solid;
    public boolean door;
    public boolean isStatic;
    public TileTextureType type;

    public int tileWidth;
    public int tileHeight;

    public TileDefinition(String name, String textureFile, boolean solid, boolean door, boolean isStatic, int tileWidth, int tileHeight, TileTextureType type) {
        this.name = name;
        this.textureFile = textureFile;
        this.solid = solid;
        this.door = door;
        this.isStatic = isStatic;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.type = type;
    }

    public enum TileTextureType {
        regular,
        connected,
        animated,
    }
}
