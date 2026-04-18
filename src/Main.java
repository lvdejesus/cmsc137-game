import org.joml.Vector2f;
import org.lwjgl.opengl.*;

import components.*;
import framework.engine.*;
import framework.rendering.*;
import systems.*;

import java.nio.file.*;
import java.io.IOException;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {
    private long window;
    private int shaderProgram;
    private Camera camera;
    private final int WIDTH = 800, HEIGHT = 600;
    private Engine<Context> engine;

    public void run() {
        init();

        engine = new Engine<>();
        engine.register(TransformComponent.class);
        engine.register(MovementComponent.class);
        engine.register(RenderComponent.class);
        engine.register(AnimationComponent.class);

        engine.addSystem(new MovementSystem());
        engine.addSystem(new AnimationSystem());
        engine.addSystem(new RenderSystem());

        Entity<Context> player = engine.createEntity();

        TransformComponent transformComponent = new TransformComponent(new Vector2f(400.0f, 300.0f));
        RenderComponent renderComponent = new RenderComponent();

        double currentTime = glfwGetTime();
        AnimationComponent animationComponent = new AnimationComponent(Animation.fromFile("tile.png", 2, 0.3f),
                (float) currentTime);

        player.addComponent(renderComponent);
        player.addComponent(transformComponent);
        player.addComponent(animationComponent);

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

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1); // VSync
        GL.createCapabilities();

        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        shaderProgram = loadShaderProgram("res/shaders/shader.vert", "res/shaders/shader.frag");

        TextureAtlas.get();
        camera = new Camera(WIDTH, HEIGHT);
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

            engine.update(ctx);

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    private int loadShaderProgram(String vertPath, String fragPath) {
        try {
            String vertCode = new String(Files.readAllBytes(Paths.get(vertPath)));
            String fragCode = new String(Files.readAllBytes(Paths.get(fragPath)));

            int vShader = glCreateShader(GL_VERTEX_SHADER);
            glShaderSource(vShader, vertCode);
            glCompileShader(vShader);
            checkShader(vShader);

            int fShader = glCreateShader(GL_FRAGMENT_SHADER);
            glShaderSource(fShader, fragCode);
            glCompileShader(fShader);
            checkShader(fShader);

            int program = glCreateProgram();
            glAttachShader(program, vShader);
            glAttachShader(program, fShader);
            glLinkProgram(program);

            glDeleteShader(vShader);
            glDeleteShader(fShader);
            return program;
        } catch (IOException e) {
            throw new RuntimeException("Shaders missing!");
        }
    }

    private void checkShader(int id) {
        if (glGetShaderi(id, GL_COMPILE_STATUS) == GL_FALSE) {
            System.err.println(glGetShaderInfoLog(id));
            throw new RuntimeException("Shader failed to compile!");
        }
    }

    public static void main(String[] args) {
        new Main().run();
    }
}
