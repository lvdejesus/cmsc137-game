package framework.engine;

public class ComponentMapper<T extends Component> {
    private final int index;
    private Object[] data;

    ComponentMapper(int index, int initialCapacity) {
        this.index = index;
        data = new Object[initialCapacity];
    }

    void resize(int capacity) {
        data = java.util.Arrays.copyOf(data, capacity);
    }

    int getIndex() {
        return index;
    }

    @SuppressWarnings("unchecked")
    public T get(int id) {
        return (T) data[id];
    }

    void set(int id, T component) {
        data[id] = component;
    }
}
