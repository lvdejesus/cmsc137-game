package client.systems.client.player;

import client.components.TransformComponent;
import client.components.player.PlayerTagComponent;
import client.systems.client.Context;
import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.IteratingEntitySystem;

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
        float dx = ctx.cursor.x - tc.position.x;
        float dy = ctx.cursor.y - tc.position.y;
        float angle = (float) Math.toDegrees(Math.atan2(dy, dx)) + 90.0f;
        tc.rotation = angle;
    }

}
