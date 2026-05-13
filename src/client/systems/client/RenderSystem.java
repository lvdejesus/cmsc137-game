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

    public RenderSystem(Camera camera, String layer) {
        super(RenderComponent.class, TransformComponent.class);

        this.camera = camera;
        this.layer = layer;

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

        int shaderProgram = ShaderProgram.getShaderProgram("res/shaders/default.vert", "res/shaders/default.frag");

        glViewport((int)camera.viewportX, (int)camera.viewportY,
                   (int)camera.viewportWidth, (int)camera.viewportHeight);
        camera.bind(shaderProgram);
        batch.flush();
    }

    @Override
    public void processEntity(int id, Context ctx) {
        RenderComponent rc = rm.get(id);
        if (!rc.layer.equals(layer)) {
            return;
        }

        TransformComponent tc = tm.get(id);

        Texture tex = rc.texture;
        if (tex != null) {
            batch.draw(tex, tc.position.x, tc.position.y, rc.z,
                    tc.rotation, rc.texture.width * tc.scale.x, rc.texture.height * tc.scale.y, rc.tint.x, rc.tint.y, rc.tint.z, rc.tint.w, tc.anchor);
        }
    }
}
