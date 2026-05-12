package client.components.player;

import framework.engine.SyncComponent;

import java.nio.ByteBuffer;

public class MovementInputComponent implements SyncComponent {
    public float x;
    public float y;

    public MovementInputComponent() {
        this(0.0f, 0.0f);
    }

    public MovementInputComponent(float x, float y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void syncFromBytes(byte[] bytes) {
        MovementInputComponent.Sync sync = MovementInputComponent.Sync.fromBytes(bytes);
        sync.apply(this);
    }

    public static class Sync {
        private final float x;
        private final float y;

        public Sync(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public void apply(MovementInputComponent mic) {
            mic.x = x;
            mic.y = y;
        }

        public byte[] toBytes() {
            ByteBuffer buf = ByteBuffer.allocate(8);

            buf.putFloat(x);
            buf.putFloat(y);

            return buf.array();
        }

        public static MovementInputComponent.Sync fromBytes(byte[] bytes) {
            ByteBuffer buf = ByteBuffer.wrap(bytes);

            float x = buf.getFloat();
            float y = buf.getFloat();

            return new MovementInputComponent.Sync(x, y);
        }

        public static MovementInputComponent.Sync extract(MovementInputComponent mic) {
            return new MovementInputComponent.Sync(mic.x, mic.y);
        }
    }
}
