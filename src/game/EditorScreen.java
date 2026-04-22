package game;

import game.components.*;
import game.rendering.Camera;
import game.rendering.Font;
import game.rendering.Texture;
import game.rendering.TextureAtlas;
import game.systems.*;
import framework.engine.Engine;
import framework.engine.Entity;
import framework.engine.EntitySystem;
import framework.rendering.Screen;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.joml.primitives.AABBf;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class EditorScreen implements Screen<Context> {
    private Font font;

    private static int TILE_SIZE = 16;
    private static int COLS  = 3;
    private static float SCALE = 4.0f;
    private static float GAP = 24.0f;

    List<EntitySystem> systems;

    private void createTiles(Engine<Context> engine) {
        Texture bgTex =  TextureAtlas.get().getRegion("bg.png");

        int g = 0;
        for (String region : TextureAtlas.get().listRegions()) {
            if (!region.startsWith("tiles")) continue;

            Texture t = TextureAtlas.get().getRegion(region);
            int xCount = t.width / TILE_SIZE;
            int yCount = t.height / TILE_SIZE;
            for (int i = 0; i < yCount; i++) {
                for (int j = 0; j < xCount; j++) {
                    float x = 12.0f + (g % COLS) * (TILE_SIZE * SCALE + GAP);
                    float y = 44.0f + (float) (g / COLS) * (TILE_SIZE * SCALE + GAP);

                    Entity<Context> entity = engine.createEntity();
                    TransformComponent transformComponent = new TransformComponent(new Vector2f(x, y),
                        new Vector2f(SCALE, SCALE));

                    float du = (t.u2 - t.u1) / xCount;
                    float dv = (t.v2 - t.v1) / yCount;

                    float u1 = t.u1 + du * j;
                    float u2 = t.u1 + du * (j + 1);
                    float v1 = t.v1 + dv * i;
                    float v2 = t.v1 + dv * (i + 1);

                    Texture tex =  new Texture(u1, v1, u2, v2, 16, 16);
                    RenderComponent renderComponent = new RenderComponent(tex, 1);

                    Entity<Context> editorEntity = engine.queryOne(EditorComponent.class);
                    assert editorEntity != null;

                    EditorComponent ec = editorEntity.getComponent(EditorComponent.class);
                    ec.tiles.add(tex);

                    AABBf boundingBox = new AABBf();
                    boundingBox.minX = transformComponent.position.x;
                    boundingBox.minY = transformComponent.position.y;
                    boundingBox.minZ = Float.NEGATIVE_INFINITY;
                    boundingBox.maxX = transformComponent.position.x + renderComponent.texture.width * transformComponent.scale.x;
                    boundingBox.maxY = transformComponent.position.y + renderComponent.texture.width * transformComponent.scale.y;
                    boundingBox.maxZ = Float.POSITIVE_INFINITY;

                    ClickableComponent clickableComponent = new ClickableComponent(boundingBox);
                    TileComponent tileComponent = new TileComponent(g);

                    entity.addComponent(renderComponent);
                    entity.addComponent(transformComponent);
                    entity.addComponent(clickableComponent);
                    entity.addComponent(tileComponent);

                    Entity<Context> bgEntity = engine.createEntity();
                    TransformComponent tc = new TransformComponent(new Vector2f(x - 8, y - 8),
                        new Vector2f(20.0f * (SCALE / 16.0f), 20.0f * (SCALE / 16.0f)));
                    RenderComponent render = new RenderComponent(bgTex, 0);
                    bgEntity.addComponent(tc);
                    bgEntity.addComponent(render);

                    g++;
                }
            }
        }
    }

    @Override
    public void show(Engine<Context> engine, Camera camera) {
        try {
            Path fontPath = Paths.get("res/fonts/Inter-Regular.ttf");
            ByteBuffer fontBuffer = BufferUtils.createByteBuffer((int) Files.size(fontPath));
            Files.newByteChannel(fontPath).read(fontBuffer);
            fontBuffer.flip();
            font = new Font(fontBuffer, 24);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load font", e);
        }

        Entity<Context> entity = engine.createEntity();

        TransformComponent transformComponent = new TransformComponent(new Vector2f(COLS * (TILE_SIZE * SCALE + GAP), 0),
            new Vector2f(16.0f, 16.0f));

        Texture t = TextureAtlas.get().getRegion("bg.png");
        RenderComponent renderComponent = new RenderComponent(t, 0);

        AABBf boundingBox = new AABBf();
        boundingBox.minX = transformComponent.position.x;
        boundingBox.minY = transformComponent.position.y;
        boundingBox.minZ = Float.NEGATIVE_INFINITY;
        boundingBox.maxX = transformComponent.position.x + renderComponent.texture.width * transformComponent.scale.x;
        boundingBox.maxY = transformComponent.position.y + renderComponent.texture.width * transformComponent.scale.y;
        boundingBox.maxZ = Float.POSITIVE_INFINITY;

        ClickableComponent clickableComponent = new ClickableComponent(boundingBox);
        EditorComponent editor = new EditorComponent();

        entity.addComponent(renderComponent);
        entity.addComponent(transformComponent);
        entity.addComponent(clickableComponent);
        entity.addComponent(editor);

        createTiles(engine);

        Entity<Context> textEntity = engine.createEntity();
        TransformComponent textTransform = new TransformComponent(new Vector2f(12, 12), new Vector2f(1, 1));
        TextComponent textComponent = new TextComponent(font, "Tiles", new Vector4f(0.1f, 0.1f, 0.1f, 1.0f));

        textEntity.addComponent(textTransform);
        textEntity.addComponent(textComponent);

        Entity<Context> bgEntity = engine.createEntity();
        TransformComponent tc = new TransformComponent(new Vector2f(0.0f, 0.0f), new Vector2f(16.0f, 2.0f));
        RenderComponent rc = new RenderComponent(t, 0, new Vector4f(0.8f, 0.8f, 0.8f, 1.0f));
        bgEntity.addComponent(tc);
        bgEntity.addComponent(rc);

        systems = new ArrayList<>(List.of(new EntitySystem[]{
            new ClickSystem(camera),
            new RenderSystem(),
            new TileSystem(editor),
            new EditorSystem(transformComponent),
            new CleanupSystem(),
            new TextRenderingSystem(),
        }));

        for (var system : systems) {
            engine.addSystem(system);
        }

    }

    @Override
    public void update(Context context) {

    }

    @Override
    public void hide(Engine<Context> engine) {
        for (var system : systems) {
            engine.removeSystem(system);
        }

        systems.clear();

        engine.clearEntities();
    }
}
