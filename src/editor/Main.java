package editor;

import client.systems.client.*;
import common.TileDefinition;
import editor.components.*;
import editor.systems.CleanupSystem;
import editor.systems.EditorSystem;
import editor.systems.TileSystem;
import editor.systems.ButtonSystem;
import editor.systems.PanSystem;
import common.TileLoader;
import framework.rendering.ShaderProgram;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.joml.primitives.AABBf;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import client.components.*;
import framework.engine.*;
import client.rendering.*;
import client.rendering.Font;
import client.components.TextComponent;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static common.TileLoader.loadTileTextures;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {
    private long window;
    private ShaderProgram shaderProgram;
    private int shaderId;
    private CameraManager cameraManager;
    private Camera uiCamera;
    private Camera editorCamera;
    private final int WIDTH = 800, HEIGHT = 600;
    private final int SIDEBAR_WIDTH = 200;
    private Engine<Context> engine;

    private Font font;

    private TileGridComponent editor;

    private static final int TILE_SIZE = 16;
    private static final int COLS = 3;
    private static final float SCALE = 4.0f;
    private static final float GAP = 4.0f;

    public void run() {
        init();
        loop();

        glDeleteProgram(shaderId);
        glfwTerminate();
    }

    private void createTiles(List<TileGridComponent.TileTexture> tileTextures) {
        float tileStartY = 200;
        int g = 0;

        for (TileGridComponent.TileTexture tile : tileTextures) {
            var textures = tile.textures;

            float x = (g % COLS) * (TILE_SIZE * GAP + SCALE);
            float y = tileStartY + (float) (g / COLS) * (TILE_SIZE * GAP + SCALE);

            Entity<Context> entity = engine.createEntity();
            TransformComponent transformComponent = new TransformComponent(new Vector2f(x, y),
                new Vector2f(SCALE, SCALE), Anchor.TOP_LEFT);

            RenderComponent renderComponent = new RenderComponent(textures.get(0), 0);
            renderComponent.layer = "ui";

            AABBf boundingBox = new AABBf();
            boundingBox.minX = transformComponent.position.x;
            boundingBox.minY = transformComponent.position.y;
            boundingBox.minZ = Float.NEGATIVE_INFINITY;
            boundingBox.maxX = transformComponent.position.x + renderComponent.texture.width * transformComponent.scale.x;
            boundingBox.maxY = transformComponent.position.y + renderComponent.texture.width * transformComponent.scale.y;
            boundingBox.maxZ = Float.POSITIVE_INFINITY;

            ClickableComponent clickableComponent = new ClickableComponent(boundingBox);
            SidebarTileComponent tileComponent = new SidebarTileComponent(g, false);

            entity.addComponent(renderComponent);
            entity.addComponent(transformComponent);
            entity.addComponent(clickableComponent);
            entity.addComponent(tileComponent);

            g++;
        }
    }

    private void init() {
        if (!glfwInit()) {
            throw new IllegalStateException("GLFW failed!");
        }

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        window = glfwCreateWindow(WIDTH, HEIGHT, "Game", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Window failed!");
        }

        InputHandler.getInstance().register(window);

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1); // VSync
        GL.createCapabilities();

        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        shaderProgram = ShaderProgram.getShaderProgram("res/shaders/default.vert", "res/shaders/default.frag");
        shaderId = shaderProgram.getId();
        TextureAtlas.get();

        try {
            Path fontPath = Paths.get("res/fonts/Inter-Regular.ttf");
            ByteBuffer fontBuffer = BufferUtils.createByteBuffer((int) Files.size(fontPath));
            Files.newByteChannel(fontPath).read(fontBuffer);
            fontBuffer.flip();
            font = new Font(fontBuffer, 24);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load font", e);
        }

        // Create cameras - Editor on left (0-600), Sidebar on right (600-800)
        // Editor camera: 600x600 viewport, 600x600 world (square, 1:1 pixel)
        // Sidebar camera: 200px screen space, 1:1 with world

        cameraManager = new CameraManager();

        editorCamera = new Camera("editor");
        editorCamera.setViewport(0, 0, WIDTH - SIDEBAR_WIDTH, HEIGHT);
        editorCamera.setWorldSize(WIDTH - SIDEBAR_WIDTH, HEIGHT);

        uiCamera = new Camera("ui");
        uiCamera.setViewport(WIDTH - SIDEBAR_WIDTH, 0, SIDEBAR_WIDTH, HEIGHT);
        uiCamera.setWorldSize(SIDEBAR_WIDTH, HEIGHT);

        cameraManager.addCamera("editor", editorCamera);
        cameraManager.addCamera("ui", uiCamera);

        glfwSetFramebufferSizeCallback(window, (window, width, height) -> {
            float newEditorWidth = Math.max(0, width - SIDEBAR_WIDTH);
            editorCamera.setViewport(0, 0, newEditorWidth, height);
            editorCamera.setWorldSize(newEditorWidth, height);
            uiCamera.setViewport(width - SIDEBAR_WIDTH, 0, SIDEBAR_WIDTH, height);
            uiCamera.setWorldSize(SIDEBAR_WIDTH, height);
            glViewport(0, 0, width, height);
        });

        engine = new Engine<>();

        engine.register(TransformComponent.class);
        engine.register(RenderComponent.class);
        engine.register(ClickableComponent.class);
        engine.register(TileGridComponent.class);
        engine.register(SidebarTileComponent.class);
        engine.register(ClickEvent.class);
        engine.register(TextComponent.class);
        engine.register(ButtonComponent.class);
        engine.register(BooleanComponent.class);
        engine.register(TileComponent.class);

        List<TileDefinition> tiles;
        // Load tile definitions
        try {
            tiles = TileLoader.loadTiles();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        // Create tiled grid background for editor area
        Texture gridTex = TextureAtlas.get().getRegion("grid.png");
        int gridCols = 20;
        int gridRows = 15;
        float gridScale = 4.0f;
        float gridSize = 16.0f * gridScale;

        for (int row = 0; row < gridRows; row++) {
            for (int col = 0; col < gridCols; col++) {
                Entity<Context> gridEntity = engine.createEntity();
                TransformComponent gridTC = new TransformComponent(
                    new Vector2f(col * gridSize, row * gridSize),
                    new Vector2f(gridScale, gridScale), Anchor.TOP_LEFT);
                RenderComponent gridRC = new RenderComponent(gridTex, 0);
                gridEntity.addComponent(gridTC);
                gridEntity.addComponent(gridRC);
            }
        }

        // Editor click area - entire grid region (needs RenderComponent for ClickSystem)
        AABBf editorBox = new AABBf();
        editorBox.minX = 0;
        editorBox.minY = 0;
        editorBox.minZ = Float.NEGATIVE_INFINITY;
        editorBox.maxX = gridCols * gridSize;
        editorBox.maxY = gridRows * gridSize;
        editorBox.maxZ = Float.POSITIVE_INFINITY;

        editor = new TileGridComponent();

        // Click area for placing tiles in editor
        Entity<Context> entity = engine.createEntity();
        TransformComponent editorTC = new TransformComponent(new Vector2f(0, 0), new Vector2f(1, 1), Anchor.TOP_LEFT);
        entity.addComponent(editorTC);
        entity.addComponent(new ClickableComponent(editorBox, -1.0f)); // z=-1 so grid shows above it
        entity.addComponent(editor);

        editor.tiles = loadTileTextures(tiles);
        createTiles(editor.tiles);

        Entity<Context> textEntity = engine.createEntity();
        TransformComponent textTransform = new TransformComponent(new Vector2f(12, 12), new Vector2f(1, 1), Anchor.TOP_LEFT);
        TextComponent textComponent = new TextComponent(font, "Tiles", new Vector4f(0.9f, 0.9f, 0.9f, 1.0f));
        textComponent.layer = "ui";
        textEntity.addComponent(textTransform);
        textEntity.addComponent(textComponent);

        // Save Grid button
        Entity<Context> saveGridEntity = engine.createEntity();
        TransformComponent saveGridTransform = new TransformComponent(new Vector2f(12, 122), new Vector2f(1, 1), Anchor.TOP_LEFT);
        TextComponent saveGridText = new TextComponent(font, "Save Grid", new Vector4f(0.9f, 0.9f, 0.9f, 1.0f));
        saveGridText.layer = "ui";
        AABBf saveGridBox = new AABBf();
        saveGridBox.minX = 12;
        saveGridBox.minY = 122;
        saveGridBox.minZ = Float.NEGATIVE_INFINITY;
        saveGridBox.maxX = 112;
        saveGridBox.maxY = 152;
        saveGridBox.maxZ = Float.POSITIVE_INFINITY;
        saveGridEntity.addComponent(saveGridTransform);
        saveGridEntity.addComponent(saveGridText);
        saveGridEntity.addComponent(new ClickableComponent(saveGridBox));
        saveGridEntity.addComponent(new ButtonComponent("save_grid"));

        // Load Grid button
        Entity<Context> loadGridEntity = engine.createEntity();
        TransformComponent loadGridTransform = new TransformComponent(new Vector2f(12, 162), new Vector2f(1, 1), Anchor.TOP_LEFT);
        TextComponent loadGridText = new TextComponent(font, "Load Grid", new Vector4f(0.9f, 0.9f, 0.9f, 1.0f));
        loadGridText.layer = "ui";
        AABBf loadGridBox = new AABBf();
        loadGridBox.minX = 12;
        loadGridBox.minY = 162;
        loadGridBox.minZ = Float.NEGATIVE_INFINITY;
        loadGridBox.maxX = 112;
        loadGridBox.maxY = 192;
        loadGridBox.maxZ = Float.POSITIVE_INFINITY;
        loadGridEntity.addComponent(loadGridTransform);
        loadGridEntity.addComponent(loadGridText);
        loadGridEntity.addComponent(new ClickableComponent(loadGridBox));
        loadGridEntity.addComponent(new ButtonComponent("load_grid"));

        engine.addSystem(new ClickSystem(cameraManager));
        engine.addSystem(new RenderSystem(editorCamera, "default"));
        engine.addSystem(new RenderSystem(uiCamera, "ui"));
        engine.addSystem(new TileSystem(editor));
        engine.addSystem(new EditorSystem());
        engine.addSystem(new ButtonSystem(editor));
        engine.addSystem(new PanSystem(editorCamera));
        engine.addSystem(new TextRenderingSystem(uiCamera, "ui"));
        engine.addSystem(new CleanupSystem());
    }

    private void loop() {
        double lastTime = glfwGetTime();

        Context ctx = new Context();
        while (!glfwWindowShouldClose(window)) {
            double currentTime = glfwGetTime();
            float dt = (float) (currentTime - lastTime);
            lastTime = currentTime;

            ctx.currentTime = (float) currentTime;
            ctx.deltaTime = dt;

            glClearColor(43.0f / 255, 43.0f / 255, 43.0f / 255, 43.0f / 255);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glUseProgram(shaderId);
            TextureAtlas.get().bind();

            InputHandler.getInstance().tick();
            engine.update(ctx);

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    public static void main(String[] args) {
        new Main().run();
    }
}