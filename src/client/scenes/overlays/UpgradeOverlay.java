package client.scenes.overlays;

import client.components.*;
import client.entities.Player;
import client.entities.UpgradeCard;
import client.rendering.*;
import client.systems.client.*;
import framework.engine.*;


import static org.lwjgl.glfw.GLFW.*;

import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

public class UpgradeOverlay {
    private final Engine<Context> engine;
    private List<Entity<Context>> cards = new ArrayList<>();
    private int selectedIndex = 0;
    private final Player player;
    private boolean visible = false;

    private Random random = new Random();

    public UpgradeOverlay(Engine<Context> engine, Player player) {
        this.engine = engine;
        this.player = player;
    }


    public void splay() {
        // Destroy old upgrades
        destroyUpgrades();

        float centerx = Window.getWindow().getWidth() / 2f;
        float centery = Window.getWindow().getHeight() / 2f;
        float spacing = 350f;

        Vector2f startpos = new Vector2f(centerx - 500f, centery);
        
        for (int i = 0; i < 3; i++) {
            int randomIdx = random.nextInt(1, 4);
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
        visible = true;
    }

    public void handleInput(InputHandler input) {
        if (!visible) return;

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
                rc.shaderUniforms.put("u_Highlight", 0.3f);
            } else {
                ui.targetPosistion.y = centery;
                rc.shaderUniforms.put("u_Highlight", 0f);
            }
        }
    }

    private void confirmSelection() {
        float despawnTime = 2f;
        float centery = Window.getWindow().getHeight() / 2f;

        int upgradeKind = cards.get(selectedIndex).getComponent(UpgradeKindComponent.class).index;

        System.out.println(upgradeKind);
        player.getEntity().getComponent(PlayerUpgradeComponent.class).queueApply(upgradeKind);
        //TODO upgrade logic
        for (Entity<Context> entity : cards) {
            UiComponent ui = entity.getComponent(UiComponent.class);
            ui.lerpSpeed = 1f;
            ui.targetTint.w = 0f;
            ui.targetPosistion.y =  centery + 2000;
            entity.addComponent(new DespawnTimerComponent(despawnTime));
        }
        cards.clear();
        visible = true;
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
