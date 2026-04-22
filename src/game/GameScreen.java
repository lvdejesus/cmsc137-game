package game;

import game.components.AnimationComponent;
import game.components.MovementComponent;
import game.components.RenderComponent;
import game.components.TransformComponent;
import game.rendering.Animation;
import framework.engine.*;
import game.systems.*;
import game.rendering.Camera;
import framework.rendering.Screen;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class GameScreen implements Screen<Context> {
    private ArrayList<EntitySystem> systems;

    @Override
    public void show(Engine<Context> engine, Camera camera) {
        systems = new ArrayList<>(List.of(new EntitySystem[]{
            new MovementSystem(),
            new PhysicsSystem(),
            new AnimationSystem(),
            new RenderSystem(),
        }));

        for (var system : systems) {
            engine.addSystem(system);
        }

        Entity<Context> player = engine.createEntity();

        TransformComponent transformComponent = new TransformComponent(new Vector2f(400.0f, 300.0f), new Vector2f(2.0f, 2.0f));
        MovementComponent movementComponent = new MovementComponent(200.0f);
        RenderComponent renderComponent = new RenderComponent();
        AnimationComponent animationComponent = new AnimationComponent(Animation.fromFile("tile.png", 2, 0.3f), (float) glfwGetTime());

        player.addComponent(renderComponent);
        player.addComponent(movementComponent);
        player.addComponent(transformComponent);
        player.addComponent(animationComponent);
    }

    @Override
    public void update(Context context) {
    }

    @Override
    public void hide(Engine<Context> engine) {
        for (var system : systems) {
            engine.removeSystem(system);
        }

        systems.clear();

        engine.clearEntities();
    }
}
