package client.systems.client;

import client.components.ClickEvent;
import client.components.ClickableComponent;
import client.rendering.Camera;
import client.rendering.CameraManager;
import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.IteratingEntitySystem;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class ClickSystem extends IteratingEntitySystem<Context> {
    private ComponentMapper<ClickableComponent> cm;
    private final CameraManager cameraManager;

    private Vector3f worldMouse;
    private float maxZ;
    private int winnerId;

    public ClickSystem(CameraManager cameraManager) {
        super(ClickableComponent.class);

        this.cameraManager = cameraManager;
    }

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);

        cm = engine.getMapper(ClickableComponent.class);
    }

    @Override
    public void processEntity(int id, Context ctx) {
        ClickableComponent cc = cm.get(id);

        if (cc.boundingBox.containsPoint(worldMouse.x, worldMouse.y, 0.0f)) {
            if (cc.z > maxZ) {
                maxZ = cc.z;
                winnerId = id;
            }
        }
    }

    @Override
    public void update(Context ctx) {
        var events = InputHandler.getInstance().getEvents();

        for (var event : events) {
            if (event.consumed) continue;

            Camera camera = cameraManager.getCameraByScreenPoint(event.position.x, event.position.y);
            if (camera == null) continue;

            worldMouse = unproject(event.position, camera);
            maxZ = Float.NEGATIVE_INFINITY;
            winnerId = -1;

            super.update(ctx);

            if (winnerId != -1 && event.type == InputHandler.MouseEventType.LEFT_CLICK) {
                engine.addComponent(winnerId, new ClickEvent(worldMouse.x, worldMouse.y));
                event.consume();
            }
        }
    }

    private Vector3f unproject(Vector2f position, Camera camera) {
        // Adjust position relative to camera viewport
        float adjustedX = position.x - camera.viewportX;
        float adjustedY = camera.viewportHeight - (position.y - camera.viewportY);
        
        int[] viewport = {0, 0, (int)camera.viewportWidth, (int)camera.viewportHeight};
        Vector3f worldSpace = new Vector3f();

        return camera.getProjectionViewMatrix().unproject(
            adjustedX,
            adjustedY,
            0.0f,
            viewport,
            worldSpace
        );
    }
}