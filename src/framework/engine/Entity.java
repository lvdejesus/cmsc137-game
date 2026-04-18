package framework.engine;

public class Entity<T> {
    int id;
    Engine<T> engine;
    int version;

    Entity(Engine<T> engine, int id, int version) {
        this.engine = engine;
        this.id = id;
        this.version = version;
    }

    public void addComponent(Component component) {
        if (engine.isValid(id, version)) {
            engine.addComponent(id, component);
        } else {
            throw new RuntimeException("Modified a dead entity.");
        }

    }

    void destroy() {
        engine.destroyEntity(id);
    }
}
