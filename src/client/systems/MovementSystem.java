package client.systems;

import client.components.MovementComponent;

import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.EntitySystem;

import static org.lwjgl.glfw.GLFW.*;

public class MovementSystem extends EntitySystem<Context> {
    private ComponentMapper<MovementComponent> mm;

    public MovementSystem() {
        super(MovementComponent.class);
    }

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);

        this.mm = engine.getMapper(MovementComponent.class);
    }

    @Override
    public void processEntity(int id, Context ctx) {
        MovementComponent mc = mm.get(id);
        mc.velocity.set(0.0f, 0.0f);

        if (InputHandler.getInstance().key(GLFW_KEY_W))
            mc.velocity.y -= mc.speed;
        if (InputHandler.getInstance().key(GLFW_KEY_S))
            mc.velocity.y += mc.speed;

        if (InputHandler.getInstance().key(GLFW_KEY_A))
            mc.velocity.x -= mc.speed;
        if (InputHandler.getInstance().key(GLFW_KEY_D))
            mc.velocity.x += mc.speed;
    }
}
