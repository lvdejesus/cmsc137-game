package framework.json;

import java.io.IOException;
import java.util.Optional;

public class JsonValue {
    private final JsonReader core;

    public JsonValue(JsonReader core) {
        this.core = core;
    }

    public Optional<JsonPair> getPair() throws IOException {
        return core.consumeNestedPair();
    }

    public String getString() throws IOException {
        return core.consumeStringValue();
    }

    public int getInt() throws IOException {
        return core.consumeIntValue();
    }

    public JsonArray getArray() throws IOException {
        return core.consumeArray();
    }
}