package client.entities;

import client.components.RenderComponent;
import client.components.TransformComponent;
import client.rendering.Anchor;
import client.systems.client.Context;
import common.TileDefinition;
import editor.components.TileGridComponent;
import editor.components.TileComponent;
import framework.engine.Engine;
import framework.engine.Entity;
import org.joml.Vector2f;

public class Tile {
    public static final int[] remap = {
        0, 12, 1, 13, 4, 8, 5, 9, 3, 15, 2, 14, 7, 11, 6, 10
    };

    public static void placeTile(Entity<Context> tileEntity, TileGridComponent ec, int xTile, int yTile, int tileIndex) {
        RenderComponent rc;

        var tile = ec.tiles.get(tileIndex);
        float z = 0.0f;
        if (tileIndex >= 0 && tileIndex < ec.tileDefs.size()) {
            TileDefinition def = ec.tileDefs.get(tileIndex);
            if ("floor".equals(def.name)) {
                z = -5.0f;
            }
        }

        if (tile.type == TileDefinition.TileTextureType.regular) {
            rc = new RenderComponent(tile.textures.get(remap[0]), z);
        } else if (tile.type == TileDefinition.TileTextureType.connected) {
            int idx = getConnectionIndex(ec, xTile, yTile);
            rc = new RenderComponent(tile.textures.get(remap[idx]), z);
        } else {
            throw new RuntimeException("Invalid TileTextureType");
        }

        TileComponent etc = new TileComponent(tileIndex);

        tileEntity.addComponent(rc);
        tileEntity.addComponent(etc);

        ec.setEntity(xTile, yTile, tileEntity, tileIndex);

        updateTile(ec, xTile, yTile - 1);
        updateTile(ec, xTile + 1, yTile);
        updateTile(ec, xTile, yTile + 1);
        updateTile(ec, xTile - 1, yTile);
    }

    public static Entity<Context> placeTile(Engine<Context> engine, TileGridComponent ec, int xTile, int yTile, int tileIndex) {
        Entity<Context> tileEntity = engine.createEntity();

        TransformComponent tc = new TransformComponent(new Vector2f(xTile * 64.0f, yTile * 64.0f),
            new Vector2f(4.0f, 4.0f), Anchor.TOP_LEFT);

        placeTile(tileEntity, ec, xTile, yTile, tileIndex);

        tileEntity.addComponent(tc);

        return tileEntity;
    }

    public static boolean isConnectable(TileGridComponent ec, int x, int y) {
        Entity<Context> entity = ec.getEntity(x, y);
        if (entity == null) return false;
        TileComponent tc = entity.getComponent(TileComponent.class);
        if (tc == null) return false;
        if (tc.tile >= 0 && tc.tile < ec.tileDefs.size()) {
            TileDefinition def = ec.tileDefs.get(tc.tile);
            if ("floor".equals(def.name)) {
                return false;
            }
        }
        return true;
    }

    public static int getConnectionIndex(TileGridComponent ec, int xTile, int yTile) {
        boolean d1 = isConnectable(ec, xTile, yTile - 1);
        boolean d2 = isConnectable(ec, xTile + 1, yTile);
        boolean d3 = isConnectable(ec, xTile, yTile + 1);
        boolean d4 = isConnectable(ec, xTile - 1, yTile);
        return (d1 ? 1 : 0) + (d2 ? 2 : 0) + (d3 ? 4 : 0) + (d4 ? 8 : 0);
    }

    public static void updateTile(TileGridComponent ec, int xTile, int yTile) {
        Entity<Context> tileEntity2 = ec.getEntity(xTile, yTile);
        if (tileEntity2 == null) return;

        TileComponent etc = tileEntity2.getComponent(TileComponent.class);
        TileGridComponent.TileTexture tile = ec.tiles.get(etc.tile);
        if (tile.type != TileDefinition.TileTextureType.connected) return;

        int idx2 = getConnectionIndex(ec, xTile, yTile);
        RenderComponent rc2 = tileEntity2.getComponent(RenderComponent.class);
        rc2.texture = tile.textures.get(remap[idx2]);
    }
}
