package editor.systems;

import client.components.ClickEvent;
import client.components.RenderComponent;
import client.components.TransformComponent;
import client.rendering.Anchor;
import client.systems.Context;
import editor.components.EditorComponent;
import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.Entity;
import framework.engine.IteratingEntitySystem;
import org.joml.Vector2f;

public class EditorSystem extends IteratingEntitySystem<Context> {
    private ComponentMapper<EditorComponent> em;
    private ComponentMapper<ClickEvent> cem;

    public EditorSystem() {
        super(EditorComponent.class);
    }

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);

        this.em = engine.getMapper(EditorComponent.class);
        this.cem = engine.getMapper(ClickEvent.class);
    }

    @Override
    protected void processEntity(int entityId, Context ctx) {
        EditorComponent ec = em.get(entityId);
        ClickEvent ce = cem.get(entityId);

        if (ce != null && ec.currentTile != null) {
            int xTile = (int) Math.floor((ce.x - 200.0f) / 64.0f);
            int yTile = (int) Math.floor((ce.y - 200.0f) / 64.0f);

            Entity<Context> tileEntity = ec.getEntity(xTile, yTile);
            if (tileEntity != null) {
                RenderComponent rc = tileEntity.getComponent(RenderComponent.class);
                rc.texture = ec.tiles.get(ec.currentTile);
            } else {
                tileEntity = engine.createEntity();

                TransformComponent tc = new TransformComponent(new Vector2f(200.0f + xTile * 64.0f, 200.0f + yTile * 64.0f),
                    new Vector2f(4.0f, 4.0f), Anchor.TOP_LEFT);
                RenderComponent rc = new RenderComponent(ec.tiles.get(ec.currentTile), 1);

                tileEntity.addComponent(rc);
                tileEntity.addComponent(tc);

                ec.setEntity(xTile, yTile, tileEntity);
            }
        }

    }
}
