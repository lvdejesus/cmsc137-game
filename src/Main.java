import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import java.nio.file.*;
import java.io.IOException;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {
    private long window;
    private int shaderProgram;
    private SpriteBatch batch;
    private Camera camera;
    private final int WIDTH = 800, HEIGHT = 600;

    public void run() {
        init();
        loop();

        glDeleteProgram(shaderProgram);
        glfwTerminate();
    }

    private void init() {
        if (!glfwInit()) throw new IllegalStateException("GLFW failed!");

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        window = glfwCreateWindow(WIDTH, HEIGHT, "Game", NULL, NULL);
        if (window == NULL) throw new RuntimeException("Window failed!");

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1); // VSync
        GL.createCapabilities();

        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        shaderProgram = loadShaderProgram("res/shaders/shader.vert", "res/shaders/shader.frag");

        TextureAtlas.get();
        batch = new SpriteBatch();
        camera = new Camera(WIDTH, HEIGHT);
    }

    private void loop() {
        Sprite player = new Sprite("tile.png", 400, 300, 0.1f);
        float rotation = 0;
        float[] matrixBuffer = new float[16];

        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glUseProgram(shaderProgram);
            int pvLoc = glGetUniformLocation(shaderProgram, "u_ProjectionView");
            camera.getProjectionViewMatrix().get(matrixBuffer);
            glUniformMatrix4fv(pvLoc, false, matrixBuffer);

            TextureAtlas.get().bind();

            rotation += 1.0f;

            Texture tex = player.getTexture();
            batch.draw(tex, player.position.x, player.position.y, player.position.z,
                    rotation, player.scale.x, player.scale.y, 1, 1, 1, 1);

            for(int i = 0; i < 10; i++) {
                batch.draw(TextureAtlas.get().getRegion("grass.png"),
                        i * 64, 100, 0.5f, 0, 64, 64, 1, 1, 1, 1);
            }

            batch.flush();

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

    public static void main(String[] args) { new Main().run(); }
}
