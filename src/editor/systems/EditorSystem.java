package editor.systems;

import client.components.ClickEvent;
import client.components.RenderComponent;
import client.systems.client.Context;
import editor.components.TileGridComponent;
import editor.components.TileComponent;
import editor.util.TileRegistry;
import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.Entity;
import framework.engine.IteratingEntitySystem;

import static client.entities.Tile.*;

public class EditorSystem extends IteratingEntitySystem<Context> {
    private ComponentMapper<TileGridComponent> em;
    private ComponentMapper<ClickEvent> cem;

    public EditorSystem() {
        super(TileGridComponent.class);
    }

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);

        this.em = engine.getMapper(TileGridComponent.class);
        this.cem = engine.getMapper(ClickEvent.class);
    }

    @Override
    protected void processEntity(int entityId, Context ctx) {
        TileGridComponent ec = em.get(entityId);
        ClickEvent ce = cem.get(entityId);

        if (ce != null && ec.currentTile != null) {
            int xTile = (int) Math.floor((ce.x) / 64.0f);
            int yTile = (int) Math.floor((ce.y) / 64.0f);

            Entity<Context> tileEntity = ec.getEntity(xTile, yTile);
            if (tileEntity != null) {
                RenderComponent rc = tileEntity.getComponent(RenderComponent.class);
                TileComponent etc = tileEntity.getComponent(TileComponent.class);
                etc.tile = ec.currentTile;

                var tile = ec.tiles.get(ec.currentTile);
                if (tile.type == TileRegistry.TileTextureType.regular) {
                    rc.texture = tile.textures.get(0);
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
}
