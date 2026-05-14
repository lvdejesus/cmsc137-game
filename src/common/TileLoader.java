package common;

import editor.components.TileGridComponent;
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

public class TileLoader {
    private static final int TILE_SIZE = 16;

    public static List<TileDefinition> loadTiles() throws IOException {
        List<TileDefinition> tiles = new ArrayList<>();
        try (JsonReader reader = new JsonReader("./res/textures/tiles/tiles.json")) {
            Optional<JsonPair> pair;
            while (true) {
                pair = reader.getPair();
                if (pair.isEmpty())
                    break;
                String name = pair.get().key();
                JsonValue value = pair.get().value();

                String textureFile = null;
                boolean solid = false;
                boolean door = false;
                int[] dims = new int[2];
                TileDefinition.TileTextureType type = TileDefinition.TileTextureType.regular;

                Optional<JsonPair> tilePair;
                while (true) {
                    tilePair = value.getPair();
                    if (tilePair.isEmpty()) break;

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
                            break;
                        case "solid":
                            solid = tilePair.get().value().getInt() == 1;
                            break;
                        case "door":
                            door = tilePair.get().value().getInt() == 1;
                            break;
                        case "connecting":
                            if (tilePair.get().value().getInt() == 1) {
                                type = TileDefinition.TileTextureType.connected;
                            }
                            break;
                    }

                }
                if (textureFile == null) {
                    throw new RuntimeException("Texture missing!");
                }

                tiles.add(new TileDefinition(name, textureFile, solid, door, dims[0], dims[1], type));
            }
        }

        return tiles;
    }

    public static List<TileGridComponent.TileTexture> loadTileTextures(List<TileDefinition> tiles) {
        ArrayList<TileGridComponent.TileTexture> tileTextures = new ArrayList<>();

        for (TileDefinition tile : tiles) {
            String regionKey = "tiles/" + tile.textureFile;
            Texture t = TextureAtlas.get().getRegion(regionKey);

            int xCount = t.width / (TILE_SIZE * tile.tileWidth);
            int yCount = t.height / (TILE_SIZE * tile.tileHeight);

            List<Texture> textures;

            if (tile.type == TileDefinition.TileTextureType.regular) {
                int i = 0;
                int j = 0;

                float du = (t.u2 - t.u1) / xCount;
                float dv = (t.v2 - t.v1) / yCount;

                float u1 = t.u1 + du * j;
                float u2 = t.u1 + du * (j + 1);
                float v1 = t.v1 + dv * i;
                float v2 = t.v1 + dv * (i + 1);

                Texture tex = new Texture(u1, v1, u2, v2, TILE_SIZE * tile.tileWidth, TILE_SIZE * tile.tileHeight);

                textures = new ArrayList<>(List.of(new Texture[]{tex}));
                tileTextures.add(new TileGridComponent.TileTexture(TileDefinition.TileTextureType.regular, textures));
            } else if (tile.type == TileDefinition.TileTextureType.connected) {
                textures = new ArrayList<>();

                for (int i = 0; i < yCount; i++) {
                    for (int j = 0; j < xCount; j++) {
                        float du = (t.u2 - t.u1) / xCount;
                        float dv = (t.v2 - t.v1) / yCount;

                        float u1 = t.u1 + du * j;
                        float u2 = t.u1 + du * (j + 1);
                        float v1 = t.v1 + dv * (yCount - i - 1);
                        float v2 = t.v1 + dv * (yCount - i);

                        Texture tex = new Texture(u1, v1, u2, v2, TILE_SIZE * tile.tileWidth, TILE_SIZE * tile.tileHeight);
                        textures.add(tex);
                    }
                }
                tileTextures.add(new TileGridComponent.TileTexture(TileDefinition.TileTextureType.connected, textures));
            } else {
                throw new RuntimeException("Invalid texture type.");
            }

        }
        return tileTextures;
    }
}