package client.entities;

import client.components.*;
import client.rendering.*;
import client.systems.client.Context;
import common.MapGenerator;
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

public class TilePrefab extends Prefab {
    int networkId;
    int x;
    int y;
    int tileIndex;
    boolean isBoss;
    int bx;
    int by;

    public TilePrefab(Engine<Context> engine, int networkId, int x, int y, int tileIndex) {
        super(engine);
        this.networkId = networkId;
        this.x = x;
        this.y = y;
        this.tileIndex = tileIndex;
        this.isBoss = false;
    }

    public TilePrefab(Engine<Context> engine, int networkId, int x, int y, int tileIndex, int bx, int by) {
        super(engine);
        this.networkId = networkId;
        this.x = x;
        this.y = y;
        this.tileIndex = tileIndex;
        this.isBoss = true;
        this.bx = bx;
        this.by = by;
    }

    @Override
    public void spawnClientInternal() {
        List<Integer> tgcs = engine.getFamily(TileGridComponent.class).toList();
        TileGridComponent tgc;

        if (tgcs.isEmpty()) {
            Entity<Context> e = engine.createEntity();
            tgc = new TileGridComponent();
            try {
                tgc.tileDefs = TileLoader.loadTiles();
                tgc.tiles = TileLoader.loadTileTextures(tgc.tileDefs);
            } catch (IOException err) {
                throw new RuntimeException(err);
            }
            e.addComponent(tgc);
        } else {
            tgc = engine.getMapper(TileGridComponent.class).get(tgcs.getFirst());
        }

        placeTile(entity, tgc, x, y, tileIndex);

        var def = tgc.tileDefs.get(tileIndex);
        if (def != null && def.door) {
            String regionKey = "tiles/" + def.textureFile;
            Texture doorTex = TextureAtlas.get().getRegion(regionKey);
            int frameW = 16;
            int frameCount = doorTex.width / frameW;
            float du = (doorTex.u2 - doorTex.u1) / frameCount;
            Texture[] frames = new Texture[frameCount];
            for (int i = 0; i < frameCount; i++) {
                float u1 = doorTex.u1 + du * i;
                float u2 = doorTex.u1 + du * (i + 1);
                frames[i] = new Texture(u1, doorTex.v1, u2, doorTex.v2, frameW, doorTex.height);
            }
            DoorComponent dc = entity.getComponent(DoorComponent.class);
            if (dc != null) dc.frames = frames;
            entity.getComponent(RenderComponent.class).texture = frames[0];
        }
    }

    private TileGridComponent getTGC() {
        List<Integer> tgcs = engine.getFamily(TileGridComponent.class).toList();
        TileGridComponent tgc;
        if (tgcs.isEmpty()) {
            Entity<Context> e = engine.createEntity();
            tgc = new TileGridComponent();
            try {
                tgc.tileDefs = TileLoader.loadTiles();
                tgc.tiles = TileLoader.loadTileTextures(tgc.tileDefs);
            } catch (IOException err) {
                throw new RuntimeException(err);
            }
            e.addComponent(tgc);
        } else {
            tgc = engine.getMapper(TileGridComponent.class).get(tgcs.getFirst());
        }
        return tgc;
    }

    @Override
    public void spawnCommon() {
        TileGridComponent tgc = getTGC();

        var tileDef = tgc.tileDefs.get(tileIndex);
        if (!"floor".equals(tileDef.name)) {
            entity.addComponent(new CollisionComponent(new AABBf(0.0f, 0.0f, 0.0f, 64.0f, 64.0f, 0.1f)));
        }
        if (tileDef.solid) {
            entity.addComponent(new WallComponent(tileDef.isStatic));
        }
        entity.addComponent(new TransformComponent(new Vector2f(x * 64.0f, y * 64.0f),
            new Vector2f(4.0f, 4.0f), Anchor.TOP_LEFT));
        if (tgc.tileDefs.get(tileIndex).door) {
            entity.addComponent(new DoorComponent(x, y, tileIndex));
            if (!isBoss) {
                entity.addComponent(new NetworkDuplicateComponent(DoorComponent.class, WallComponent.class));
            } else {
                entity.addComponent(new NetworkDuplicateComponent(DoorComponent.class, WallComponent.class));
            }
        }
        entity.addComponent(new NetworkIdComponent(networkId));
    }

    @Override
    public void spawnServerInternal() {
        if (isBoss) {
            entity.addComponent(new BossDoorComponent(bx, by));
            entity.addComponent(new NetworkDuplicateComponent(DoorComponent.class, WallComponent.class));
        } else {
            var tgc = getTGC();
            if (tgc.tileDefs.get(tileIndex).door) {
                entity.addComponent(new DoorComponent(x, y, tileIndex));
                entity.addComponent(new NetworkDuplicateComponent(DoorComponent.class, WallComponent.class));
            }
        }
    }

    public static TilePrefab deserialize(Engine<Context> engine, int networkId, ByteBuffer bytes) throws IOException {
        int x = bytes.getInt();
        int y = bytes.getInt();
        int tileIndex = bytes.getInt();
        boolean isBoss = bytes.get() == 1;

        if (isBoss) {
            int bx = bytes.getInt();
            int by = bytes.getInt();
            return new TilePrefab(engine, networkId, x, y, tileIndex, bx, by);
        } else {
            return new TilePrefab(engine, networkId, x, y, tileIndex);
        }
    }

    public static byte[] serialize(int x, int y, int tileIndex) {
        ByteBuffer bytes = ByteBuffer.allocate(13);
        bytes.putInt(x);
        bytes.putInt(y);
        bytes.putInt(tileIndex);
        bytes.put((byte) 0);
        return bytes.array();
    }

    public static byte[] serialize(int x, int y, int tileIndex, int bx, int by) {
        ByteBuffer bytes = ByteBuffer.allocate(21);
        bytes.putInt(x);
        bytes.putInt(y);
        bytes.putInt(tileIndex);
        bytes.put((byte) 1);
        bytes.putInt(bx);
        bytes.putInt(by);
        return bytes.array();
    }
}
