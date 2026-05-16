package client.components;

import framework.engine.Component;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class DespawnTimerComponent implements Component {
    public double time;

    public DespawnTimerComponent(float time) {
        this.time = glfwGetTime() + time;
    }
}
