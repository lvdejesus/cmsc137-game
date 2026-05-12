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

    public int getId() {
        return id;
    }

    public void addComponent(Component component) {
        if (engine.isValid(id, version)) {
            engine.addComponent(id, component);
        } else {
            throw new RuntimeException("Modified a dead entity.");
        }
    }

    public void removeComponent(Component component){
        if (engine.isValid(id, version)) {
            engine.removeComponent(id,component.getClass());
        } else {
            throw new RuntimeException("Modified a dead entity.");
        }
    }

    public <U extends Component> U getComponent(Class<U> component) {
        return engine.getMapper(component).get(id);
    }

    public void destroy() {
        engine.destroyEntity(id);
    }
}
