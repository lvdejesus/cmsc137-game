package client.scenes;
import client.rendering.Camera;
import client.systems.client.Context;
import framework.engine.Engine;

public abstract class Scene {
    protected Camera camera;

    public abstract void init(Engine<Context> engine);
    public abstract void update();
    public abstract void clean();

    public void setCamera(Camera camera) {
        this.camera = camera;
    }
}
