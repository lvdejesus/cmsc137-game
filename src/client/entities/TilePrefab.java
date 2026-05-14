package client.entities;

import client.components.CollisionComponent;
import client.components.WallComponent;
import client.systems.client.Context;
import editor.components.TileGridComponent;
import editor.util.TileRegistry;
import framework.engine.Engine;
import framework.engine.Entity;
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
            e.addComponent(tgc);
        } else {
            tgc = engine.getMapper(TileGridComponent.class).get(tgcs.getFirst());
        }

        tgc.tiles = TileRegistry.loadTileTextures();
        placeTile(entity, tgc, x, y, tileIndex);
    }

    @Override
    public void spawnCommon() {
        entity.addComponent(new CollisionComponent(new AABBf((float) x, (float) y, 0.0f, x + 48.0f, y + 48.0f, 0.1f)));
        entity.addComponent(new WallComponent());
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
