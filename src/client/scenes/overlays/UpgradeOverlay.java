package client.scenes.overlays;

import client.components.DespawnTimerComponent;
import client.components.RenderComponent;
import client.components.TransformComponent;
import client.components.UiComponent;
import client.entities.UpgradeCard;
import client.rendering.*;
import client.systems.client.*;
import framework.engine.*;


import static org.lwjgl.glfw.GLFW.*;

import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class UpgradeOverlay {
    private final Engine<Context> engine;
    private List<Entity<Context>> cards = new ArrayList<>();
    private int selectedIndex = 0;

    public UpgradeOverlay(Engine<Context> engine) {
        this.engine = engine;
    }


    public void splay() {
        // Destroy old upgrades
        destroyUpgrades();

        float centerx = Window.getWindow().getWidth() / 2f;
        float centery = Window.getWindow().getHeight() / 2f;
        float spacing = 350f;

        Vector2f startpos = new Vector2f(centerx - 500f, centery);
        
        for (int i = 0; i < 3; i++) {
            int randomIdx = (int) (Math.random() * 2) + 1;
            Entity<Context> card = UpgradeCard.create(engine, randomIdx, startpos);
            cards.add(card);

            // Gets the ui component from card
            UiComponent ui = card.getComponent(UiComponent.class);
            // Animation
            ui.lerpSpeed = 5f;
            ui.targetPosistion.set(centerx + (i-1) * spacing,centery);
            // To flip
            ui.targetState = 1;            
        }
        selectedIndex = 1;
    }

    public void handleInput(InputHandler input) {

        int oldIndex = selectedIndex;
        if (input.keyDown(GLFW_KEY_LEFT)) selectedIndex = Math.max(0, selectedIndex - 1);
        if (input.keyDown(GLFW_KEY_RIGHT)) selectedIndex = Math.min(cards.size() - 1, selectedIndex + 1);

        if (oldIndex != selectedIndex) {
            updateSelection();
        }
        if (input.keyDown(GLFW_KEY_ENTER)) {
            confirmSelection();
        }
    }

    private void updateSelection() {
        
        float centery = Window.getWindow().getHeight() / 2f;
        
        for (int i = 0; i < cards.size(); i++) {
            UiComponent ui = cards.get(i).getComponent(UiComponent.class);
            RenderComponent rc = cards.get(i).getComponent(RenderComponent.class);
            //Apply visuals for selected card
            ui.lerpSpeed = 5f;
            if (i == selectedIndex) {
                ui.targetPosistion.y = centery - 50f;
                rc.shaderUniforms.put("u_Highlight", 1f);
            } else {
                ui.targetPosistion.y = centery;
                rc.shaderUniforms.put("u_Highlight", 0f);
            }
        }
    }

    private void confirmSelection() {
        float despawnTime = 2f;
        float centery = Window.getWindow().getHeight() / 2f;
        //TODO upgrade logic
        for (Entity<Context> entity : cards) {
            UiComponent ui = entity.getComponent(UiComponent.class);
            ui.lerpSpeed = 1f;
            ui.targetTint.w = 0f;
            ui.targetPosistion.y =  centery + 2000;
            entity.addComponent(new DespawnTimerComponent(despawnTime));
        }
        cards.clear();
    }

    public void destroyUpgrades() {
        if (cards == null || cards.isEmpty()) return;

        for (Entity<Context> entity : cards) {
            if (entity != null) {
                engine.destroyEntity(entity.getId());
            }
        }
        cards.clear();
    }

}
