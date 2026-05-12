package framework.engine;

public abstract class IteratingEntitySystem<T> {
    private final Class<? extends Component>[] componentTypes;
    protected Engine<T> engine;
    private long familyMask;
    private boolean enabled = true;

    @SafeVarargs
    public IteratingEntitySystem(Class<? extends Component>... types) {
        this.componentTypes = types;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setEngine(Engine<T> engine) {
        this.engine = engine;
        this.familyMask = 0;
        for (Class<? extends Component> type : componentTypes) {
            int bitIndex = engine.getComponentIndex(type);
            this.familyMask |= 1L << bitIndex;
        }
    }

    public void update(T ctx) {
        long[] bitsets = engine.getBitsets();
        for (int i = 0; i < engine.getEntityMax(); i++) {
            if ((bitsets[i] & familyMask) == familyMask) {
                processEntity(i, ctx);
            }
        }
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

    protected long getFamilyMask() {
        return familyMask;
    }

    protected abstract void processEntity(int entityId, T ctx);
}
