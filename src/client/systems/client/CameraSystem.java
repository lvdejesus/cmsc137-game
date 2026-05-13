package client.systems.client;

import client.components.TransformComponent;
import client.components.player.PlayerTagComponent;
import client.rendering.Camera;
import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.IteratingEntitySystem;

public class CameraSystem extends IteratingEntitySystem<Context> {
    private final Camera camera;

    private ComponentMapper<TransformComponent> tm;

    public CameraSystem(Camera camera) {
        super(PlayerTagComponent.class, TransformComponent.class);

        this.camera = camera;
    }

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);

        tm = engine.getMapper(TransformComponent.class);
    }

    @Override
    protected void processEntity(int entityId, Context ctx) {
        TransformComponent tc = tm.get(entityId);
        this.camera.position.set(tc.position.x - this.camera.getWidth() / 2, tc.position.y - this.camera.getHeight() / 2);
    }
}
