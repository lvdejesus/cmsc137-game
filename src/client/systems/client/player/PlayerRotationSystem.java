package client.systems.client.player;

import client.components.TransformComponent;
import client.components.player.PlayerTagComponent;
import client.systems.client.Context;
import client.systems.client.InputHandler;
import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.IteratingEntitySystem;
import org.joml.Vector2f;

public class PlayerRotationSystem extends IteratingEntitySystem<Context> {
    private ComponentMapper<TransformComponent> tm;

    public PlayerRotationSystem() {
        super(TransformComponent.class, PlayerTagComponent.class);
    }

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);
        this.tm = engine.getMapper(TransformComponent.class);
    }

    @Override
    public void processEntity(int id, Context ctx) {
        TransformComponent tc = tm.get(id);
        Vector2f cursorPosition = InputHandler.getInstance().cursorPosition;
        float dx = cursorPosition.x - tc.position.x;
        float dy = cursorPosition.y - tc.position.y;
        float angle = (float) Math.toDegrees(Math.atan2(dy, dx)) + 90.0f;
        tc.rotation = angle;
    }

}
