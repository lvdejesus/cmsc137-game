package client.scenes;
import client.rendering.Camera;
import client.systems.client.Context;
import framework.engine.*;
public class SceneManager {
    private static Scene currScene;
    private static Camera camera;

    public static void setScene(Scene newScene, Engine<Context> engine){
        if (currScene != null){
            currScene.clean();
            engine.clearEntities();
            currScene.clearSystems(engine);
        }
        currScene = newScene;
        currScene.setCamera(camera);
        currScene.init(engine);
        currScene.addSystems(engine);
    }

    public static void update(){
        if (currScene != null){
            currScene.update();
        }
    }

    public static void setCamera(Camera camera) {
        SceneManager.camera = camera;
    }
}
