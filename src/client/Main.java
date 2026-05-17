package client;

import client.systems.client.Context;
import client.systems.client.InputHandler;
import common.NativeLoader;
import framework.rendering.ShaderProgram;
import framework.engine.*;
import client.rendering.*;
import client.util.EngineConfig;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.glfw.GLFW.*;

//
//


public class Main {
    private ShaderProgram shaderProgram;
    private int shaderId;
    private Camera camera;
    private Camera fixedCamera;
    private Engine<Context> engine;
    private Window window;
    private static final int currentScene = -1;
    private client.components.TransformComponent playerTransform;

    public void run() {

        // Initialize window
        this.window = Window.getWindow();
        this.window.init();

        // Initialize Render
        shaderProgram = ShaderProgram.getShaderProgram("res/shaders/default.vert", "res/shaders/default.frag");
        shaderId = shaderProgram.getId();
        TextureAtlas.get();

        // Initialize Camera
        this.camera = new Camera("default", 1.2f);
        camera.setSize((int) window.getWidth(), (int) window.getHeight());

        this.fixedCamera = new Camera("fixed");
        fixedCamera.setSize((int) window.getWidth(), (int) window.getHeight());

        glfwSetFramebufferSizeCallback(window.getHandle(), (handle, width, height) -> {
            camera.setSize(width, height);
            fixedCamera.setSize(width, height);
            Window.getWindow().setSize(width, height);
            glViewport(0, 0, width, height);
        });

        // ECS set
        engine = new Engine<>();
        // Add systems and components
        EngineConfig.registerComponents(engine);
        EngineConfig.addSystems(engine, camera, fixedCamera);

        // Set initial scene
        client.scenes.SceneManager.setCamera(camera);
        client.scenes.SceneManager.setScene(new client.scenes.MenuScene(window), engine);

        loop();
        glDeleteProgram(shaderId);
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
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glUseProgram(shaderId);

            TextureAtlas.get().bind();

            // Scene specific update
            client.scenes.SceneManager.update();

            engine.update(ctx);
            InputHandler.getInstance().tick();

            glfwSwapBuffers(this.window.getHandle());
            glfwPollEvents();
        }
    }

    public static void main(String[] args) {
        NativeLoader.loadNatives();
        new Main().run();
    }
}
