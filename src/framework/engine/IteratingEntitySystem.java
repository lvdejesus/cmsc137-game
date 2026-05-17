package framework.engine;

import java.util.ArrayList;
import java.util.List;

public abstract class IteratingEntitySystem<T> extends EntitySystem<T> {
    private final Class<? extends Component>[] componentTypes;
    private long familyMask;
    private final List<Integer> entityBuffer = new ArrayList<>();

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
        entityBuffer.clear();
        for (var entry : engine.getArchetypes().entrySet()) {
            if ((entry.getKey() & familyMask) == familyMask) {
                entityBuffer.addAll(entry.getValue());
            }
        }
        for (int entityId : entityBuffer) {
            processEntity(entityId, ctx);
        }
    }

    protected long getFamilyMask() {
        return familyMask;
    }

    protected abstract void processEntity(int entityId, T ctx);
}
