package client.util;

import client.components.enemy.EnemyComponent;
import client.network.NetworkSpawnManager;
import client.systems.client.*;
import framework.engine.*;
import client.systems.client.player.PlayerRotationSystem;
import client.components.*;
import client.components.bullet.BulletComponent;
import client.components.player.*;


public class EngineConfig {
    public static void registerSyncComponents(Engine<Context> engine) {
        engine.register(TransformComponent.class);
        engine.register(PlayerNetworkComponent.class);
        engine.register(NetworkIdComponent.class);
        engine.register(EnemyComponent.class);
        engine.register(BulletComponent.class);
        engine.register(CollisionComponent.class);
        engine.register(MovementComponent.class);
        engine.register(PlayerStateComponent.class);
        engine.register(HealthComponent.class);
    }

    public static void registerComponents(Engine<Context> engine) {
        registerSyncComponents(engine);

        engine.register(RenderComponent.class);
        engine.register(AnimationComponent.class);
        engine.register(TextComponent.class);

        //tags
        engine.register(PlayerTagComponent.class);
    }

    public static void addSystems(Engine<Context> engine) {
        // Add systems
        engine.addSystem(new PhysicsSystem());
        // Wall collision
        engine.addSystem(new WallCollisionSystem());

        engine.addSystem(new AnimationSystem());
        engine.addSystem(new RenderSystem());
        engine.addSystem(new TextRenderingSystem());
        // Player Specific systems
        engine.addSystem(new PlayerRotationSystem());
        engine.addSystem(new MovementSystem());
        // Enemy systems
        // engine.addSystem(new EnemySystem());
    }

    public static void addServerSystems(Engine<Context> engine, NetworkSpawnManager nsm) {
        engine.addSystem(new PhysicsSystem());
        engine.addSystem(new EnemySpawnSystem(nsm));
        engine.addSystem(new EnemySystem(nsm));
        engine.addSystem(new BulletSystem(nsm));
        engine.addSystem(new BulletWallCollisionSystem(nsm));
        engine.addSystem(new DamageSystem(nsm));
    }
}
