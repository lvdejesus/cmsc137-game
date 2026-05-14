package client.entities;

import client.components.CollisionComponent;
import client.components.TransformComponent;
import client.components.WallComponent;
import client.rendering.Anchor;
import client.systems.client.Context;
import editor.components.TileGridComponent;
import common.TileLoader;
import framework.engine.Engine;
import framework.engine.Entity;
import org.joml.Vector2f;
import org.joml.primitives.AABBf;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import static client.entities.Tile.placeTile;

public class TilePrefab extends Prefab{
    int networkId;
    int x;
    int y;
    int tileIndex;

    public TilePrefab(Engine<Context> engine, int networkId, int x, int y, int tileIndex) {
        super(engine);

        this.networkId = networkId;
        this.x = x;
        this.y = y;
        this.tileIndex = tileIndex;
    }

    @Override
    public void spawnClientInternal() {
        List<Integer> tgcs = engine.getFamily(TileGridComponent.class).toList();
        TileGridComponent tgc;

        // TODO: this is inefficient, keep a reference somewhere for the tgc to be passed.
        if (tgcs.isEmpty()) {
            Entity<Context> e = engine.createEntity();
            tgc = new TileGridComponent();
            try {
                tgc.tiles = TileLoader.loadTileTextures(TileLoader.loadTiles());
            } catch (IOException err) {
                throw new RuntimeException(err);
            }
            e.addComponent(tgc);
        } else {
            tgc = engine.getMapper(TileGridComponent.class).get(tgcs.getFirst());
        }

        placeTile(entity, tgc, x, y, tileIndex);
    }

    @Override
    public void spawnCommon() {
        entity.addComponent(new CollisionComponent(new AABBf(0.0f, 0.0f, 0.0f, 48.0f, 48.0f, 0.1f)));
        entity.addComponent(new WallComponent());
        entity.addComponent(new TransformComponent(new Vector2f(x * 64.0f, y * 64.0f),
            new Vector2f(4.0f, 4.0f), Anchor.TOP_LEFT));
    }

    public static TilePrefab deserialize(Engine<Context> engine, int networkId, ByteBuffer bytes) throws IOException {
        return new TilePrefab(engine, networkId,bytes.getInt(), bytes.getInt(), bytes.getInt());
    }

    public static byte[] serialize(int x, int y, int tileIndex) {
        ByteBuffer bytes = ByteBuffer.allocate(12);

        bytes.putInt(x);
        bytes.putInt(y);
        bytes.putInt(tileIndex);

        return bytes.array();
    }
}
