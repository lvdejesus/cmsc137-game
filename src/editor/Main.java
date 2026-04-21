package editor;

import editor.components.EditorComponent;
import editor.components.TileComponent;
import editor.systems.CleanupSystem;
import editor.systems.EditorSystem;
import editor.systems.TileSystem;
import framework.rendering.ShaderProgram;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.joml.primitives.AABBf;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import client.components.*;
import framework.engine.*;
import client.rendering.*;
import client.systems.*;
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
    private Camera camera;
    private final int WIDTH = 800, HEIGHT = 600;
    private Engine<Context> engine;
    
    private Font font;

    private EditorComponent editor;

    private static int TILE_SIZE = 16;
    private static int COLS  = 3;
    private static float SCALE = 4.0f;
    private static float GAP = 4.0f;

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
                        new Vector2f(SCALE, SCALE));

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
                    TileComponent tileComponent = new TileComponent(g);

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

        camera = new Camera();
        camera.setSize(WIDTH, HEIGHT);

        glfwSetFramebufferSizeCallback(window, (window, width, height) -> {
            camera.setSize(width, height);
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

        Entity<Context> entity = engine.createEntity();

        TransformComponent transformComponent = new TransformComponent(new Vector2f(200, 200),
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
        editor = new EditorComponent();

        entity.addComponent(renderComponent);
        entity.addComponent(transformComponent);
        entity.addComponent(clickableComponent);
        entity.addComponent(editor);

        createTiles();
        
        Entity<Context> textEntity = engine.createEntity();
        TransformComponent textTransform = new TransformComponent(new Vector2f(12, 12), new Vector2f(1, 1));
        TextComponent textComponent = new TextComponent(font, "Tiles", new Vector4f(0.1f, 0.1f, 0.1f, 1.0f));
        textEntity.addComponent(textTransform);
        textEntity.addComponent(textComponent);

        engine.addSystem(new ClickSystem(camera));
        engine.addSystem(new RenderSystem());
        engine.addSystem(new TileSystem(editor));
        engine.addSystem(new EditorSystem());
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

            glUseProgram(shaderProgram);
            int pvLoc = glGetUniformLocation(shaderProgram, "u_ProjectionView");
            camera.getProjectionViewMatrix().get(matrixBuffer);
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