package client.scenes;
import client.systems.Context;
import framework.engine.*;
public class SceneManager {
    private static Scene currScene;

    public static void setScene(Scene newScene, Engine<Context> engine){
        if (currScene != null){
            // Cleans scene
            currScene.clean();
            // Todo Erase all entities
        }
        currScene = newScene;
        currScene.init(engine);
    }


}
