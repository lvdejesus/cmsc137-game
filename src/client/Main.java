package client;

import framework.rendering.ShaderProgram;
import org.lwjgl.opengl.*;
import client.entities.Player;
import client.entities.Bullet;
import framework.engine.*;
import client.rendering.*;
import client.scenes.Scene;
import client.systems.*;
import client.components.TransformComponent;
import client.util.EngineConfig;

import java.nio.file.*;
import java.io.IOException;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.glfw.GLFW.*;

import org.lwjgl.BufferUtils;

import java.nio.Buffer;
//
import java.nio.DoubleBuffer;
//
import client.entities.Player;

public class Main {
    private int shaderProgram;
    private Camera camera;
    private Engine<Context> engine;
    private Window window;
    private static int currentScene = -1;
    private client.components.TransformComponent playerTransform;

    public void run() {

        // Initialize window
        this.window = Window.getWindow();
        this.window.init();

        // Initialize Render
        shaderProgram = ShaderProgram.getShaderProgram("res/shaders/shader.vert", "res/shaders/shader.frag");
        TextureAtlas.get();

        // Initialize Camera
        this.camera = new Camera();
        camera.setSize((int) window.getWidth(), (int) window.getHeight());
        glfwSetFramebufferSizeCallback(window.getHandle(), (handle, width, height) -> {
            camera.setSize(width, height);
            glViewport(0, 0, width, height);
        });

        // ECS set
        engine = new Engine<>();
        // Add systems and components
        EngineConfig.registerComponents(engine);
        EngineConfig.addSystems(engine, camera);

        Player player = new Player(engine);
        this.playerTransform = player.getEntity().getComponent(client.components.TransformComponent.class);

        loop();
        glDeleteProgram(shaderProgram);
        glfwTerminate();
    }

    private void loop() {

        float[] matrixBuffer = new float[16];
        double lastTime = glfwGetTime();

        // Create context (Stores Golbal Variables)
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

            for (InputHandler.MouseEvent event : InputHandler.getInstance().getEvents()) {
                if (event.type == InputHandler.MouseEventType.LEFT_CLICK && !event.consumed) {
                    event.consume();

                    if (playerTransform != null) {
                        float playerX = playerTransform.position.x;
                        float playerY = playerTransform.position.y;

                        // Calculate angle from player to mouse
                        float mouseX = event.position.x;
                        float mouseY = event.position.y;

                        float dx = mouseX - playerX;
                        float dy = mouseY - playerY;
                        float angle = (float) Math.toDegrees(Math.atan2(dy, dx));

                        new Bullet(engine, playerX, playerY, angle);
                    }
                }
            }

            glfwSwapBuffers(this.window.getHandle());
            glfwPollEvents();
        }
    }

    public static void main(String[] args) {
        new Main().run();
    }
}
