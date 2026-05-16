package client.systems.client;

import client.components.RenderComponent;
import client.components.TransformComponent;
import client.components.UiComponent;
import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.IteratingEntitySystem;
import client.rendering.*;

public class UiRenderSystem extends IteratingEntitySystem<Context>{
    private ComponentMapper<RenderComponent> rm;
    private ComponentMapper<TransformComponent> tm;
    private ComponentMapper<UiComponent> um;
    private final Batch batch = new Batch();
    private final String targetLayer;

    public UiRenderSystem(String targetLayer) {
        super(RenderComponent.class,TransformComponent.class);
        this.targetLayer = targetLayer;
    }

    @Override
    public void setEngine(Engine<Context>engine) {
        super.setEngine(engine);
        this.rm = engine.getMapper(RenderComponent.class);
        this.tm = engine.getMapper(TransformComponent.class);
        this.um = engine.getMapper(UiComponent.class);
    }

    @Override
    protected void processEntity(int id, Context ctx) {
        RenderComponent rc = rm.get(id);
        TransformComponent tc = tm.get(id);
        UiComponent ui = um.get(id);
        float dt = ctx.deltaTime;

        // Only renders if entity is in target layer
        if(!rc.layer.equals(targetLayer)) return;

        // Manages Position Tweening
        tc.position.lerp(ui.targetPosistion, ui.lerpSpeed*dt);
        tc.rotation += (ui.targetRotation - tc.rotation * ui.lerpSpeed * dt);
        
        // Manages Opacity Tweening
        rc.tint.lerp(ui.targetTint, ui.lerpSpeed * dt);

        // Managing Pinching
        float targetVWidth = (ui.currentState == ui.targetState) ? 1.0f : 0.0f;
        rc.visualScaleX += (targetVWidth = rc.visualScaleX) * ui.lerpSpeed * dt;
        

    }
}
