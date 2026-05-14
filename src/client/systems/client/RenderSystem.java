package client.systems.client;

import client.components.RenderComponent;
import client.components.TransformComponent;
import client.rendering.Camera;
import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.IteratingEntitySystem;
import client.rendering.Batch;
import client.rendering.Texture;
import framework.rendering.ShaderProgram;

import static org.lwjgl.opengl.GL11.*;

public class RenderSystem extends IteratingEntitySystem<Context> {
    private ComponentMapper<RenderComponent> rm;
    private ComponentMapper<TransformComponent> tm;
    private final Batch batch;
    private final Camera camera;
    private final String layer;
    private final java.util.Map<Integer, VisualCache> visualCache = new java.util.HashMap<>();

    public RenderSystem(Camera camera, String layer) {
        super(RenderComponent.class, TransformComponent.class);

        this.camera = camera;
        this.layer = layer;

        batch = new Batch();
    }

    private static class VisualCache {
        float vScalex;
        boolean initialized = false;
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

        int shaderProgram = ShaderProgram.getShaderProgram("res/shaders/default.vert", "res/shaders/default.frag");

        glViewport((int)camera.viewportX, (int)camera.viewportY,
                   (int)camera.viewportWidth, (int)camera.viewportHeight);
        camera.bind(shaderProgram);
        batch.flush();
    }

    @Override
    public void processEntity(int id, Context ctx) {
        RenderComponent rc = rm.get(id);
        TransformComponent tc = tm.get(id);
        float dt = ctx.deltaTime;
        if (!rc.layer.equals(layer)) {return;}

        VisualCache cache = visualCache.computeIfAbsent(id, k -> new VisualCache());
        // If this entity has been rendered before
        if (!cache.initialized){
            cache.vScalex = tc.scale.x;
            cache.initialized = true;
        }
        float targetScalex = (rc.isFlipping) ? 0f : tc.scale.x;
        if (Math.abs(cache.vScalex - targetScalex) > 0.001f) {
            float speed = 8f * dt;
            if (cache.vScalex > targetScalex) {
                cache.vScalex = Math.max(cache.vScalex - speed, targetScalex);
            } else {
                cache.vScalex = Math.min(cache.vScalex   + speed, tc.scale.x);
            }
        }

        Texture tex = rc.texture;
        


        if (tex != null) {
            batch.draw(
                tex, 
                tc.position.x, 
                tc.position.y, 
                rc.z,
                tc.rotation, 
                rc.texture.width * tc.scale.x, 
                rc.texture.height * tc.scale.y, 
                rc.tint.x, rc.tint.y, rc.tint.z, rc.tint.w, 
                tc.anchor
            );
        }
    }
}
