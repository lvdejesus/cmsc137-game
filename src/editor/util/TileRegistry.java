package editor.util;

import framework.json.JsonArray;
import framework.json.JsonPair;
import framework.json.JsonReader;
import framework.json.JsonValue;
import client.rendering.Texture;
import client.rendering.TextureAtlas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TileRegistry {
    public static enum TileTextureType {
        regular,
        connected,
        animated,
    }

    public static class TileDefinition {
        public String name;
        public String textureFile;
        public boolean solid;
        public boolean door;
        public Texture texture;
        public TileTextureType type;

        public int tileWidth;
        public int tileHeight;

        public TileDefinition(String name, String textureFile, boolean solid, boolean door, int tileWidth, int tileHeight, TileTextureType type) {
            this.name = name;
            this.textureFile = textureFile;
            this.solid = solid;
            this.door = door;
            this.tileWidth = tileWidth;
            this.tileHeight = tileHeight;
            this.type = type;
        }
    }

    private static final int TILE_SIZE = 16;
    public static List<TileDefinition> tiles = new ArrayList<>();

    public static void loadTiles() throws IOException {
        try (JsonReader reader = new JsonReader("./res/textures/tiles/tiles.json")) {
            Optional<JsonPair> pair;
            while (true) {
                pair = reader.getPair();
                if (pair.isPresent()) {
                    String name = pair.get().key();
                    JsonValue value = pair.get().value();

                    String textureFile = null;
                    boolean solid = false;
                    boolean door = false;
                    int[] dims = new int[2];
                    TileTextureType type = TileTextureType.regular;

                    Optional<JsonPair> tilePair;
                    while (true) {
                        tilePair = value.getPair();
                        if (tilePair.isEmpty()) break;
                        System.out.println(tilePair.get().key());

                        String key = tilePair.get().key();
                        switch (key) {
                            case "texture":
                                textureFile = tilePair.get().value().getString();
                                break;
                            case "size":
                                JsonArray arr = tilePair.get().value().getArray();
                                int count = 0;

                                while (count < 2) {
                                    Optional<JsonValue> item = arr.getItem();
                                    if (item.isPresent()) {
                                        dims[count++] = item.get().getInt();
                                    } else {
                                        break;
                                    }
                                }

                                while (arr.getItem().isPresent()) ;
                                System.out.println("width: " + dims[0] + ", height: " + dims[1]);
                                break;
                            case "solid":
                                solid = tilePair.get().value().getInt() == 1;
                                break;
                            case "door":
                                door = tilePair.get().value().getInt() == 1;
                                break;
                            case "connecting":
                                if(tilePair.get().value().getInt() == 1) {
                                    type = TileTextureType.connected;
                                }
                                break;
                        }
                    }

                    if (textureFile == null) {
                        throw new RuntimeException("Texture missing!");
                    }

                    tiles.add(new TileDefinition(name, textureFile, solid, door, dims[0], dims[1], type));
                } else {
                    break;
                }
            }
        }

        for (TileDefinition tile : tiles) {
            Texture tex = TextureAtlas.get().getRegion("tiles/" + tile.textureFile);
            tile.texture = tex;
        }
    }

    public static TileDefinition getTile(int index) {
        return tiles.get(index);
    }

    public static int getTileCount() {
        return tiles.size();
    }
}