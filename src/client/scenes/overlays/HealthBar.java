package client.scenes.overlays;

import client.components.RenderComponent;
import client.components.TransformComponent;
import client.rendering.*;
import client.systems.client.*;
import framework.engine.*;


import static org.lwjgl.glfw.GLFW.*;

import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

public class HealthBar {
    private final Engine<Context> engine;
    private List<Entity<Context>> healthBarEntities = new ArrayList<>();
    private List<Entity<Context>> segmentEntities = new ArrayList<>();
    private float currentHealth = 0;
    public HealthBar(Engine<Context> engine) {
        this.engine = engine;
    }

    public void createHealthBar(){
        // Load textures in
        Texture bgTex = TextureAtlas.get().getRegion("healthbar/healthbar_empty.png");
        Texture gemTex = TextureAtlas.get().getRegion("healthbar/healthgem.png");

        Vector2f basePos = new Vector2f(-30.0f, -10.0f);
        Vector2f scale = new Vector2f(3.0f, 3.0f);

        // bg layer
        createLayer(basePos, scale, bgTex, 0.0f);

        // segment layer
        updateHealth(currentHealth);

        // top layer
        Vector2f gemPos = new Vector2f(basePos.x + 8, basePos.y + 30);
        createLayer(gemPos, scale, gemTex, 0.2f);
    }
    
    private void createLayer(Vector2f pos, Vector2f scale, Texture tex, float z) {
            Entity<Context> entity = engine.createEntity();
            entity.addComponent(
                new TransformComponent(
                    new Vector2f(pos.x, pos.y), 
                    new Vector2f(scale.x, scale.y), 
                    Anchor.TOP_LEFT
                )
            );
            entity.addComponent(new RenderComponent(tex, z, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), "fixed"));
            healthBarEntities.add(entity);
        }

    private void createSegment(int index) {
        Texture segTex = TextureAtlas.get().getRegion("healthbar/healthSegment.png");
        
        float offset = 50.0f; // distance between segments
        Vector2f segPos = new Vector2f(100 + index * offset, 65);
        Vector2f scale = new Vector2f(3.0f, 3.0f);
        
        Entity<Context> entity = engine.createEntity();
        entity.addComponent(
            new TransformComponent(
                new Vector2f(segPos.x, segPos.y), 
                scale, 
                Anchor.TOP_LEFT
            )   
        );
        entity.addComponent(new RenderComponent(segTex, 0.1f, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), "fixed"));
        segmentEntities.add(entity);
    }
    
    public void destroyHealthBar() {
        for (Entity<Context> entity : healthBarEntities) {
            engine.destroyEntity(entity.getId());
        }
        healthBarEntities.clear();;
    }

    public void updateHealth(float newHealth) {
        if (currentHealth == newHealth) return;
        currentHealth = newHealth;

        // Remove existing segments
        for (Entity<Context> entity : segmentEntities) {
            engine.destroyEntity(entity.getId());
        }
        segmentEntities.clear();

        // Create new segments
        for (int i = 0; i < Math.round(currentHealth / 25.0f); i++) {
            createSegment(i);
        }
    }
}
