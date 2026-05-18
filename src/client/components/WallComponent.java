package client.components;

import framework.engine.SyncComponent;

import java.nio.ByteBuffer;

public class WallComponent implements SyncComponent {
    private boolean active = true;
    public boolean isStatic;

    public WallComponent() {}

    public WallComponent(boolean isStatic) {
        this.isStatic = isStatic;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean isEqual(SyncComponent other) {
        return other instanceof WallComponent w && active == w.active && isStatic == w.isStatic;
    }

    @Override
    public void copyFrom(SyncComponent other) {
        if (!(other instanceof WallComponent w)) return;
        active = w.active;
        isStatic = w.isStatic;
    }

    @Override
    public SyncComponent clone() {
        WallComponent c = new WallComponent();
        c.active = active;
        c.isStatic = isStatic;
        return c;
    }

    @Override
    public byte[] toBytes() {
        ByteBuffer buf = ByteBuffer.allocate(2);
        buf.put((byte) (active ? 1 : 0));
        buf.put((byte) (isStatic ? 1 : 0));
        return buf.array();
    }

    @Override
    public void fromBytes(byte[] bytes) {
        active = bytes[0] != 0;
        isStatic = bytes[1] != 0;
    }
}
