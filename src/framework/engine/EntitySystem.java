package framework.engine;

public abstract class EntitySystem<T> {
    protected Engine<T> engine;

    public abstract void update(T ctx);

    private boolean enabled = true;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEngine(Engine<T> engine) {
        this.engine = engine;
    }

    protected long[] getBitsets() {
        return engine.getBitsets();
    }

    protected int getEntityMax() {
        return engine.getEntityMax();
    }

    protected int getComponentIndex(Class<? extends Component> component) {
        return engine.getComponentIndex(component);
    }
}
