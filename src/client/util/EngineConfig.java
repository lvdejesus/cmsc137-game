package client.util;
import client.components.enemy.EnemyComponent;
import client.systems.client.*;
import framework.engine.*;
import client.systems.client.player.PlayerRotationSystem;
import client.components.*;
import client.components.bullet.BulletComponent;
import client.components.player.*;
import client.rendering.Camera;


public class EngineConfig {
    public static void registerSyncComponents(Engine<Context> engine) {
        engine.register(TransformComponent.class);
        engine.register(NetworkIdComponent.class);
    }

    public static void registerComponents(Engine<Context> engine){
        registerSyncComponents(engine);

        engine.register(MovementComponent.class);
        engine.register(RenderComponent.class);
        engine.register(AnimationComponent.class);
        engine.register(PlayerStateComponent.class);
        engine.register(TextComponent.class);
        engine.register(HealthComponent.class);
        engine.register(CollisionComponent.class);

        //tags
        engine.register(EnemyComponent.class);
        engine.register(PlayerTagComponent.class);
        //bullet
        engine.register(BulletComponent.class);
    }

    public static void addSystems(Engine<Context> engine, Camera camera){
        // Add systems
        engine.addSystem(new PhysicsSystem());
        // Wall collision
        engine.addSystem(new WallCollisionSystem(camera));

        engine.addSystem(new AnimationSystem());
        engine.addSystem(new RenderSystem());
        engine.addSystem(new TextRenderingSystem());
        // Player Specific systems
        engine.addSystem(new PlayerRotationSystem());
        engine.addSystem(new MovementSystem());
        // Enemy systems
        engine.addSystem(new EnemySystem());
        engine.addSystem(new BulletSystem());
        engine.addSystem(new DamageSystem());
    }
}
