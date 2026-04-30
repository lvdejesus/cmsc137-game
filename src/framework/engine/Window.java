package framework.engine;
import org.lwjgl.opengl.*;
import client.systems.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_LEQUAL;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDepthFunc;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryUtil.*;


public class Window {
    
    private int width,height;
    private static Window window = null;
    private String title; 
    private long windowHandle;

    private Window(){
        this.width = 800;
        this.height = 600;
        this.title = "GameTitle";
    }

    public static Window getWindow() {
        if(Window.window == null){
            Window.window = new Window();
        }
        return Window.window;
    }
    
    public void init(){

        if (!glfwInit()) {
            throw new IllegalStateException("GLFW failed!");
        }
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        this.windowHandle = glfwCreateWindow(width, height, title, NULL, NULL);
        
        if (windowHandle == NULL) {
            throw new RuntimeException("Window failed!");
        }

        InputHandler.getInstance().register(windowHandle);

        glfwMakeContextCurrent(windowHandle);
        glfwSwapInterval(1); // VSync
        glfwShowWindow(windowHandle);
        GL.createCapabilities();

        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    public long getHandle(){return windowHandle;}
    public long getHeight(){return height;}
    public long getWidth(){return width;}

}
