package client.network.messages;

import java.io.DataInputStream;
import java.io.IOException;

@FunctionalInterface
public interface Deserializer {
    Message deserialize(DataInputStream in) throws IOException;
}
