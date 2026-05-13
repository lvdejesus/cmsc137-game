package editor.systems;

import client.components.ClickEvent;
import client.components.RenderComponent;
import client.components.TransformComponent;
import client.rendering.Anchor;
import client.systems.client.Context;
import editor.components.EditorComponent;
import editor.components.EditorTileComponent;
import editor.util.TileRegistry;
import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.Entity;
import framework.engine.IteratingEntitySystem;
import org.joml.Vector2f;

public class EditorSystem extends IteratingEntitySystem<Context> {
    private ComponentMapper<EditorComponent> em;
    private ComponentMapper<EditorTileComponent> etm;
    private ComponentMapper<ClickEvent> cem;

    private static final int[] remap = {
        0, 12, 1, 13, 4, 8, 5, 9, 3, 15, 2, 14, 7, 11, 6, 10
    };

    public EditorSystem() {
        super(EditorComponent.class);
    }

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);

        this.em = engine.getMapper(EditorComponent.class);
        this.etm = engine.getMapper(EditorTileComponent.class);
        this.cem = engine.getMapper(ClickEvent.class);
    }

    @Override
    protected void processEntity(int entityId, Context ctx) {
        EditorComponent ec = em.get(entityId);
        ClickEvent ce = cem.get(entityId);

        if (ce != null && ec.currentTile != null) {
            int xTile = (int) Math.floor((ce.x) / 64.0f);
            int yTile = (int) Math.floor((ce.y) / 64.0f);

            Entity<Context> tileEntity = ec.getEntity(xTile, yTile);
            if (tileEntity != null) {
                RenderComponent rc = tileEntity.getComponent(RenderComponent.class);
                EditorTileComponent etc = tileEntity.getComponent(EditorTileComponent.class);
                etc.tile = ec.currentTile;

                var tile = ec.tiles.get(ec.currentTile);
                if (tile.type == TileRegistry.TileTextureType.regular) {
                    rc.texture = tile.textures.getFirst();
                } else if (tile.type == TileRegistry.TileTextureType.connected) {
                    int idx = getConnectionIndex(ec, xTile, yTile);
                    rc.texture = tile.textures.get(remap[idx]);
                }

                updateTile(ec, xTile, yTile - 1);
                updateTile(ec, xTile + 1, yTile);
                updateTile(ec, xTile, yTile + 1);
                updateTile(ec, xTile - 1, yTile);
            } else {
                placeTile(engine, ec, xTile, yTile, ec.currentTile);
            }
        }
    }

    static void placeTile(Engine<Context> engine, EditorComponent ec, int xTile, int yTile, int tileIndex) {
        Entity<Context> tileEntity = engine.createEntity();

        TransformComponent tc = new TransformComponent(new Vector2f(xTile * 64.0f, yTile * 64.0f),
            new Vector2f(4.0f, 4.0f), Anchor.TOP_LEFT);

        RenderComponent rc;

        var tile = ec.tiles.get(tileIndex);
        if (tile.type == TileRegistry.TileTextureType.regular) {
            rc = new RenderComponent(tile.textures.get(remap[0]), 1);
        } else if (tile.type == TileRegistry.TileTextureType.connected) {
            int idx = getConnectionIndex(ec, xTile, yTile);
            rc = new RenderComponent(tile.textures.get(remap[idx]), 1);
        } else {
            throw new RuntimeException("Invalid TileTextureType");
        }

        EditorTileComponent etc = new EditorTileComponent(tileIndex);

        tileEntity.addComponent(rc);
        tileEntity.addComponent(tc);
        tileEntity.addComponent(etc);

        ec.setEntity(xTile, yTile, tileEntity, tileIndex);

        updateTile(ec, xTile, yTile - 1);
        updateTile(ec, xTile + 1, yTile);
        updateTile(ec, xTile, yTile + 1);
        updateTile(ec, xTile - 1, yTile);
    }

    private static int getConnectionIndex(EditorComponent ec, int xTile, int yTile) {
        boolean d1 = ec.getEntity(xTile, yTile - 1) != null;
        boolean d2 = ec.getEntity(xTile + 1, yTile) != null;
        boolean d3 = ec.getEntity(xTile, yTile + 1) != null;
        boolean d4 = ec.getEntity(xTile - 1, yTile) != null;
        return (d1 ? 1 : 0) + (d2 ? 2 : 0) + (d3 ? 4 : 0) + (d4 ? 8 : 0);
    }

    private static void updateTile(EditorComponent ec, int xTile, int yTile) {
        Entity<Context> tileEntity2 = ec.getEntity(xTile, yTile);
        if (tileEntity2 == null) return;

        EditorTileComponent etc = tileEntity2.getComponent(EditorTileComponent.class);
        EditorComponent.TileTexture tile = ec.tiles.get(etc.tile);
        if (tile.type != TileRegistry.TileTextureType.connected) return;

        int idx2 = getConnectionIndex(ec, xTile, yTile);
        RenderComponent rc2 = tileEntity2.getComponent(RenderComponent.class);
        rc2.texture = tile.textures.get(remap[idx2]);
    }
}
