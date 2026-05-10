package editor.util;

import editor.components.TileComponent;
import framework.engine.Engine;
import framework.json.JsonPair;
import framework.json.JsonReader;
import framework.json.JsonValue;
import client.rendering.Texture;
import client.rendering.TextureAtlas;
import client.systems.Context;
import org.joml.Vector2f;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TileRegistry {
    public static class TileDefinition {
        public String name;
        public String textureFile;
        public int index;
        public boolean solid;
        public Texture texture;

        public TileDefinition(String name, String textureFile, int index, boolean solid) {
            this.name = name;
            this.textureFile = textureFile;
            this.index = index;
            this.solid = solid;
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

                    String textureFile = "grass.png";
                    int index = 0;
                    boolean solid = false;

                    Optional<JsonPair> tilePair;
                    while (true) {
                        tilePair = value.getPair();
                        if (tilePair.isEmpty()) break;

                        String key = tilePair.get().key();
                        switch (key) {
                            case "texture":
                                textureFile = tilePair.get().value().getString();
                                break;
                            case "index":
                                index = tilePair.get().value().getInt();
                                break;
                            case "solid":
                                solid = tilePair.get().value().getInt() == 1;
                                break;
                        }
                    }

                    tiles.add(new TileDefinition(name, textureFile, index, solid));
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

    public static void setSolid(int index, boolean solid) {
        tiles.get(index).solid = solid;
    }

    public static void saveTiles() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        for (int i = 0; i < tiles.size(); i++) {
            TileDefinition tile = tiles.get(i);
            sb.append("    \"").append(tile.name).append("\": {\n");
            sb.append("        \"texture\": \"").append(tile.textureFile).append("\",\n");
            sb.append("        \"index\": ").append(tile.index).append(",\n");
            sb.append("        \"solid\": ").append(tile.solid ? 1 : 0).append("\n");
            sb.append("    }");
            if (i < tiles.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("}\n");
        
        java.nio.file.Files.writeString(
            java.nio.file.Path.of("./res/textures/tiles/tiles.json"),
            sb.toString()
        );
    }

    public static int getTileCount() {
        return tiles.size();
    }
}