package client.entities;

import client.systems.client.Context;
import framework.engine.Engine;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;

public class PrefabRegistry {
    private final Map<Integer, PrefabDeserializer> deserializers = new LinkedHashMap<>();
    private final Map<Class<? extends Prefab>, Integer> classes = new LinkedHashMap<>();
    private int currentId = 0;

    protected void register(Class<? extends Prefab> clazz, PrefabDeserializer deserializer) {
        int id = currentId++;
        classes.put(clazz, id);
        deserializers.put(id, deserializer);
    }

    public Prefab spawn(Engine<Context> engine, int prefabId, int networkId, byte[] bytes) {
        try {
            PrefabDeserializer d = deserializers.get(prefabId);
            if (d == null) throw new IOException("Unknown prefab type: " + prefabId);
            Prefab prefab = d.deserialize(engine, networkId, ByteBuffer.wrap(bytes));
            prefab.spawn();

            return prefab;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Prefab spawnServer(Engine<Context> engine, int prefabId, int networkId, byte[] bytes) {
        try {
            PrefabDeserializer d = deserializers.get(prefabId);
            if (d == null) throw new IOException("Unknown prefab type: " + prefabId);
            Prefab prefab = d.deserialize(engine, networkId, ByteBuffer.wrap(bytes));
            prefab.spawnServer();

            return prefab;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Integer get(Class<? extends  Prefab> clazz) {
        return classes.get(clazz);
    }
}