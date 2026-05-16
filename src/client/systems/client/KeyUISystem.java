package client.systems.client;

import client.components.FollowComponent;
import client.components.PlayerKeysComponent;
import client.components.RenderComponent;
import client.components.TransformComponent;
import client.rendering.Anchor;
import client.rendering.Texture;
import client.rendering.TextureAtlas;
import framework.engine.Entity;
import framework.engine.IteratingEntitySystem;
import framework.engine.Window;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.ArrayList;

public class KeyUISystem extends IteratingEntitySystem<Context> {
    private final ArrayList<Integer> keyEntities = new ArrayList<>();

    public KeyUISystem() {
        super(FollowComponent.class);
    }

    @Override
    public void processEntity(int entityId, Context ctx) {
        PlayerKeysComponent pkc = engine.getMapper(PlayerKeysComponent.class).get(entityId);
        if (pkc.lastKeyCount != pkc.keyCount) {
            updateGraphics(pkc.keyCount);
            pkc.lastKeyCount = pkc.keyCount;
        }
    }

    private void updateGraphics(int keyCount) {
        for (int keyEntity : keyEntities) {
            engine.destroyEntity(keyEntity);
        }
        keyEntities.clear();

        for (int i = 0; i < 3; i++) {
            Entity<Context> entity = engine.createEntity();

            TransformComponent tc = new TransformComponent(new Vector2f(- 96.0f + i * 40.0f, 16.0f), new Vector2f(2.0f, 2.0f), Anchor.TOP_RIGHT);
            tc.globalAnchor = true;
            entity.addComponent(tc);
            Texture texture;
            if (i < keyCount) {
                texture = TextureAtlas.get().getRegion("key.png");
            } else {
                texture = TextureAtlas.get().getRegion("key-gray.png");
            }
            entity.addComponent(new RenderComponent(texture, 0.1f, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), "fixed"));
            keyEntities.add(entity.getId());
        }
    }
}
