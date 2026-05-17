package client.systems.client;

import client.components.ExperienceComponent;
import client.components.HealthComponent;
import framework.engine.IteratingEntitySystem;

public class ExperienceSystem extends IteratingEntitySystem<Context> {
    public ExperienceSystem() {
        super(ExperienceComponent.class, HealthComponent.class);
    }

    @Override
    protected void processEntity(int entityId, Context ctx) {
        HealthComponent hc = engine.getMapper(HealthComponent.class).get(entityId);
        ExperienceComponent xpc = engine.getMapper(ExperienceComponent.class).get(entityId);
        if (xpc.lastLevelCount != xpc.getLevels()) {
            hc.currentHealth = hc.maxHealth;
            xpc.lastLevelCount = xpc.getLevels();
        }
    }
}
