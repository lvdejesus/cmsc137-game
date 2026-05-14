package client.components;

import framework.engine.Component;
import framework.engine.SyncComponent;

import java.util.HashMap;
import java.util.Map;

public class NetworkDuplicateComponent implements Component {
    public Map<Class<? extends Component>, SyncComponent> components = new HashMap<>();

    @SafeVarargs
    public NetworkDuplicateComponent(Class<? extends Component>... clazzes) {
        for (var clazz : clazzes) {
            components.put(clazz, null);
        }
    }
}
