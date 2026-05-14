package client.components;

import client.rendering.Anchor;
import framework.engine.SyncComponent;
import org.joml.Vector2f;

import framework.engine.Component;

import java.nio.ByteBuffer;

public class TransformComponent implements SyncComponent {
    public Vector2f position;
    public Vector2f scale;
    public Anchor anchor;
    public float rotation = 0.0f;

    public TransformComponent(Vector2f position, Vector2f scale, Anchor anchor) {
        this.position = position;
        this.scale = scale;
        this.anchor = anchor;
    }

    public TransformComponent(Vector2f position, Vector2f scale) {
        this(position, scale, Anchor.CENTER);
    }

    public TransformComponent(Vector2f position) {
        this(position, new Vector2f(1.0f, 1.0f));
    }

    @Override
    public void fromBytes(byte[] bytes) {
        ByteBuffer buf = ByteBuffer.wrap(bytes);

        position.x = buf.getFloat();
        position.y = buf.getFloat();
        rotation = buf.getFloat();
    }

    @Override
    public byte[] toBytes() {
        ByteBuffer buf = ByteBuffer.allocate(12);

        buf.putFloat(position.x);
        buf.putFloat(position.y);
        buf.putFloat(rotation);

        return buf.array();
    }
}
