package framework.engine;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Stream;

class ComponentRegistry {
    private int componentCount = 0;
    private final Map<Class<? extends Component>, Integer> componentIndex = new HashMap<>();
    private final Map<Integer, Class<? extends Component>> invertedIndex = new HashMap<>();

    int register(Class<? extends Component> type) {
        int id = componentCount++;
        componentIndex.put(type, id);
        invertedIndex.put(id, type);
        return id;
    }

    int get(Class<? extends Component> type) {
        Integer index = componentIndex.get(type);
        if (index == null) {
            throw new RuntimeException(String.format("%s is not registered.", type.getName()));
        }
        return index;
    }

    Class<? extends Component> index(int index) {
        return invertedIndex.get(index);
    }

    public Iterable<Class<? extends Component>> getComponentClasses() {
        return invertedIndex.values();
    }
}

public class Engine<T> {
    private static final int INITIAL_CAPACITY = 128;

    private int entityMax = 0;
    private final Queue<Integer> entityReuse = new ArrayDeque<Integer>();
    private long[] componentBitset = new long[INITIAL_CAPACITY];
    private int[] entityVersions = new int[INITIAL_CAPACITY];
    private boolean[] isAlive = new boolean[INITIAL_CAPACITY];

    private final Map<Class<? extends Component>, ComponentMapper<? extends Component>> mappers = new HashMap<>();
    private final ComponentRegistry componentRegistry = new ComponentRegistry();
    private final Map<Integer, ArrayList<EntitySystem<T>>> systems = new HashMap<>();
    private final Map<Long, List<Integer>> archetypes = new HashMap<>();

    public Entity<T> createEntity() {
        int index;
        if (!entityReuse.isEmpty()) {
            index = entityReuse.remove();
        } else {
            index = entityMax++;
            ensureCapacity(index);
            entityVersions[index] = 1;
        }

        isAlive[index] = true;
        componentBitset[index] = 0L;
        archetypes.computeIfAbsent(0L, k -> new ArrayList<>()).add(index);
        return new Entity<>(this, index, entityVersions[index]);
    }

    public void register(Class<? extends Component> type) {
        int index = componentRegistry.register(type);
        mappers.put(type, new ComponentMapper<>(index, componentBitset.length));
    }

    @SuppressWarnings("unchecked")
    public void addComponent(int id, Component component) {
        ComponentMapper<Component> mapper = (ComponentMapper<Component>) mappers.get(component.getClass());
        long oldSig = componentBitset[id];
        componentBitset[id] |= 1L << mapper.getIndex();
        mapper.set(id, component);
        moveArchetype(id, oldSig, componentBitset[id]);
    }

    public void removeComponent(int id, Class<? extends Component> component) {
        var mapper = mappers.get(component);
        long oldSig = componentBitset[id];
        componentBitset[id] &= ~(1L << mapper.getIndex());
        mapper.set(id, null);
        moveArchetype(id, oldSig, componentBitset[id]);
    }

    private void moveArchetype(int entityId, long oldSig, long newSig) {
        if (oldSig == newSig) return;
        List<Integer> oldList = archetypes.get(oldSig);
        if (oldList != null) {
            oldList.remove((Integer) entityId);
            if (oldList.isEmpty()) archetypes.remove(oldSig);
        }
        archetypes.computeIfAbsent(newSig, k -> new ArrayList<>()).add(entityId);
    }

    public void destroyEntity(int id) {
        if (!isAlive[id]) {
            throw new RuntimeException(String.format("Tried destroying a dead entity %d.", id));
        }
        isAlive[id] = false;
        entityVersions[id]++;
        long bitset = componentBitset[id];
        for (ComponentMapper<?> mapper : mappers.values()) {
            if ((bitset & (1L << mapper.getIndex())) != 0) {
                mapper.set(id, null);
            }
        }
        List<Integer> list = archetypes.get(bitset);
        if (list != null) {
            list.remove((Integer) id);
            if (list.isEmpty()) archetypes.remove(bitset);
        }
        componentBitset[id] = 0L;
        entityReuse.add(id);
    }

    private void ensureCapacity(int id) {
        if (id >= componentBitset.length) {
            int newCapacity = componentBitset.length * 2;
            componentBitset = java.util.Arrays.copyOf(componentBitset, newCapacity);
            entityVersions = java.util.Arrays.copyOf(entityVersions, newCapacity);
            isAlive = java.util.Arrays.copyOf(isAlive, newCapacity); // right here!
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

    public int getEntityMax() {
        return entityMax;
    }

    int getComponentIndex(Class<? extends Component> component) {
        return componentRegistry.get(component);
    }

    public void addSystem(EntitySystem<T> system, int priority) {
        systems.computeIfAbsent(priority, k -> new ArrayList<>()).add(system);
        system.setEngine(this);
    }

    public void addSystem(EntitySystem<T> system) {
        addSystem(system, 0);
    }

    public void update(T ctx) {
        var sortedSystems = systems.entrySet().stream().sorted((a, b) -> Integer.compare(a.getKey(), b.getKey())).toList();
        for (var systemGroup : sortedSystems) {
            for (var system : systemGroup.getValue()) {
                if (system.isEnabled()) {
                    system.update(ctx);
                }
            }
        }
    }

    public void clearEntities() {
        archetypes.clear();
        for (int i = 0; i < entityMax; i++) {
            if (componentBitset[i] != 0L) {
                destroyEntity(i);
            }
        }
    }

    Map<Long, List<Integer>> getArchetypes() {
        return archetypes;
    }

    public void removeSystems(int priority) {
        systems.remove(priority);
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
    public Class<? extends Component> getComponentClass(int componentId) {
        return componentRegistry.index(componentId);
    }

    public Iterable<Class<? extends Component>> getComponentClasses() {
        return componentRegistry.getComponentClasses();
    }


    @SafeVarargs
    public final Stream<Integer> getFamily(Class<? extends Component>... types) {
        long familyMask = 0;
        for (Class<? extends Component> type : types) {
            familyMask |= 1L << getComponentIndex(type);
        }

        final long finalMask = familyMask;
        List<Integer> result = new ArrayList<>();
        for (var entry : archetypes.entrySet()) {
            if ((entry.getKey() & finalMask) == finalMask) {
                result.addAll(entry.getValue());
            }
        }
        return result.stream();
    }
}
