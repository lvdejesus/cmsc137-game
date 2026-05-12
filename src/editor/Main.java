package editor;

import client.systems.client.*;
import editor.components.EditorComponent;
import editor.components.TileComponent;
import editor.systems.CleanupSystem;
import editor.systems.EditorSystem;
import editor.systems.TileSystem;
import editor.systems.PropertySystem;
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
    private Engine<Context> engine;
    
    private Font font;

    private EditorComponent editor;

    private static final int TILE_SIZE = 16;
    private static final int COLS  = 3;
    private static final float SCALE = 4.0f;
    private static final float GAP = 4.0f;

    public void run() {
        init();
        loop();

        glDeleteProgram(shaderProgram);
        glfwTerminate();
    }

    private void createTiles() {
        int g = 0;
        for (String region : TextureAtlas.get().listRegions()) {
            if (!region.startsWith("tiles")) continue;

            Texture t = TextureAtlas.get().getRegion(region);
            int xCount = t.width / TILE_SIZE;
            int yCount = t.height / TILE_SIZE;
            for (int i = 0; i < yCount; i++) {
                for (int j = 0; j < xCount; j++) {
                    float x = (g % COLS) * (TILE_SIZE * GAP + SCALE);
                    float y = (float) (g / COLS) * (TILE_SIZE * GAP + SCALE);

                    Entity<Context> entity = engine.createEntity();
                    TransformComponent transformComponent = new TransformComponent(new Vector2f(x, y),
                        new Vector2f(SCALE, SCALE), Anchor.TOP_LEFT);

                    float du = (t.u2 - t.u1) / xCount;
                    float dv = (t.v2 - t.v1) / yCount;

                    float u1 = t.u1 + du * j;
                    float u2 = t.u1 + du * (j + 1);
                    float v1 = t.v1 + dv * i;
                    float v2 = t.v1 + dv * (i + 1);

                    Texture tex =  new Texture(u1, v1, u2, v2, 16, 16);
                    RenderComponent renderComponent = new RenderComponent(tex, 0);

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

        shaderProgram = ShaderProgram.getShaderProgram("res/shaders/shader.vert", "res/shaders/shader.frag");

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

        // Create cameras - UI on left (0-200), Editor on right (200-800)
        // UI camera: 200px screen space, 1:1 with world (world = 200xHEIGHT)
        // Editor camera: fixed world size (1280x960)
        
        cameraManager = new CameraManager();
        uiCamera = new Camera("ui");
        uiCamera.setViewport(0, 0, 200, HEIGHT);
        uiCamera.setWorldSize(200, HEIGHT);  // 1:1 scale
        
        editorCamera = new Camera("editor");
        editorCamera.setViewport(200, 0, WIDTH - 200, HEIGHT);
        editorCamera.setWorldSize(1280, 960);  // Fixed world size
        
        cameraManager.addCamera("ui", uiCamera);
        cameraManager.addCamera("editor", editorCamera);

        glfwSetFramebufferSizeCallback(window, (window, width, height) -> {
            uiCamera.setViewport(0, 0, 200, height);
            uiCamera.setWorldSize(200, height);  // UI keeps 1:1 scale
            float editorWidth = Math.max(0, width - 200);
            editorCamera.setViewport(200, 0, editorWidth, height);
            // Editor world size stays fixed at 1280x960
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

        // Load tile definitions
        try {
            TileRegistry.loadTiles();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create tiled grid background for editor area
        Texture gridTex = TextureAtlas.get().getRegion("tiles/grid.png");
        int gridCols = 20;
        int gridRows = 15;
        float gridScale = 4.0f;
        float gridSize = 16.0f * gridScale;

        for (int row = 0; row < gridRows; row++) {
            for (int col = 0; col < gridCols; col++) {
                Entity<Context> gridEntity = engine.createEntity();
                TransformComponent gridTC = new TransformComponent(
                    new Vector2f(200 + col * gridSize, 200 + row * gridSize),
                    new Vector2f(gridScale, gridScale), Anchor.TOP_LEFT);
                RenderComponent gridRC = new RenderComponent(gridTex, 0);
                gridEntity.addComponent(gridTC);
                gridEntity.addComponent(gridRC);
            }
        }

        // Editor click area - entire grid region (needs RenderComponent for ClickSystem)
        AABBf editorBox = new AABBf();
        editorBox.minX = 200;
        editorBox.minY = 200;
        editorBox.minZ = Float.NEGATIVE_INFINITY;
        editorBox.maxX = 200 + gridCols * gridSize;
        editorBox.maxY = 200 + gridRows * gridSize;
        editorBox.maxZ = Float.POSITIVE_INFINITY;

        editor = new EditorComponent();

        // Click area for placing tiles in editor
        Entity<Context> entity = engine.createEntity();
        TransformComponent editorTC = new TransformComponent(new Vector2f(200, 200), new Vector2f(1, 1), Anchor.TOP_LEFT);
        entity.addComponent(editorTC);
        entity.addComponent(new ClickableComponent(editorBox, -1.0f)); // z=-1 so grid shows above it
        entity.addComponent(editor);

        createTiles();
        
        Entity<Context> textEntity = engine.createEntity();
        TransformComponent textTransform = new TransformComponent(new Vector2f(12, 12), new Vector2f(1, 1), Anchor.TOP_LEFT);
        TextComponent textComponent = new TextComponent(font, "Tiles", new Vector4f(0.1f, 0.1f, 0.1f, 1.0f));
        textEntity.addComponent(textTransform);
        textEntity.addComponent(textComponent);

        // Solid toggle button - moved to right side to avoid overlap
        Entity<Context> solidEntity = engine.createEntity();
        TransformComponent solidTransform = new TransformComponent(new Vector2f(300, 12), new Vector2f(1, 1), Anchor.TOP_LEFT);
        TextComponent solidText = new TextComponent(font, "Solid: OFF", new Vector4f(0.1f, 0.1f, 0.1f, 1.0f));
        AABBf solidBox = new AABBf();
        solidBox.minX = 300; solidBox.minY = 12; solidBox.minZ = Float.NEGATIVE_INFINITY;
        solidBox.maxX = 400; solidBox.maxY = 42; solidBox.maxZ = Float.POSITIVE_INFINITY;
        solidEntity.addComponent(solidTransform);
        solidEntity.addComponent(solidText);
        solidEntity.addComponent(new ClickableComponent(solidBox));

        // Save button
        Entity<Context> saveEntity = engine.createEntity();
        TransformComponent saveTransform = new TransformComponent(new Vector2f(300, 52), new Vector2f(1, 1), Anchor.TOP_LEFT);
        TextComponent saveText = new TextComponent(font, "SAVE", new Vector4f(0.1f, 0.1f, 0.1f, 1.0f));
        AABBf saveBox = new AABBf();
        saveBox.minX = 300; saveBox.minY = 52; saveBox.minZ = Float.NEGATIVE_INFINITY;
        saveBox.maxX = 360; saveBox.maxY = 82; saveBox.maxZ = Float.POSITIVE_INFINITY;
        saveEntity.addComponent(saveTransform);
        saveEntity.addComponent(saveText);
        saveEntity.addComponent(new ClickableComponent(saveBox));

        engine.addSystem(new ClickSystem(cameraManager));
        engine.addSystem(new RenderSystem());
        engine.addSystem(new TileSystem(editor));
        engine.addSystem(new EditorSystem());
        engine.addSystem(new PropertySystem(editor, font));
        engine.addSystem(new PanSystem(editorCamera));
        engine.addSystem(new CleanupSystem());
        engine.addSystem(new TextRenderingSystem());
    }

    private void loop() {
        float[] matrixBuffer = new float[16];
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

            // Set viewport for editor camera
            glViewport((int)editorCamera.viewportX, (int)editorCamera.viewportY, 
                       (int)editorCamera.viewportWidth, (int)editorCamera.viewportHeight);

            glUseProgram(shaderProgram);
            int pvLoc = glGetUniformLocation(shaderProgram, "u_ProjectionView");
            editorCamera.getProjectionViewMatrix().get(matrixBuffer);
            glUniformMatrix4fv(pvLoc, false, matrixBuffer);

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