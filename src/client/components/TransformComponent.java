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
    public void syncFromBytes(byte[] bytes) {
        Sync sync = Sync.fromBytes(bytes);
        sync.apply(this);
    }

    public static class Sync {
        private final Vector2f position;
        private final float rotation;

        public Sync(Vector2f position, float rotation) {
            this.position = position;
            this.rotation = rotation;
        }

        public void apply(TransformComponent tc) {
            tc.position.set(position);
            tc.rotation = rotation;
        }

        public byte[] toBytes() {
            ByteBuffer buf = ByteBuffer.allocate(12);

            buf.putFloat(position.x);
            buf.putFloat(position.y);
            buf.putFloat(rotation);

            return buf.array();
        }

        public static Sync fromBytes(byte[] bytes) {
            ByteBuffer buf = ByteBuffer.wrap(bytes);

            float x = buf.getFloat();
            float y = buf.getFloat();
            float rot = buf.getFloat();

            return new Sync(new Vector2f(x, y), rot);
        }

        public static Sync extract(TransformComponent tc) {
            return new Sync(tc.position, tc.rotation);
        }
    }
}
