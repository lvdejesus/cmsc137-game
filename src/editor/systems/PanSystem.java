package editor.systems;

import client.systems.Context;
import client.systems.InputHandler;
import client.rendering.Camera;
import framework.engine.Engine;
import framework.engine.EntitySystem;

public class PanSystem extends EntitySystem<Context> {
    private final Camera camera;

    public PanSystem(Camera camera) {
        super();
        this.camera = camera;
    }

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);
    }

    @Override
    public void update(Context ctx) {
        super.update(ctx);

        InputHandler input = InputHandler.getInstance();
        if (input.middleMouseHeld) {
            camera.position.x -= input.middleMouseDragDelta.x;
            camera.position.y -= input.middleMouseDragDelta.y;
        }
    }

    @Override
    protected void processEntity(int entityId, Context ctx) {

    }
}