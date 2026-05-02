package client;

import framework.rendering.ShaderProgram;
import org.lwjgl.opengl.*;
import client.entities.Player;
import framework.engine.*;
import client.rendering.*;
import client.scenes.Scene;
import client.systems.*;
import client.util.EngineConfig;
import client.util.DungeonGenerator;
import client.components.WorldComponent;
import java.nio.file.*;
import java.io.IOException;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.glfw.GLFW.*;

import org.lwjgl.BufferUtils;

import java.nio.Buffer;
import java.nio.DoubleBuffer;

public class Main {
    private int shaderProgram;
    private Camera camera;
    private Engine<Context> engine;
    private Window window;
    private static int currentScene = -1;

    public void run() {
        // Initialize window
        this.window = Window.getWindow();
        this.window.init();

        // Initialize Render
        shaderProgram = ShaderProgram.getShaderProgram("res/shaders/shader.vert", "res/shaders/shader.frag");
        TextureAtlas.get();

        // Initialize Camera
        this.camera = new Camera();
        camera.setSize((int)window.getWidth(), (int)window.getHeight());
        glfwSetFramebufferSizeCallback(window.getHandle(), (handle, width, height) -> {
            camera.setSize(width, height);
            glViewport(0, 0, width, height);
        });

        // ECS setup
        engine = new Engine<>();
        EngineConfig.registerComponents(engine);
        EngineConfig.addSystems(engine);

        // Create Dungeon
        DungeonGenerator generator = new DungeonGenerator(50, 50);
        int[][] map = generator.generate();
        engine.createEntity().addComponent(new WorldComponent(map));

        // Find walkable spawn point
        float spawnX = 400, spawnY = 300;
        findSpawn:
        for (int x = 0; x < map.length; x++) {
            for (int y = 0; y < map[0].length; y++) {
                if (map[x][y] == 0) { // TILE_FLOOR
                    spawnX = x * 32;
                    spawnY = y * 32;
                    break findSpawn;
                }
            }
        }

        // Create player
        Player player = new Player(engine);
        player.getEntity().getComponent(client.components.TransformComponent.class).position.set(spawnX, spawnY);

        loop();
        glDeleteProgram(shaderProgram);
        glfwTerminate();
    }

    private void loop() {
        float[] matrixBuffer = new float[16];
        double lastTime = glfwGetTime();

        Context ctx = new Context();

        while (!glfwWindowShouldClose(this.window.getHandle())) {
            double currentTime = glfwGetTime();
            float dt = (float) (currentTime - lastTime);
            lastTime = currentTime;

            ctx.currentTime = (float) currentTime;
            ctx.deltaTime = dt;
            ctx.cursor.set(
                (float) MouseListener.getX(),
                (float) MouseListener.getY()
            );

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glUseProgram(shaderProgram);
            int pvLoc = glGetUniformLocation(shaderProgram, "u_ProjectionView");
            camera.getProjectionViewMatrix().get(matrixBuffer);
            glUniformMatrix4fv(pvLoc, false, matrixBuffer);

            TextureAtlas.get().bind();

            engine.update(ctx);
            InputHandler.getInstance().tick();

            glfwSwapBuffers(this.window.getHandle());
            glfwPollEvents();
        }
    }

    public static void main(String[] args) {
        new Main().run();
    }
}
