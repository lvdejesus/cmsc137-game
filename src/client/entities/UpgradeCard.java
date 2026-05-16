    package client.entities;

    import client.components.*;
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
                cardBackTexture,
                0.5f,
                new Vector4f(1,1,1,1),
                "fixed"
            );
            entity.addComponent(new UpgradeKindComponent(idx));
            rc.visualScaleX = 1.0f;
            entity.addComponent(rc);
        
            UiComponent ui = new UiComponent("BACK","FRONT");
            ui.lerpSpeed = 3f;
            ui.targetPosistion.set(startPos);
            
            ui.onStateChanged = (index, state) -> {
                rc.texture = state.equals("FRONT") ? cardFrontTexture : cardBackTexture;
            };
            ui.shaderFrag= "res/shaders/highlight.frag";
            entity.addComponent(ui);

            return entity;
        }    
            
    }
