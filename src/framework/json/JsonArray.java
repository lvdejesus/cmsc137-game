package framework.json;

import java.io.IOException;
import java.util.Optional;

public class JsonArray {
    private final JsonReader core;

    public JsonArray(JsonReader core) {
        this.core = core;
    }

    public Optional<JsonValue> getItem() throws IOException {
        return core.consumeNestedArrayItem();
    }
}
