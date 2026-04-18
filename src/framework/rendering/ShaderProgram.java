package framework.rendering;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;

public class ShaderProgram {
    static HashMap<StringPair, Integer> programs = new HashMap<>();

    private record StringPair(String vertPath, String fragPath) {}

    public static int getShaderProgram(String vertPath, String fragPath ){
        StringPair pair = new StringPair(vertPath, fragPath);
        Integer program = programs.get(pair);
        if (program == null) {
            program = loadShaderProgram(vertPath, fragPath);
        }
        return program;
    }

    private static int loadShaderProgram(String vertPath, String fragPath) {
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

    private static void checkShader(int id) {
        if (glGetShaderi(id, GL_COMPILE_STATUS) == GL_FALSE) {
            System.err.println(glGetShaderInfoLog(id));
            throw new RuntimeException("Shader failed to compile!");
        }
    }
}
