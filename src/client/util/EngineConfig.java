package client.util;

import framework.engine.*;
import client.systems.*;
import client.systems.player.PlayerRotationSystem;
import client.components.*;
import client.components.player.*;



public class EngineConfig {
    public static void registerComponents(Engine<Context> engine){
        engine.register(TransformComponent.class);
        engine.register(MovementComponent.class);
        engine.register(RenderComponent.class);
        engine.register(AnimationComponent.class);
        engine.register(PlayerStateComponent.class);
        engine.register(TextComponent.class);
        engine.register(BulletComponent.class);

        //tags
        engine.register(PlayerTagComponent.class);
        engine.register(BulletTagComponent.class);
        engine.register(WorldComponent.class);
    }

    public static void addSystems(Engine<Context> engine){
        // Add systems
        engine.addSystem(new PhysicsSystem());
        engine.addSystem(new AnimationSystem());
        engine.addSystem(new ShootingSystem());
        engine.addSystem(new BulletSystem());
        engine.addSystem(new DungeonSystem());
        engine.addSystem(new RenderSystem());
        engine.addSystem(new TextRenderingSystem());
        // PlayerRotationSystem disabled — using 4-directional sprites, no cursor rotation
        // engine.addSystem(new PlayerRotationSystem());
        engine.addSystem(new MovementSystem());
    }
}
