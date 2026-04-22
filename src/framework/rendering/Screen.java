package framework.rendering;

import game.rendering.Camera;
import framework.engine.Engine;

public interface Screen<T> {
    void show(Engine<T> engine, Camera camera);
    void update(T context);
    void hide(Engine<T> engine);
}
