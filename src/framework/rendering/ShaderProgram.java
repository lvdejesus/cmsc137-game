package framework.rendering;

import common.ResourceLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;


public class ShaderProgram {
    static HashMap<StringPair, ShaderProgram> programs = new HashMap<>();

    private record StringPair(String vertPath, String fragPath) {};
    
    private final int id;
    private final Map<String,Integer> uniformLocation = new HashMap<>();
    
    private ShaderProgram(int id) {
        this.id = id;
    }

    public static ShaderProgram getShaderProgram(String vertPath, String fragPath ){
        StringPair pair = new StringPair(vertPath, fragPath);
        ShaderProgram program = programs.get(pair);
        if (program == null) {
            int id = loadShaderProgram(vertPath, fragPath);
            program = new ShaderProgram(id);
            programs.put(pair, program);
        }
        return program;
    }

    public int getId(){return id;}

    private static int loadShaderProgram(String vertPath, String fragPath) {
        try {
            String vertCode = new String(ResourceLoader.read(vertPath), StandardCharsets.UTF_8);
            String fragCode = new String(ResourceLoader.read(fragPath), StandardCharsets.UTF_8);

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
        } catch (Exception e) {
            throw new RuntimeException("Shaders missing!", e);
        }
    }

    private static void checkShader(int id) {
        if (glGetShaderi(id, GL_COMPILE_STATUS) == GL_FALSE) {
            System.err.println(glGetShaderInfoLog(id));
            throw new RuntimeException("Shader failed to compile!");
        }
    }

    public int getUniformLocation(String uniformName) {
        // check cache
        if(uniformLocation.containsKey(uniformName)){
            return uniformLocation.get(uniformName);
        }
        int location = glGetUniformLocation(this.id, uniformName);
        uniformLocation.put(uniformName,location);
        return location;
    }
}
