package client.systems;

import static org.lwjgl.glfw.GLFW.*;

public class MouseListener {
    private static MouseListener instance;
    private double x,y,lastX,lastY;

    private MouseListener(){
        this.x = 0;
        this.y = 0;
        this.lastX = 0;
        this.lastY = 0;
    }

    public static MouseListener getInstance() {
        if (MouseListener.instance == null){
            MouseListener.instance = new MouseListener();
        }
        return MouseListener.instance;
    }   

    public void register(long windowHandle) {
        glfwSetCursorPosCallback(windowHandle, (window, xpos, ypos) -> {
            this.x = xpos;
            this.y = ypos;
        });
    }


    // Getters
    public static  double getX(){
        return getInstance().x;
    }
    public static double getY(){
        return getInstance().y;
    }
    public static double getDx(){
        return (getInstance().x - getInstance().lastX);
    }
    public static double getDy(){
        return (getInstance().y - getInstance().lastY);
    }
}
