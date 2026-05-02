package client.systems;

import client.components.WorldComponent;
import client.rendering.Anchor;
import client.rendering.Batch;
import client.rendering.TextureAtlas;
import client.rendering.Texture;
import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.EntitySystem;

public class DungeonSystem extends EntitySystem<Context> {
    private ComponentMapper<WorldComponent> wm;
    private Batch batch;
    private Texture floorTex;
    private Texture wallTex;

    public DungeonSystem() {
        super(WorldComponent.class);
        this.batch = new Batch();
    }

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);
        this.wm = engine.getMapper(WorldComponent.class);
        
        // Use existing tiles
        try {
            this.floorTex = TextureAtlas.get().getRegion("textures/tiles/floor.png");
            this.wallTex = TextureAtlas.get().getRegion("textures/tile.png");
        } catch (Exception e) {
            System.err.println("Tile textures not found, check paths in TextureAtlas");
        }
    }

    @Override
    public void processEntity(int id, Context ctx) {
        WorldComponent world = wm.get(id);
        int[][] map = world.map;

        for (int x = 0; x < map.length; x++) {
            for (int y = 0; y < map[0].length; y++) {
                Texture tex = (map[x][y] == 0) ? floorTex : wallTex;
                if (tex != null) {
                    batch.draw(tex, 
                        x * world.tileWidth, 
                        y * world.tileHeight, 
                        -1, // Background layer
                        0, 
                        world.tileWidth + 1, world.tileHeight + 1, 
                        1, 1, 1, 1, Anchor.TOP_LEFT);
                }
            }
        }
        batch.flush();
    }
}
