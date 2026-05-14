package client.components.player;

import client.components.MovementComponent;
import client.components.TransformComponent;
import framework.engine.SyncComponent;
import org.joml.Vector2f;

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

    @Override
    public boolean isEqual(SyncComponent other) {
        if (!(other instanceof MovementInputComponent c)) return false;

        return c.x == x && c.y == y;
    }

    @Override
    public void copyFrom(SyncComponent other) {
        if (!(other instanceof MovementInputComponent c)) return;

        this.x = c.x;
        this.y = c.y;
    }

    @Override
    public SyncComponent clone() {
        return new MovementInputComponent(x, y);
    }
}
