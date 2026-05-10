package client.scenes;

import client.entities.Bullet;
import client.entities.Player;
import client.systems.Context;
import client.systems.InputHandler;
import client.components.TransformComponent;
import framework.engine.Engine;

public class LevelScene implements Scene {
    private Engine<Context> engine;
    private Player player;
    private TransformComponent playerTransform;

    @Override
    public void init(Engine<Context> engine) {
        this.engine = engine;
        
        // Ensure systems are enabled
        setGameSystemsEnabled(true);

        this.player = new Player(engine);
        this.playerTransform = player.getEntity().getComponent(TransformComponent.class);
    }

    private void setGameSystemsEnabled(boolean enabled) {
        enableSystem(client.systems.EnemySystem.class, enabled);
        enableSystem(client.systems.BulletSystem.class, enabled);
        enableSystem(client.systems.MovementSystem.class, enabled);
        enableSystem(client.systems.player.PlayerRotationSystem.class, enabled);
        enableSystem(client.systems.DamageSystem.class, enabled);
    }

    private <T extends framework.engine.EntitySystem<Context>> void enableSystem(Class<T> type, boolean enabled) {
        T system = engine.getSystem(type);
        if (system != null) {
            system.setEnabled(enabled);
        }
    }

    @Override
    public void update() {
        // Logic moved from Main loop
        for (InputHandler.MouseEvent event : InputHandler.getInstance().getEvents()) {
            if (event.type == InputHandler.MouseEventType.LEFT_CLICK && !event.consumed) {
                event.consume();

                if (playerTransform != null) {
                    float playerX = playerTransform.position.x;
                    float playerY = playerTransform.position.y;

                    float mouseX = event.position.x;
                    float mouseY = event.position.y;

                    float dx = mouseX - playerX;
                    float dy = mouseY - playerY;
                    float angle = (float) Math.toDegrees(Math.atan2(dy, dx));

                    new Bullet(engine, playerX, playerY, angle);
                }
            }
        }
    }

    @Override
    public void clean() {
        // Nothing specific to clean up yet
    }
}
