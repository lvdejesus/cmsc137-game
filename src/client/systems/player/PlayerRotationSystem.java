package client.systems.player;

import client.components.TransformComponent;
import client.components.player.PlayerTagComponent;
import client.systems.Context;
import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.Entity;
import framework.engine.EntitySystem;

public class PlayerRotationSystem extends EntitySystem<Context>{
    private ComponentMapper<TransformComponent> tm;
    
    public PlayerRotationSystem() {
        super(TransformComponent.class,PlayerTagComponent.class);
    }

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);
        this.tm = engine.getMapper(TransformComponent.class);
    }

    @Override
    public void processEntity(int id, Context ctx) {
        TransformComponent tc = tm.get(id);
        float dx = tc.position.x - ctx.cursor.x;
        float dy = tc.position.y - ctx.cursor.y;
        tc.rotation = (float) Math.toDegrees(Math.atan2(dy,dx));
    }

}
