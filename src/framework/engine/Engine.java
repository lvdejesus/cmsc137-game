package framework.engine;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

class ComponentRegistry {
    private int componentCount = 0;
    private final Map<Class<? extends Component>, Integer> componentIndex = new HashMap<>();

    int register(Class<? extends Component> type) {
        int id = componentCount++;
        componentIndex.put(type, id);
        return id;
    }

    int get(Class<? extends Component> type) {
        Integer index = componentIndex.get(type);
        if (index == null) {
            throw new RuntimeException(String.format("%s is not registered.", type.getName()));
        }
        return index;
    }
}

public class Engine<T> {
    private static final int INITIAL_CAPACITY = 128;

    private int entityMax = 0;
    private final Queue<Integer> entityReuse = new ArrayDeque<Integer>();
    private long[] componentBitset = new long[INITIAL_CAPACITY];
    private int[] entityVersions = new int[INITIAL_CAPACITY];
    private final Map<Class<? extends Component>, ComponentMapper<? extends Component>> mappers = new HashMap<>();
    private final ComponentRegistry componentRegistry = new ComponentRegistry();
    private final ArrayList<EntitySystem<T>> systems = new ArrayList<>();

    public Entity<T> createEntity() {
        int index;
        if (!entityReuse.isEmpty()) {
            index = entityReuse.remove();
        } else {
            index = entityMax++;
            ensureCapacity(index);
            entityVersions[index] = 1;
        }

        componentBitset[index] = 0L;
        return new Entity<>(this, index, entityVersions[index]);
    }

    public void register(Class<? extends Component> type) {
        int index = componentRegistry.register(type);
        mappers.put(type, new ComponentMapper<>(index, componentBitset.length));
    }

    @SuppressWarnings("unchecked")
    public void addComponent(int id, Component component) {
        ComponentMapper<Component> mapper = (ComponentMapper<Component>) mappers.get(component.getClass());
        componentBitset[id] |= 1L << mapper.getIndex();
        mapper.set(id, component);
    }

    public void removeComponent(int id, Class<? extends Component> component) {
        var mapper =  mappers.get(component);
        componentBitset[id] ^= 1L << mapper.getIndex();
        mapper.set(id, null);
    }

    public void destroyEntity(int id) {
        entityVersions[id]++;
        long bitset = componentBitset[id];
        for (ComponentMapper<?> mapper : mappers.values()) {
            if ((bitset & (1L << mapper.getIndex())) != 0) {
                mapper.set(id, null);
            }
        }
        componentBitset[id] = 0L;
        entityReuse.add(id);
    }

    private void ensureCapacity(int id) {
        if (id >= componentBitset.length) {
            int newCapacity = componentBitset.length * 2;
            componentBitset = java.util.Arrays.copyOf(componentBitset, newCapacity);
            entityVersions = java.util.Arrays.copyOf(entityVersions, newCapacity);
            for (ComponentMapper<?> mapper : mappers.values()) {
                mapper.resize(newCapacity);
            }
        }
    }

    boolean isValid(int index, int version) {
        return index < entityMax && entityVersions[index] == version;
    }

    long[] getBitsets() {
        return componentBitset;
    }

    int getEntityMax() {
        return entityMax;
    }

    int getComponentIndex(Class<? extends Component> component) {
        return componentRegistry.get(component);
    }

    public void addSystem(EntitySystem<T> system) {
        systems.add(system);
        system.setEngine(this);
    }

    public void update(T ctx) {
        for (var system : systems) {
            if (system.isEnabled()) {
                system.update(ctx);
            }
        }
    }

    public void clearEntities() {
        for (int i = 0; i < entityMax; i++) {
            if (componentBitset[i] != 0L) {
                destroyEntity(i);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <U extends Component> ComponentMapper<U> getMapper(Class<U> type) {
        var mapper = mappers.get(type);
        if (mapper == null) {
            throw new RuntimeException(String.format("%s not registered.", type));
        }

        return (ComponentMapper<U>) mapper;
    }

    @SuppressWarnings("unchecked")
    public <U extends EntitySystem<T>> U getSystem(Class<U> type) {
        for (var system : systems) {
            if (system.getClass().equals(type)) {
                return (U) system;
            }
        }
        return null;
    }
}
