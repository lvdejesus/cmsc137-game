package client.rendering;

import java.util.HashMap;
import java.util.Map;

public class CameraManager {
    private final Map<String, Camera> cameras = new HashMap<>();

    public void addCamera(String name, Camera camera) {
        cameras.put(name, camera);
    }

    public Camera getCamera(String name) {
        return cameras.get(name);
    }

    public Camera getCameraByScreenPoint(float screenX, float screenY) {
        for (Camera camera : cameras.values()) {
            if (camera.containsScreenPoint(screenX, screenY)) {
                return camera;
            }
        }
        return null;
    }

    public Iterable<Camera> getAllCameras() {
        return cameras.values();
    }
}