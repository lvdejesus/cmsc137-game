package client.systems;

import client.components.RenderComponent;
import client.components.TransformComponent;
import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.IteratingEntitySystem;
import client.rendering.Batch;
import client.rendering.Texture;

public class RenderSystem extends IteratingEntitySystem<Context> {
    private ComponentMapper<RenderComponent> rm;
    private ComponentMapper<TransformComponent> tm;
    private final Batch batch;

    public RenderSystem() {
        super(RenderComponent.class, TransformComponent.class);
        batch = new Batch();
    }

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);

        this.rm = engine.getMapper(RenderComponent.class);
        this.tm = engine.getMapper(TransformComponent.class);
    }

    @Override
    public void update(Context ctx) {
        super.update(ctx);
        batch.flush();
    }

    @Override
    public void processEntity(int id, Context ctx) {
        RenderComponent rc = rm.get(id);
        TransformComponent tc = tm.get(id);

        Texture tex = rc.texture;
        if (tex != null) {
            batch.draw(tex, tc.position.x, tc.position.y, rc.z,
                    tc.rotation, rc.texture.width * tc.scale.x, rc.texture.height * tc.scale.y, rc.tint.x, rc.tint.y, rc.tint.z, rc.tint.w, tc.anchor);
        }
    }
}
