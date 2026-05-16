package client.systems.client;

import client.components.HealthComponent;
import client.components.player.MovementInputComponent;
import client.components.player.PlayerTagComponent;
import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.IteratingEntitySystem;

import static org.lwjgl.glfw.GLFW.*;

public class MovementInputSystem extends IteratingEntitySystem<Context> {
    private ComponentMapper<MovementInputComponent> mim;

    public MovementInputSystem() {
        super(MovementInputComponent.class, PlayerTagComponent.class);
    }

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);
        this.mim = engine.getMapper(MovementInputComponent.class);
    }

    @Override
    public void processEntity(int id, Context ctx) {
        if (!engine.getMapper(HealthComponent.class).get(id).isAlive()) return;

        float x = 0;
        float y = 0;

        if (InputHandler.getInstance().key(GLFW_KEY_W)) y -= 1;
        if (InputHandler.getInstance().key(GLFW_KEY_S)) y += 1;
        if (InputHandler.getInstance().key(GLFW_KEY_A)) x -= 1;
        if (InputHandler.getInstance().key(GLFW_KEY_D)) x += 1;

        MovementInputComponent mic = mim.get(id);
        mic.x = x;
        mic.y = y;
    }
}
