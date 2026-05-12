package client.systems.client;

import org.joml.Vector2f;
import org.lwjgl.system.MemoryStack;

import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import static org.lwjgl.glfw.GLFW.*;

public class InputHandler {
    private final Set<Integer> toPress = new HashSet<>();
    private final Set<Integer> toRelease = new HashSet<>();

    private final Set<Integer> pressed = new HashSet<>();
    private final Set<Integer> held = new HashSet<>();
    private final Set<Integer> released = new HashSet<>();

    private final ArrayList<MouseEvent> eventsToAdd = new ArrayList<>();
    private final ArrayList<MouseEvent> events = new ArrayList<>();

    public Vector2f cursorPosition = new Vector2f(0, 0);
    public Vector2f lastCursorPosition = new Vector2f(0, 0);
    public boolean middleMouseHeld = false;
    public Vector2f middleMouseDragDelta = new Vector2f(0, 0);

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
            if (button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS) {
                try (MemoryStack stack = MemoryStack.stackPush()) {
                    DoubleBuffer xBuffer = stack.mallocDouble(1);
                    DoubleBuffer yBuffer = stack.mallocDouble(1);

                    glfwGetCursorPos(window, xBuffer, yBuffer);

                    Vector2f cursorPos = new Vector2f((float) xBuffer.get(), (float) yBuffer.get());
                    eventsToAdd.add(new MouseEvent(MouseEventType.LEFT_CLICK, cursorPos));
                }
            }
            if (button == GLFW_MOUSE_BUTTON_MIDDLE) {
                middleMouseHeld = (action == GLFW_PRESS);
                if (!middleMouseHeld) {
                    middleMouseDragDelta.set(0, 0);
                }
            }
        });

        glfwSetCursorPosCallback(windowHandle, (window, xpos, ypos) -> {
            lastCursorPosition.set(cursorPosition);
            cursorPosition.set((float) xpos, (float) ypos);
            if (middleMouseHeld) {
                middleMouseDragDelta.set(
                    cursorPosition.x - lastCursorPosition.x,
                    cursorPosition.y - lastCursorPosition.y
                );
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

    public Iterable<MouseEvent> getEvents() {
        return events;
    }


    public enum MouseEventType {
        LEFT_CLICK,
        RIGHT_CLICK,
    }

    public static class MouseEvent {
        public MouseEventType type;
        public Vector2f position;
        public boolean consumed;
        
        public MouseEvent(MouseEventType type, Vector2f position) {
            this.type = type;
            this.position = position;
            this.consumed = false;
        }
        
        public void consume() {
            this.consumed = true;
        }
    }

}
