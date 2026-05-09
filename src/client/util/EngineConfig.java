package client.util;
import client.components.enemy.EnemyComponent;
import framework.engine.*;
import client.systems.*;
import client.systems.player.PlayerRotationSystem;
import client.components.*;
import client.components.bullet.BulletComponent;
import client.components.player.*;



public class EngineConfig {
    public static void registerComponents(Engine<Context> engine){
        engine.register(TransformComponent.class);
        engine.register(MovementComponent.class);
        engine.register(RenderComponent.class);
        engine.register(AnimationComponent.class);
        engine.register(PlayerStateComponent.class);
        engine.register(TextComponent.class);

        //tags
        engine.register(EnemyComponent.class);
        engine.register(PlayerTagComponent.class);
        //bullet
        engine.register(BulletComponent.class);
    }

    public static void addSystems(Engine<Context> engine){
        // Add systems
        engine.addSystem(new PhysicsSystem());
        engine.addSystem(new AnimationSystem());
        engine.addSystem(new RenderSystem());
        engine.addSystem(new TextRenderingSystem());
        // Player Specific systems
        engine.addSystem(new PlayerRotationSystem());
        engine.addSystem(new MovementSystem());

        engine.addSystem(new EnemySystem());
    }
}
