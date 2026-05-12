package framework.engine;

public abstract class IteratingEntitySystem<T> extends EntitySystem<T> {
    private final Class<? extends Component>[] componentTypes;
    private long familyMask;

    @SafeVarargs
    public IteratingEntitySystem(Class<? extends Component>... types) {
        super();

        this.componentTypes = types;
    }

    public void setEngine(Engine<T> engine) {
        super.setEngine(engine);

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

    protected long getFamilyMask() {
        return familyMask;
    }

    protected abstract void processEntity(int entityId, T ctx);
}
