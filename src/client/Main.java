package client;

import framework.rendering.ShaderProgram;
import org.joml.Vector2f;
import org.lwjgl.opengl.*;
import client.entities.Player;
import client.components.*;
import client.components.player.PlayerTagComponent;
import framework.engine.*;
import client.rendering.*;
import client.systems.*;
import client.systems.player.PlayerRotationSystem;

import java.nio.file.*;
import java.io.IOException;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryUtil.*;
import org.lwjgl.BufferUtils;

import java.nio.Buffer;
//
import java.nio.DoubleBuffer;
//
import client.entities.Player;

public class Main {
    private long window;
    private int shaderProgram;
    private Camera camera;
    private final int WIDTH = 800, HEIGHT = 600;
    private Engine<Context> engine;

    public void run() {
        init();

        engine = new Engine<>();
        // Register components
        engine.register(TransformComponent.class);
        engine.register(MovementComponent.class);
        engine.register(RenderComponent.class);
        engine.register(AnimationComponent.class);
        engine.register(PlayerTagComponent.class);
        
        // Add systems
        engine.addSystem(new MovementSystem());
        engine.addSystem(new PhysicsSystem());
        engine.addSystem(new AnimationSystem());
        engine.addSystem(new RenderSystem());
        
        // Player Specific systems
        engine.addSystem(new PlayerRotationSystem());

        // Create player 
        Player player = new Player(engine);
        
        loop();
        glDeleteProgram(shaderProgram);
        glfwTerminate();
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
        DoubleBuffer xBuf = BufferUtils.createDoubleBuffer(1);
        DoubleBuffer yBuf = BufferUtils.createDoubleBuffer(1);
        
        float[] matrixBuffer = new float[16];
        double lastTime = glfwGetTime();

        Context ctx = new Context();
        while (!glfwWindowShouldClose(window)) {
            double currentTime = glfwGetTime();
            float dt = (float) (currentTime - lastTime);
            lastTime = currentTime;

            ctx.currentTime = (float) currentTime;
            ctx.deltaTime = dt;
            
            // Update cursor pos
            glfwGetCursorPos(window, xBuf,yBuf);
            ctx.cursor.set((float)xBuf.get(0),(float)yBuf.get(0));
            xBuf.rewind();
            yBuf.rewind();
            //
            
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            
            glUseProgram(shaderProgram);
            int pvLoc = glGetUniformLocation(shaderProgram, "u_ProjectionView");
            camera.getProjectionViewMatrix().get(matrixBuffer);
            glUniformMatrix4fv(pvLoc, false, matrixBuffer);

            TextureAtlas.get().bind();

            engine.update(ctx);
            InputHandler.getInstance().tick();

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    public static void main(String[] args) {
        new Main().run();
    }
}
