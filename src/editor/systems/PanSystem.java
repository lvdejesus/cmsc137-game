package editor.systems;

import client.systems.client.Context;
import client.systems.client.InputHandler;
import client.rendering.Camera;
import framework.engine.Engine;
import framework.engine.IteratingEntitySystem;

public class PanSystem extends IteratingEntitySystem<Context> {
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