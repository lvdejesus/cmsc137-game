package client.systems.client;

import client.components.RenderComponent;
import client.components.TransformComponent;
import client.components.UiComponent;
import client.rendering.Camera;
import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.IteratingEntitySystem;
import client.rendering.Batch;
import client.rendering.Texture;
import framework.rendering.ShaderProgram;

import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL20.*;


public class RenderSystem extends IteratingEntitySystem<Context> {
    private ComponentMapper<RenderComponent> rm;
    private ComponentMapper<TransformComponent> tm;
    private ComponentMapper<UiComponent> um;
    private final Batch batch;
    private final Camera camera;
    private final String layer;
    private int activeShaderId = -1;
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
        this.um =engine.getMapper(UiComponent.class);
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
        UiComponent ui = um.get(id);

        // Only applies to specified layer
        if (!rc.layer.equals(layer)) {return;}

        
        // Manages shaders

        // If not a ui component default
        String vertPath = (ui != null) ? ui.shaderVert : "res/shaders/default.vert";
        String fragPath = (ui != null) ? ui.shaderFrag : "res/shaders/default.frag";
        
        Texture tex = rc.texture;
        
        int shader = ShaderProgram.getShaderProgram(vertPath,fragPath);
        
        // Applies new shader if it chanes
        if(shader != activeShaderId){
            // Clears old texture to apply this one
            batch.flush();
            // Applies shader
            glUseProgram(shader);
            camera.bind(shader);
            activeShaderId = shader;
        }

        
        if (tex != null) {
            batch.draw(
                tex, 
                tc.position.x, 
                tc.position.y, 
                rc.z,
                tc.rotation, 
                tex.width * tc.scale.x * rc.visualScaleX, 
                tex.height * tc.scale.y, 
                rc.tint.x, rc.tint.y, rc.tint.z, rc.tint.w, 
                tc.anchor
            );
        }
    }
}
