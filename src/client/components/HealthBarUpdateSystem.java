package client.components;

import client.scenes.overlays.HealthBar;
import client.systems.client.Context;
import framework.engine.EntitySystem;

public class HealthBarUpdateSystem extends EntitySystem<Context> {
    private final HealthBar healthBar;

    public HealthBarUpdateSystem(HealthBar healthBar) {
        this.healthBar = healthBar;
    }

    @Override
    public void update(Context ctx) {
        Iterable<Integer> entityIds = engine.getFamily(FollowComponent.class, HealthComponent.class)::iterator;
        for (int i : entityIds) {
            healthBar.updateHealth(engine.getMapper(HealthComponent.class).get(i).currentHealth);
            break;
        }

    }
}
