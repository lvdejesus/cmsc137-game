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
    public void fromBytes(byte[] bytes) {
        ByteBuffer buf = ByteBuffer.wrap(bytes);

        x = buf.getFloat();
        y = buf.getFloat();
    }

    @Override
    public byte[] toBytes() {
        ByteBuffer buf = ByteBuffer.allocate(12);

        buf.putFloat(x);
        buf.putFloat(y);

        return buf.array();
    }
}
