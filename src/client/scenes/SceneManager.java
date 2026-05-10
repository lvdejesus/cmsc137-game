package client.scenes;
import client.systems.Context;
import framework.engine.*;
public class SceneManager {
    private static Scene currScene;

    public static void setScene(Scene newScene, Engine<Context> engine){
        if (currScene != null){
            currScene.clean();
            engine.clearEntities();
        }
        currScene = newScene;
        currScene.init(engine);
    }

    public static void update(){
        if (currScene != null){
            currScene.update();
        }
    }
}
