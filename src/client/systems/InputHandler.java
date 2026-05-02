package client.systems;

import org.joml.Vector2f;
import org.lwjgl.system.MemoryStack;

import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import static org.lwjgl.glfw.GLFW.*;

public class InputHandler {
    private Set<Integer> toPress = new HashSet<>();
    private Set<Integer> toRelease = new HashSet<>();

    private Set<Integer> pressed = new HashSet<>();
    private Set<Integer> held = new HashSet<>();
    private Set<Integer> released = new HashSet<>();

    // Mouse button tracking
    private Set<Integer> mouseToPress = new HashSet<>();
    private Set<Integer> mouseToRelease = new HashSet<>();

    private Set<Integer> mousePressed = new HashSet<>();
    private Set<Integer> mouseHeld = new HashSet<>();
    private Set<Integer> mouseReleased = new HashSet<>();

    private ArrayList<MouseEvent> eventsToAdd = new ArrayList<>();
    private ArrayList<MouseEvent> events = new ArrayList<>();

    private static InputHandler instance;

    public static InputHandler getInstance() {
        if (instance == null) {
            instance = new InputHandler();
        }
        return instance;
    }

    private InputHandler() {
    }

    public void register(long windowHandle) {
        glfwSetKeyCallback(windowHandle, (window, key, scancode, action, mods) -> {
            if (action == GLFW_PRESS) {
                toPress.add(key);
            } else if (action == GLFW_RELEASE) {
                toRelease.add(key);
            }
        });

        glfwSetMouseButtonCallback(windowHandle, (window, button, action, mods) -> {
            if (action == GLFW_PRESS) {
                mouseToPress.add(button);

                if (button == GLFW_MOUSE_BUTTON_LEFT || button == GLFW_MOUSE_BUTTON_RIGHT) {
                    try (MemoryStack stack = MemoryStack.stackPush()) {
                        DoubleBuffer xBuffer = stack.mallocDouble(1);
                        DoubleBuffer yBuffer = stack.mallocDouble(1);

                        glfwGetCursorPos(window, xBuffer, yBuffer);

                        Vector2f cursorPos = new Vector2f((float) xBuffer.get(), (float) yBuffer.get());
                        MouseEventType type = (button == GLFW_MOUSE_BUTTON_LEFT)
                                ? MouseEventType.LEFT_CLICK
                                : MouseEventType.RIGHT_CLICK;

                        eventsToAdd.add(new MouseEvent(type, cursorPos));
                    }
                }
            } else if (action == GLFW_RELEASE) {
                mouseToRelease.add(button);
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

        // Mouse state updates
        mousePressed.clear();
        mouseReleased.clear();

        for (int button : mouseToPress) {
            mousePressed.add(button);
            mouseHeld.add(button);
        }
        mouseToPress.clear();

        for (int button : mouseToRelease) {
            mouseReleased.add(button);
            mouseHeld.remove(button);
        }
        mouseToRelease.clear();

        events.clear();
        events.addAll(eventsToAdd);
        eventsToAdd.clear();
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

    public boolean mouseButtonDown(int button) {
        return mousePressed.contains(button);
    }

    public boolean mouseButton(int button) {
        return mouseHeld.contains(button);
    }

    public boolean mouseButtonUp(int button) {
        return mouseReleased.contains(button);
    }

    public Iterable<MouseEvent> getEvents() {
        return events;
    }

    public enum MouseEventType {
        LEFT_CLICK,
        RIGHT_CLICK,
    }

    public static class MouseEvent {
        MouseEventType type;
        Vector2f position;
        boolean consumed;

        MouseEvent(MouseEventType type, Vector2f position) {
            this.type = type;
            this.position = position;
            this.consumed = false;
        }

        void consume() {
            this.consumed = true;
        }
    }
}