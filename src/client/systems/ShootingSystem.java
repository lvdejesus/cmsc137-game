package client.systems;

import client.components.AnimationComponent;
import client.components.BulletComponent;
import client.components.BulletTagComponent;
import client.components.RenderComponent;
import client.components.TransformComponent;
import client.components.player.PlayerTagComponent;
import client.rendering.Animation;
import client.rendering.Texture;
import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.Entity;
import framework.engine.EntitySystem;
import org.joml.Vector2f;


import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_Z;


public class ShootingSystem extends EntitySystem<Context> {
    private static final float BULLET_SPEED  = 600f;
    private static final float BULLET_SCALE  = 0.05f;
    private static final float FIRE_COOLDOWN = 0.08f;   // seconds between shots


    private static final String BULLET_TEXTURE = "textures/bullets.png";
    private static final int    BULLET_FRAMES  = 1;

    private ComponentMapper<TransformComponent> tm;
    private ComponentMapper<AnimationComponent> am;
    private ComponentMapper<PlayerTagComponent> pm;

    private float cooldownRemaining = 0;


    private Texture bulletTex;


    private int playerId = -1;

    public ShootingSystem() {
        super(TransformComponent.class, AnimationComponent.class, PlayerTagComponent.class);
    }

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);
        this.tm = engine.getMapper(TransformComponent.class);
        this.am = engine.getMapper(AnimationComponent.class);
        this.pm = engine.getMapper(PlayerTagComponent.class);
    }


    @Override
    public void update(Context ctx) {
        cooldownRemaining -= ctx.deltaTime;


        if (playerId == -1) {
            super.update(ctx); 
            return;
        }


        boolean fired = false;
        if (InputHandler.getInstance().mouseButtonDown(GLFW_MOUSE_BUTTON_LEFT) && cooldownRemaining <= 0f) {
            fired = true;
        }

        if (fired) {
            cooldownRemaining = FIRE_COOLDOWN;
            TransformComponent tc = tm.get(playerId);
            AnimationComponent ac = am.get(playerId);

            Vector2f dir = switch (ac.currentAnim) {
                case 1 -> new Vector2f(-1,  0);   // Left
                case 2 -> new Vector2f( 1,  0);   // Right
                case 3 -> new Vector2f( 0, -1);   // Up
                default -> new Vector2f( 0,  1);  // Down (0)
            };

            spawnBullet(new Vector2f(tc.position), dir);
        }
    }

    @Override
    public void processEntity(int id, Context ctx) {
       
        playerId = id;
    }

    private void spawnBullet(Vector2f origin, Vector2f dir) {

        if (bulletTex == null) {
            Animation anim = Animation.fromFile(BULLET_TEXTURE, BULLET_FRAMES, 0f);
            bulletTex = anim.frames[0];
        }

        Entity<Context> bullet = engine.createEntity();

        bullet.addComponent(new TransformComponent(
            origin,
            new Vector2f(BULLET_SCALE, BULLET_SCALE)
        ));

        bullet.addComponent(new RenderComponent(bulletTex, 0));

        bullet.addComponent(new BulletComponent(
            new Vector2f(dir).mul(BULLET_SPEED)
        ));

        bullet.addComponent(new BulletTagComponent());
    }
}
