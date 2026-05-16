package client.systems.client;

import client.components.*;
import client.rendering.Anchor;
import client.rendering.Texture;
import client.rendering.TextureAtlas;
import framework.engine.Entity;
import framework.engine.IteratingEntitySystem;
import framework.engine.Window;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.ArrayList;

public class UpgradeUISystem extends IteratingEntitySystem<Context> {
    private final ArrayList<Integer> cardEntities = new ArrayList<>();

    public UpgradeUISystem() {
        super(ExperienceComponent.class);
    }

    @Override
    public void processEntity(int entityId, Context ctx) {
        ExperienceComponent xpc = engine.getMapper(ExperienceComponent.class).get(entityId);
        if (xpc.lastUpgradeCount != xpc.getRemainingUpgrades()) {
            updateGraphics(xpc.getRemainingUpgrades());
            xpc.lastUpgradeCount = xpc.getRemainingUpgrades();
        }
    }

    private void updateGraphics(int upgradeCount) {
        for (int cardEntity : cardEntities) {
            engine.destroyEntity(cardEntity);
        }
        cardEntities.clear();

        for (int i = 0; i < upgradeCount; i++) {
            for (int j = 0; j < 3; j++) {
                Entity<Context> entity = engine.createEntity();
                var tc = new TransformComponent(new Vector2f(-16.0f - 64.0f * i - 12.0f * j, 80.0f), new Vector2f(0.5f, 0.5f), Anchor.TOP_RIGHT);
                tc.globalAnchor = true;
                entity.addComponent(tc);
                Texture texture = TextureAtlas.get().getRegion("upgrades/card_back.png");
                entity.addComponent(new RenderComponent(texture, 0.4f - 0.1f * j, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), "fixed"));
                cardEntities.add(entity.getId());
            }
        }
    }
}
