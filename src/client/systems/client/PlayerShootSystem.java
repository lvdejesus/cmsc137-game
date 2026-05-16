package client.systems.client;

import client.components.MovementComponent;
import client.components.PlayerUpgradeComponent;
import client.components.TransformComponent;
import client.components.player.PlayerTagComponent;
import client.entities.Player;
import client.network.messages.Message;
import client.network.messages.client.C_PlayerState;
import client.network.messages.client.C_Shoot;
import client.rendering.Camera;
import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.IteratingEntitySystem;
import org.joml.Vector2f;

import java.util.concurrent.ConcurrentLinkedQueue;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class PlayerShootSystem extends IteratingEntitySystem<Context> {
    private final ConcurrentLinkedQueue<Message> outQueue;
    private final Camera camera;

    private ComponentMapper<MovementComponent> mm;
    private ComponentMapper<PlayerTagComponent> ptm;

    public PlayerShootSystem(ConcurrentLinkedQueue<Message> outQueue, Camera camera) {
        super(PlayerTagComponent.class);

        this.outQueue = outQueue;
        this.camera = camera;
    }

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);

        mm = engine.getMapper(MovementComponent.class);
        ptm = engine.getMapper(PlayerTagComponent.class);
    }

    @Override
    protected void processEntity(int entityId, Context ctx) {
        MovementComponent mc = mm.get(entityId);
        PlayerTagComponent ptc = ptm.get(entityId);
        PlayerUpgradeComponent puc = engine.getMapper(PlayerUpgradeComponent.class).get(entityId);

        if (InputHandler.getInstance().leftMouseHeld) {
            if (ptc.shootTimer < glfwGetTime()) {
                Vector2f d = camera.toWorldPosition(InputHandler.getInstance().cursorPosition);

                // Get player's current velocity for velocity inheritance
                float pvx = mc.velocity.x;
                float pvy = mc.velocity.y;

                if (puc.splatter > 1) {
                    TransformComponent tc = engine.getMapper(TransformComponent.class).get(entityId);
                    Vector2f dirVec = new Vector2f(d).sub(tc.position);
                    float angle = (float) Math.atan2(dirVec.y, dirVec.x);
                    float increment = (float) (Math.PI * 2.0f / puc.splatter);
                    for (int i = 0; i < puc.splatter; i++) {
                        float finalAngle = angle + increment * i;
                        float nx = (float) (tc.position.x + Math.cos(finalAngle));
                        float ny = (float) (tc.position.y + Math.sin(finalAngle));
                        outQueue.offer(new C_Shoot(nx, ny, pvx, pvy));
                    }
                } else {
                    outQueue.offer(new C_Shoot(d.x, d.y, pvx, pvy));
                }

                ptc.shootTimer = glfwGetTime() + 1.0f / puc.getEffectiveFireRate();
            }
        }
    }
}
