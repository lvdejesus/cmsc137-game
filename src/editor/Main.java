package editor;

import client.systems.client.*;
import editor.components.EditorComponent;
import editor.components.TileComponent;
import editor.components.ButtonComponent;
import editor.components.BooleanComponent;
import editor.systems.CleanupSystem;
import editor.systems.EditorSystem;
import editor.systems.TileSystem;
import editor.systems.ButtonSystem;
import editor.systems.StringCheckboxSystem;
import editor.systems.PanSystem;
import editor.util.TileRegistry;
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

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {
    private long window;
    private int shaderProgram;
    private CameraManager cameraManager;
    private Camera uiCamera;
    private Camera editorCamera;
    private final int WIDTH = 800, HEIGHT = 600;
    private final int SIDEBAR_WIDTH = 200;
    private Engine<Context> engine;

    private Font font;

    private EditorComponent editor;

    private static final int TILE_SIZE = 16;
    private static final int COLS = 3;
    private static final float SCALE = 4.0f;
    private static final float GAP = 4.0f;

    public void run() {
        init();
        loop();

        glDeleteProgram(shaderProgram);
        glfwTerminate();
    }

    private void createTiles() {
        float tileStartY = 140;

        int g = 0;
        for (String region : TextureAtlas.get().listRegions()) {
            if (!region.startsWith("tiles/")) continue;
            System.out.println("Found tile " + region);

            Texture t = TextureAtlas.get().getRegion(region);
            int xCount = t.width / TILE_SIZE;
            int yCount = t.height / TILE_SIZE;
            for (int i = 0; i < yCount; i++) {
                for (int j = 0; j < xCount; j++) {
                    float x = (g % COLS) * (TILE_SIZE * GAP + SCALE);
                    float y = tileStartY + (float) (g / COLS) * (TILE_SIZE * GAP + SCALE);

                    Entity<Context> entity = engine.createEntity();
                    TransformComponent transformComponent = new TransformComponent(new Vector2f(x, y),
                        new Vector2f(SCALE, SCALE), Anchor.TOP_LEFT);

                    float du = (t.u2 - t.u1) / xCount;
                    float dv = (t.v2 - t.v1) / yCount;

                    float u1 = t.u1 + du * j;
                    float u2 = t.u1 + du * (j + 1);
                    float v1 = t.v1 + dv * i;
                    float v2 = t.v1 + dv * (i + 1);

                    Texture tex = new Texture(u1, v1, u2, v2, 16, 16);
                    RenderComponent renderComponent = new RenderComponent(tex, 0);
                    renderComponent.layer = "ui";

                    editor.tiles.add(tex);

                    AABBf boundingBox = new AABBf();
                    boundingBox.minX = transformComponent.position.x;
                    boundingBox.minY = transformComponent.position.y;
                    boundingBox.minZ = Float.NEGATIVE_INFINITY;
                    boundingBox.maxX = transformComponent.position.x + renderComponent.texture.width * transformComponent.scale.x;
                    boundingBox.maxY = transformComponent.position.y + renderComponent.texture.width * transformComponent.scale.y;
                    boundingBox.maxZ = Float.POSITIVE_INFINITY;

                    ClickableComponent clickableComponent = new ClickableComponent(boundingBox);
                    TileComponent tileComponent = new TileComponent(g, false);

                    entity.addComponent(renderComponent);
                    entity.addComponent(transformComponent);
                    entity.addComponent(clickableComponent);
                    entity.addComponent(tileComponent);

                    g++;
                }
            }
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
        engine.register(EditorComponent.class);
        engine.register(TileComponent.class);
        engine.register(ClickEvent.class);
        engine.register(TextComponent.class);
        engine.register(ButtonComponent.class);
        engine.register(BooleanComponent.class);

        // Load tile definitions
        try {
            TileRegistry.loadTiles();
        } catch (IOException e) {
            e.printStackTrace();
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

        editor = new EditorComponent();

        // Click area for placing tiles in editor
        Entity<Context> entity = engine.createEntity();
        TransformComponent editorTC = new TransformComponent(new Vector2f(0, 0), new Vector2f(1, 1), Anchor.TOP_LEFT);
        entity.addComponent(editorTC);
        entity.addComponent(new ClickableComponent(editorBox, -1.0f)); // z=-1 so grid shows above it
        entity.addComponent(editor);

        createTiles();

        Entity<Context> textEntity = engine.createEntity();
        TransformComponent textTransform = new TransformComponent(new Vector2f(12, 12), new Vector2f(1, 1), Anchor.TOP_LEFT);
        TextComponent textComponent = new TextComponent(font, "Tiles", new Vector4f(0.1f, 0.1f, 0.1f, 1.0f));
        textComponent.layer = "ui";
        textEntity.addComponent(textTransform);
        textEntity.addComponent(textComponent);

        // Solid toggle button
        Entity<Context> solidEntity = engine.createEntity();
        TransformComponent solidTransform = new TransformComponent(new Vector2f(12, 52), new Vector2f(1, 1), Anchor.TOP_LEFT);
        TextComponent solidText = new TextComponent(font, "Solid: OFF", new Vector4f(0.1f, 0.1f, 0.1f, 1.0f));
        solidText.layer = "ui";
        AABBf solidBox = new AABBf();
        solidBox.minX = 12;
        solidBox.minY = 52;
        solidBox.minZ = Float.NEGATIVE_INFINITY;
        solidBox.maxX = 112;
        solidBox.maxY = 82;
        solidBox.maxZ = Float.POSITIVE_INFINITY;
        solidEntity.addComponent(solidTransform);
        solidEntity.addComponent(solidText);
        solidEntity.addComponent(new ClickableComponent(solidBox));
        solidEntity.addComponent(new ButtonComponent("toggle_solid"));
        solidEntity.addComponent(new BooleanComponent(false));

        // Save button
        Entity<Context> saveEntity = engine.createEntity();
        TransformComponent saveTransform = new TransformComponent(new Vector2f(12, 92), new Vector2f(1, 1), Anchor.TOP_LEFT);
        TextComponent saveText = new TextComponent(font, "SAVE", new Vector4f(0.1f, 0.1f, 0.1f, 1.0f));
        saveText.layer = "ui";
        AABBf saveBox = new AABBf();
        saveBox.minX = 12;
        saveBox.minY = 92;
        saveBox.minZ = Float.NEGATIVE_INFINITY;
        saveBox.maxX = 72;
        saveBox.maxY = 122;
        saveBox.maxZ = Float.POSITIVE_INFINITY;
        saveEntity.addComponent(saveTransform);
        saveEntity.addComponent(saveText);
        saveEntity.addComponent(new ClickableComponent(saveBox));
        saveEntity.addComponent(new ButtonComponent("save_tiles"));

        engine.addSystem(new ClickSystem(cameraManager));
        engine.addSystem(new RenderSystem(editorCamera, "default"));
        engine.addSystem(new RenderSystem(uiCamera, "ui"));
        engine.addSystem(new TileSystem(editor));
        engine.addSystem(new EditorSystem());
        engine.addSystem(new ButtonSystem(editor));
        engine.addSystem(new StringCheckboxSystem(editor));
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

            glClearColor(203.0f / 255, 219.0f / 255, 252.0f / 255, 255.0f / 255);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glUseProgram(shaderProgram);
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