package editor;

import framework.rendering.ShaderProgram;
import org.joml.Vector2f;
import org.joml.primitives.AABBf;
import org.lwjgl.opengl.*;

import client.components.*;
import framework.engine.*;
import client.rendering.*;
import client.systems.*;

import java.nio.file.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryUtil.*;

record IntPair(int x, int y) {}

public class Main {
    private long window;
    private int shaderProgram;
    private Camera camera;
    private final int WIDTH = 800, HEIGHT = 600;
    private Engine<Context> engine;

    private ArrayList<Texture> tiles = new ArrayList<>();
    private Integer currentTile = null;
    private HashMap<IntPair, Entity<Context>> grid = new HashMap<>();

    public void run() {
        init();

        engine = new Engine<>();
        engine.register(TransformComponent.class);
        engine.register(RenderComponent.class);
        engine.register(ClickableComponent.class);

        engine.addSystem(new ClickSystem(camera));
        engine.addSystem(new RenderSystem());

        createTiles();
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

        ClickableComponent clickableComponent = new ClickableComponent(boundingBox, this::onGridClick);

        entity.addComponent(renderComponent);
        entity.addComponent(transformComponent);
        entity.addComponent(clickableComponent);

        loop();

        glDeleteProgram(shaderProgram);
        glfwTerminate();
    }

    private void createTiles() {
        int g = 0;
        for (String region : TextureAtlas.get().listRegions()) {
            if (!region.startsWith("tiles")) continue;

            Texture t = TextureAtlas.get().getRegion(region);
            int xCount = t.width / 16;
            int yCount = t.height / 16;
            for (int i = 0; i < yCount; i++) {
                for (int j = 0; j < xCount; j++) {
                    float x = (g % 3) * 68;
                    float y = (float) (Math.floor(g / 3.0f) * 68);

                    Entity<Context> entity = engine.createEntity();
                    TransformComponent transformComponent = new TransformComponent(new Vector2f(x, y),
                        new Vector2f(4.0f, 4.0f));

                    float du = (t.u2 - t.u1) / xCount;
                    float dv = (t.v2 - t.v1) / yCount;

                    float u1 = t.u1 + du * j;
                    float u2 = t.u1 + du * (j + 1);
                    float v1 = t.v1 + dv * i;
                    float v2 = t.v1 + dv * (i + 1);

                    Texture tex =  new Texture(u1, v1, u2, v2, 16, 16);
                    RenderComponent renderComponent = new RenderComponent(tex, 0);

                    tiles.add(tex);

                    AABBf boundingBox = new AABBf();
                    boundingBox.minX = transformComponent.position.x;
                    boundingBox.minY = transformComponent.position.y;
                    boundingBox.minZ = Float.NEGATIVE_INFINITY;
                    boundingBox.maxX = transformComponent.position.x + renderComponent.texture.width * transformComponent.scale.x;
                    boundingBox.maxY = transformComponent.position.y + renderComponent.texture.width * transformComponent.scale.y;
                    boundingBox.maxZ = Float.POSITIVE_INFINITY;

                    final int tile = g;
                    ClickableComponent clickableComponent = new ClickableComponent(boundingBox, ( _x, _y) -> {
                        currentTile = tile;
                    });


                    entity.addComponent(renderComponent);
                    entity.addComponent(transformComponent);
                    entity.addComponent(clickableComponent);

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

        camera = new Camera();
        camera.setSize(WIDTH, HEIGHT);

        glfwSetFramebufferSizeCallback(window, (window, width, height) -> {
            camera.setSize(width, height);
            glViewport(0, 0, width, height);
        });
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

    private void onGridClick(float x, float y) {
        if (currentTile == null) return;

        int xTile = (int) Math.floor((x - 200.0f) / 64.0f);
        int yTile = (int) Math.floor((y - 200.0f) / 64.0f);

        IntPair pair = new IntPair(xTile, yTile);
        Entity<Context> tileEntity = grid.get(pair);
        if (tileEntity != null) {
            RenderComponent rc = tileEntity.getComponent(RenderComponent.class);
            rc.texture = tiles.get(currentTile);
        } else {
            tileEntity = engine.createEntity();

            TransformComponent tc = new TransformComponent(new Vector2f(200.0f + xTile * 64.0f, 200.0f + yTile * 64.0f),
                new Vector2f(4.0f, 4.0f));
            RenderComponent rc = new RenderComponent(tiles.get(currentTile), 1);

            tileEntity.addComponent(rc);
            tileEntity.addComponent(tc);

            grid.put(pair, tileEntity);
        }
    }
}
