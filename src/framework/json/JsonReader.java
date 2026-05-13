package framework.json;

import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.util.Optional;

public class JsonReader implements AutoCloseable {
    private final PushbackReader stream;
    private boolean pendingValue = false;

    public JsonReader(String filename) throws IOException {
        this.stream = new PushbackReader(new FileReader(filename), 1);
    }

    private int peekChar() throws IOException {
        int c = stream.read();
        if (c != -1) stream.unread(c);
        return c;
    }

    private void skipWhitespace() throws IOException {
        int c;
        while ((c = peekChar()) != -1 && Character.isWhitespace(c)) {
            stream.read();
        }
    }

    public Optional<JsonPair> getPair() throws IOException {
        if (pendingValue) {
            skipValue();
            pendingValue = false;
        }

        skipWhitespace();
        int lookahead = peekChar();

        if (lookahead == '{' || lookahead == ',') {
            stream.read();
            skipWhitespace();
            lookahead = peekChar();
        }

        if (lookahead == '}') {
            stream.read();
            pendingValue = false;
            return Optional.empty();
        }

        if (lookahead != '"') {
            throw new IOException("Expected string key, got '" + (char) lookahead + "'");
        }

        stream.read();
        StringBuilder keyBuilder = new StringBuilder();
        int c;
        while ((c = stream.read()) != -1 && c != '"') {
            keyBuilder.append((char) c);
        }
        String key = keyBuilder.toString();

        skipWhitespace();
        if (stream.read() != ':') {
            throw new IOException("Missing colon after key: " + key);
        }

        pendingValue = true;
        return Optional.of(new JsonPair(key, new JsonValue(this)));
    }

    protected Optional<JsonPair> consumeNestedPair() throws IOException {
        if (pendingValue) {
            skipWhitespace();
            int c = peekChar();
            if (c == '{') {
                stream.read();
                pendingValue = false;
            } else {
                throw new IOException("Called getPair() on a value that isn't an object!");
            }
        }
        return getPair();
    }

    protected String consumeStringValue() throws IOException {
        if (!pendingValue) throw new IOException("Value already consumed!");
        pendingValue = false;
        skipWhitespace();
        if (peekChar() != '"') return null;

        stream.read();
        StringBuilder sb = new StringBuilder();
        int c;
        while ((c = stream.read()) != -1 && c != '"') {
            sb.append((char) c);
        }
        return sb.toString();
    }

    protected int consumeIntValue() throws IOException {
        if (!pendingValue) throw new IOException("Value already consumed!");
        pendingValue = false;
        skipWhitespace();

        StringBuilder sb = new StringBuilder();
        int c;
        while ((c = peekChar()) != -1 && (Character.isDigit(c) || c == '-')) {
            sb.append((char) stream.read());
        }
        return Integer.parseInt(sb.toString());
    }

    public Optional<JsonValue> getArrayItem() throws IOException {
        if (pendingValue) {
            skipValue();
            pendingValue = false;
        }

        skipWhitespace();
        int lookahead = peekChar();

        if (lookahead == ',') {
            stream.read();
            skipWhitespace();
            lookahead = peekChar();
        }

        if (lookahead == ']') {
            stream.read();
            pendingValue = false;
            return Optional.empty();
        }

        pendingValue = true;
        return Optional.of(new JsonValue(this));
    }

    protected JsonArray consumeArray() throws IOException {
        if (!pendingValue) {
            throw new IOException("value already consumed!");
        }
        skipWhitespace();
        if (peekChar() != '[') {
            throw new IOException("called getArray() on a value that isn't an array!");
        }
        return new JsonArray(this);
    }

    protected Optional<JsonValue> consumeNestedArrayItem() throws IOException {
        if (pendingValue) {
            skipWhitespace();
            int c = peekChar();
            if (c == '[') {
                stream.read();
                pendingValue = false;
            } else {
                throw new IOException("called getItem() on a value that isn't an array!");
            }
        }
        return getArrayItem();
    }

    private void skipValue() throws IOException {
        skipWhitespace();
        int c = peekChar();

        if (c == '"') {
            consumeStringValue();
        } else if (c == '{' || c == '[') {
            int open = stream.read();
            int close = (open == '{') ? '}' : ']';
            int depth = 1;
            boolean inString = false;
            boolean escape = false;

            do {
                int ch = stream.read();
                if (ch == -1) break;

                if (inString) {
                    if (ch == '\\') escape = !escape;
                    else if (ch == '"' && !escape) inString = false;
                    else escape = false;
                } else {
                    if (ch == '"') inString = true;
                    else if (ch == open) depth++;
                    else if (ch == close) depth--;
                }
            } while (depth > 0);
        } else {
            while ((c = peekChar()) != -1 && c != ',' && c != '}' && c != ']' && !Character.isWhitespace(c)) {
                stream.read();
            }
        }
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }
}