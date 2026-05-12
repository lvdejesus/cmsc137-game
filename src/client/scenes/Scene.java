package client.scenes;
import client.systems.client.Context;
import framework.engine.Engine;

public interface Scene {
    void init(Engine<Context> engine);
    void update();
    void clean();
}
