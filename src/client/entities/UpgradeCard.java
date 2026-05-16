package client.entities;

import client.components.RenderComponent;
import client.components.TransformComponent;
import client.rendering.*;
import client.systems.client.*;
import framework.engine.*;
import org.joml.Vector2f;
import org.joml.Vector4f;


public class UpgradeCard {

    public static Entity create(Engine<Context> engine, int idx, Vector2f startPos) {
        String cardFrontPath = "upgrades/upgrade" + idx + ".png";
        String cardBackPath = "upgrades/card_back.png";
        Texture cardFrontTexture = TextureAtlas.get().getRegion(cardFrontPath);
        Texture cardBackTexture = TextureAtlas.get().getRegion(cardBackPath);
        Entity<Context> entity = engine.createEntity();
        
        // Will start stacked
        entity.addComponent(new TransformComponent(
            new Vector2f(startPos),
            new Vector2f(3f,3f),
            Anchor.CENTER
        ));
        RenderComponent rc = new RenderComponent(
            cardFrontTexture,
            0.5f,
            new Vector4f(1,1,1,1),
            "fixed"
        );
        entity.addComponent(rc);
        return entity;
    }    
}
