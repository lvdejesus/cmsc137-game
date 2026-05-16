package client.util;

import client.components.enemy.EnemyComponent;
import client.network.NetworkSpawnManager;
import client.rendering.Camera;
import client.systems.client.*;
import client.systems.client.player.PlayerTiltSystem;
import editor.components.TileComponent;
import editor.components.TileGridComponent;
import framework.engine.*;
import client.systems.client.player.PlayerRotationSystem;
import client.components.*;
import client.components.bullet.BulletComponent;
import client.components.player.*;


public class EngineConfig {
    public static void registerSyncComponents(Engine<Context> engine) {
        engine.register(TransformComponent.class);
        engine.register(MovementInputComponent.class);
        engine.register(PlayerStateComponent.class);
        engine.register(HealthComponent.class);
        engine.register(PlayerKeysComponent.class);
        engine.register(PlayerUpgradeComponent.class);

        engine.register(BossDoorComponent.class);
        engine.register(BossComponent.class);
        engine.register(KeyComponent.class);
        engine.register(PlayerNetworkComponent.class);
        engine.register(NetworkIdComponent.class);
        engine.register(EnemyComponent.class);
        engine.register(BulletComponent.class);
        engine.register(CollisionComponent.class);
        engine.register(MovementComponent.class);
        engine.register(TileGridComponent.class);
        engine.register(TileComponent.class);
        engine.register(WallComponent.class);
        engine.register(NetworkDuplicateComponent.class);
        engine.register(UpgradeKindComponent.class);
    }

    public static void registerComponents(Engine<Context> engine) {
        registerSyncComponents(engine);

        engine.register(RenderComponent.class);
        engine.register(AnimationComponent.class);
        engine.register(TextComponent.class);
        engine.register(UiComponent.class);
        engine.register(UpgradeKindComponent.class);
        engine.register(FollowComponent.class);

        //tags
        engine.register(PlayerTagComponent.class);
        engine.register(DespawnTimerComponent.class);
    }

    public static void addSystems(Engine<Context> engine, Camera camera, Camera fixedCamera) {
        engine.addSystem(new AnimationSystem(), 1000);
        engine.addSystem(new RenderSystem(camera, "default"), 1000);
        engine.addSystem(new UiRenderSystem("fixed"), 1000);
        engine.addSystem(new RenderSystem(fixedCamera, "fixed"), 1000);
        engine.addSystem(new TextRenderingSystem(camera, "default"), 1000);
        engine.addSystem(new TextRenderingSystem(fixedCamera, "fixed"), 1000);
    }

    public static void addServerSystems(Engine<Context> engine, NetworkSpawnManager nsm) {
//        engine.addSystem(new PhysicsSystem());
        engine.addSystem(new EnemySystem(nsm));
        engine.addSystem(new BossSystem(nsm));
        engine.addSystem(new BulletSystem(nsm));
        engine.addSystem(new DamageSystem(nsm));
        engine.addSystem(new BulletWallTileCollisionSystem(nsm));
        engine.addSystem(new WallTileCollisionSystem());
    }
}