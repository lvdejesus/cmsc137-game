package client.entities;

import client.components.RenderComponent;
import client.components.TransformComponent;
import client.rendering.Anchor;
import client.systems.client.Context;
import editor.components.TileGridComponent;
import editor.components.TileComponent;
import editor.util.TileRegistry;
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
        if (tile.type == TileRegistry.TileTextureType.regular) {
            rc = new RenderComponent(tile.textures.get(remap[0]), 0.0f);
        } else if (tile.type == TileRegistry.TileTextureType.connected) {
            int idx = getConnectionIndex(ec, xTile, yTile);
            rc = new RenderComponent(tile.textures.get(remap[idx]), 0.0f);
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

    public static int getConnectionIndex(TileGridComponent ec, int xTile, int yTile) {
        boolean d1 = ec.getEntity(xTile, yTile - 1) != null;
        boolean d2 = ec.getEntity(xTile + 1, yTile) != null;
        boolean d3 = ec.getEntity(xTile, yTile + 1) != null;
        boolean d4 = ec.getEntity(xTile - 1, yTile) != null;
        return (d1 ? 1 : 0) + (d2 ? 2 : 0) + (d3 ? 4 : 0) + (d4 ? 8 : 0);
    }

    public static void updateTile(TileGridComponent ec, int xTile, int yTile) {
        Entity<Context> tileEntity2 = ec.getEntity(xTile, yTile);
        if (tileEntity2 == null) return;

        TileComponent etc = tileEntity2.getComponent(TileComponent.class);
        TileGridComponent.TileTexture tile = ec.tiles.get(etc.tile);
        if (tile.type != TileRegistry.TileTextureType.connected) return;

        int idx2 = getConnectionIndex(ec, xTile, yTile);
        RenderComponent rc2 = tileEntity2.getComponent(RenderComponent.class);
        rc2.texture = tile.textures.get(remap[idx2]);
    }
}
