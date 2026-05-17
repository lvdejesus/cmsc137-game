package client.systems.client;

import client.components.*;
import client.components.player.MovementInputComponent;
import client.components.player.PlayerStateComponent;
import client.rendering.Animation;
import framework.engine.IteratingEntitySystem;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class DeathSystem extends IteratingEntitySystem<Context> {
    public DeathSystem() {
        super(PlayerStateComponent.class);
    }

    @Override
    protected void processEntity(int entityId, Context ctx) {
        var hc = engine.getMapper(HealthComponent.class).get(entityId);
        var ac = engine.getMapper(AnimationComponent.class).get(entityId);
        if (!hc.isAlive()) {
            if (ac == null) {
            } else if (ac.animation.numFrames != 8) {
                engine.removeComponent(entityId, AnimationComponent.class);
                var mc = engine.getMapper(MovementComponent.class).get(entityId);
                mc.velocity.set(0.0f, 0.0f);
                var mic = engine.getMapper(MovementInputComponent.class).get(entityId);
                mic.x = 0.0f;
                mic.y = 0.0f;

                double currentTime = glfwGetTime();
                String spritePath = "players/playerDeath.png";
                engine.addComponent(entityId, new AnimationComponent(Animation.fromFile(spritePath, 8, 0.1f), (float) currentTime, false));
            } else if (ac.currentFrameIndex == 7) {
                engine.removeComponent(entityId, AnimationComponent.class);
                engine.removeComponent(entityId, RenderComponent.class);

                boolean isFollowing = engine.getMapper(FollowComponent.class).get(entityId) != null;

                if (isFollowing) {
                    engine.removeComponent(entityId, FollowComponent.class);
                    Iterable<Integer> players = engine.getFamily(PlayerStateComponent.class)::iterator;
                    for (int i : players) {
                        if (engine.getMapper(HealthComponent.class).get(i).isAlive()) {
                            engine.addComponent(i, new FollowComponent());
                            break;
                        }
                    }
                }
            }
        }
    }
}
