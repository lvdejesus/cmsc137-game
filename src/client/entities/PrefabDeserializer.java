package client.entities;

import client.systems.client.Context;
import framework.engine.Engine;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

@FunctionalInterface
public interface PrefabDeserializer {
    Prefab deserialize(Engine<Context> engine, int networkId, ByteBuffer bytes) throws IOException;
}
