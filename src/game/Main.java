package game;

import game.systems.Context;
import game.systems.InputHandler;
import game.components.EditorComponent;
import game.components.TileComponent;
import framework.rendering.Screen;
import framework.rendering.ShaderProgram;
import org.lwjgl.opengl.*;
import framework.engine.*;
import game.rendering.*;
import game.components.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {
    private long window;
    private int shaderProgram;
    private Camera camera;
    private final int WIDTH = 800, HEIGHT = 600;
    private Engine<Context> engine;
    private Screen<Context> currentScreen;

    private EditorScreen editorScreen;
    private GameScreen gameScreen;

    public void run() {
        init();

        engine = new Engine<>();
        engine.register(TransformComponent.class);
        engine.register(MovementComponent.class);
        engine.register(RenderComponent.class);
        engine.register(AnimationComponent.class);
        engine.register(ClickableComponent.class);
        engine.register(EditorComponent.class);
        engine.register(TileComponent.class);
        engine.register(ClickEvent.class);
        engine.register(TextComponent.class);

        editorScreen = new EditorScreen();
        gameScreen = new GameScreen();

        setScreen(gameScreen);

        loop();

        if (currentScreen != null) {
            currentScreen.hide(engine);
        }
        glDeleteProgram(shaderProgram);
        glfwTerminate();
    }

    public void setScreen(Screen<Context> newScreen) {
        if (currentScreen != null) {
            currentScreen.hide(engine);
        }
        currentScreen = newScreen;
        if (currentScreen != null) {
            currentScreen.show(engine, camera);
        }
    }

    private void init() {
        if (!glfwInit()) throw new IllegalStateException("GLFW failed!");

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        window = glfwCreateWindow(WIDTH, HEIGHT, "Game", NULL, NULL);
        InputHandler.getInstance().register(window);

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
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

        while (!glfwWindowShouldClose(window)) {
            double currentTime = glfwGetTime();
            float dt = (float) (currentTime - lastTime);
            lastTime = currentTime;

            Context ctx = new Context();
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

            if (currentScreen != null) {
                currentScreen.update(ctx);
            }
            engine.update(ctx);

            if (InputHandler.getInstance().keyDown(GLFW_KEY_Y)) {
                setScreen(editorScreen);
            } else if (InputHandler.getInstance().keyDown(GLFW_KEY_U)) {
                setScreen(gameScreen);
            }

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    public static void main(String[] args) {
        new Main().run();
    }
}
