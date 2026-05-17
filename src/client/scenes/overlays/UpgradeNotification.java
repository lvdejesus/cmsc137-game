package client.scenes.overlays;

import org.joml.Vector2f;
import org.joml.Vector4f;

import client.components.DespawnTimerComponent;
import client.components.RenderComponent;
import client.components.TimerComponent;
import client.components.TransformComponent;
import client.components.UiComponent;
import client.rendering.*;
import client.systems.client.*;
import framework.engine.*;


public class UpgradeNotification {
    private final Engine<Context> engine;
    private Entity<Context> popup;
    private boolean isNotificationActive = false;

    
    public UpgradeNotification(Engine<Context> engine) {
        this.engine = engine;
    }

    public boolean isActive(){return isNotificationActive;}

    public void showUpgradeNotification(){
        Entity<Context> popup = engine.createEntity();
        
        if(popup == null) return;
        
        float centerx = Window.getWindow().getWidth() / 2f;
        float centery = Window.getWindow().getHeight() / 2f;
        
        popup.addComponent(new TransformComponent(new Vector2f(centerx, centery), new Vector2f(1, 1), Anchor.CENTER));
        popup.addComponent(new RenderComponent(TextureAtlas.get().getRegion("upgrade_avail.png"), 0.5f, new Vector4f(1, 1, 1, 0f), "fixed"));
        
        UiComponent ui = new UiComponent("default");
        ui.lerpSpeed = 4f; 
        ui.targetTint.set(1f, 1f, 1f, 1f); // Smooth fade-in
        ui.targetPosistion.set(centerx, centery);
        popup.addComponent(ui);

        popup.addComponent(new TimerComponent(3f,()->{
            // Fadeout and destroy
            ui.lerpSpeed = 3f;
            ui.targetTint.w = 0f;

            this.isNotificationActive = false;
            float fadeout = 2f;
            popup.addComponent(new DespawnTimerComponent(fadeout));
        }));
    }

    public void toggle(){
        if(isNotificationActive) return;
        isNotificationActive = true;
        showUpgradeNotification();
    }

    public void reset(){
        isNotificationActive = false;
    }
}
