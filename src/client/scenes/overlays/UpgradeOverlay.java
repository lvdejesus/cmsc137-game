package client.scenes.overlays;

import client.components.RenderComponent;
import client.components.TransformComponent;
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

    public UpgradeOverlay(Engine<Context> engine){
        this.engine = engine;
    }


    public void splay(){
        // Destroy old upgrades
        destroyUpgrades();
        
        float centerx = 1920f / 2f;
        float centery = 1080f / 2f;
        float spacing = 350f;
        Vector2f startpos = new Vector2f(centerx, 1200f);
        for(int i = 0; i < 3; i++) {
            int randomIdx = (int)(Math.random()*2)+1;
            Entity<Context> card = UpgradeCard.create(engine, randomIdx, startpos);
            cards.add(card);
            
            TransformComponent tc = card.getComponent(TransformComponent.class);
            RenderComponent rc = card.getComponent(RenderComponent.class);
            tc.position.set(centerx + (i - 1) * spacing, centery);
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

    private void updateSelection(){
        float centery = 1080f/2f;
        for(int i=0; i< cards.size(); i++){
            TransformComponent tc = cards.get(i).getComponent(TransformComponent.class);
            RenderComponent rc = cards.get(i).getComponent(RenderComponent.class);
            
            //Apply visuals for selected card
            if (i == selectedIndex){
                tc.position.y = centery - 50f;
            }
            else {
                    tc.position.y = centery + 50f;
            }
        }
    }

    private void confirmSelection() {
        //TODO upgrade logic
        for (Entity<Context>entity : cards){
            TransformComponent tc = entity.getComponent(TransformComponent.class);
            RenderComponent rc = entity.getComponent(RenderComponent.class);
            tc.position.y += 800f; // Drop them off-screen bottom
            rc.tint.w = 0f;
        }
        destroyUpgrades();
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
