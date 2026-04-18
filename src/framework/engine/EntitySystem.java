package framework.engine;

public abstract class EntitySystem<T> {
    private final Class<? extends Component>[] componentTypes;
    protected Engine<T> engine;
    private long familyMask;

    @SafeVarargs
    public EntitySystem(Class<? extends Component>... types) {
        this.componentTypes = types;
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

    protected abstract void processEntity(int entityId, T ctx);
}
