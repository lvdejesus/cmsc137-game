package client.systems.client;

import client.components.*;
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
            } else if (ac.animation.numFrames != 12) {
                System.out.println("kil" + ac.animation.numFrames);
                engine.removeComponent(entityId, AnimationComponent.class);
                double currentTime = glfwGetTime();
                String spritePath = "enemyExplosion.png";
                engine.addComponent(entityId, new AnimationComponent(Animation.fromFile(spritePath, 12, 0.1f), (float) currentTime, false));
            } else if (ac.currentFrameIndex == 11) {
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
