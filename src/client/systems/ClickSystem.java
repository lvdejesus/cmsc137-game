package client.systems;

import client.components.ClickableComponent;
import client.rendering.Camera;
import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.EntitySystem;
import org.joml.Vector3f;

public class ClickSystem extends EntitySystem<Context> {
    private ComponentMapper<ClickableComponent> cm;
    private Camera camera;

    public ClickSystem(Camera camera) {
        super(ClickableComponent.class);

        this.camera = camera;
    }

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);

        cm = engine.getMapper(ClickableComponent.class);
    }

    @Override
    public void processEntity(int id, Context ctx) {
        ClickableComponent cc = cm.get(id);
        var events = InputHandler.getInstance().getEvents();
        for (var event: events) {
            int[] viewport = {0, 0, camera.width, camera.height};
            Vector3f worldSpace = new Vector3f();
            camera.getProjectionViewMatrix().unproject(
                event.position.x,
                camera.height - event.position.y,
                0.0f,
                viewport,
                worldSpace
            );
            boolean hasPoint = cc.boundingBox.containsPoint(worldSpace.x, worldSpace.y, 0.0f);
            if (hasPoint && event.type == InputHandler.MouseEventType.LEFT_CLICK) {
                System.out.printf("(%f, %f)\n", worldSpace.x, worldSpace.y);
                cc.onClick.onClick();
            }
        }
    }
}