package client.systems;

import java.util.Set;
import java.util.HashSet;

import static org.lwjgl.glfw.GLFW.*;

public class InputHandler {
    private Set<Integer> toPress = new HashSet<>();
    private Set<Integer> toRelease = new HashSet<>();

    private Set<Integer> pressed = new HashSet<>();
    private Set<Integer> held = new HashSet<>();
    private Set<Integer> released = new HashSet<>();

    private static InputHandler instance;

    public static InputHandler getInstance() {
        if (instance == null) {
            instance = new InputHandler();
        }

        return instance;
    }

    private InputHandler() {
    };

    public void register(long windowHandle) {
        glfwSetKeyCallback(windowHandle, (window, key, scancode, action, mods) -> {
            if (action == GLFW_PRESS) {
                toPress.add(key);
            } else if (action == GLFW_RELEASE) {
                toRelease.add(key);
            }
        });
    }

    public void tick() {
        pressed.clear();
        released.clear();

        for (int key : toPress) {
            pressed.add(key);
            held.add(key);
        }
        toPress.clear();

        for (int key : toRelease) {
            released.add(key);
            held.remove(key);
        }
        toRelease.clear();
    }

    public boolean keyDown(int key) {
        return pressed.contains(key);
    }

    public boolean keyUp(int key) {
        return released.contains(key);
    }

    public boolean key(int key) {
        return held.contains(key);
    }
}
