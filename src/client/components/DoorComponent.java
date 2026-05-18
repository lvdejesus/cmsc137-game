package client.components;

import client.rendering.Texture;
import framework.engine.SyncComponent;

import java.nio.ByteBuffer;

public class DoorComponent implements SyncComponent {
    public enum State { CLOSED, OPENING, OPEN }
    public State state = State.CLOSED;
    public float animTimer = 0;
    public int gridX, gridY;
    public int tileIndex;
    public Texture[] frames;

    public DoorComponent() {}

    public DoorComponent(int gridX, int gridY, int tileIndex) {
        this.gridX = gridX;
        this.gridY = gridY;
        this.tileIndex = tileIndex;
    }

    @Override
    public boolean isEqual(SyncComponent other) {
        return other instanceof DoorComponent c && state == c.state;
    }

    @Override
    public void copyFrom(SyncComponent other) {
        if (!(other instanceof DoorComponent c)) return;
        state = c.state;
        animTimer = 0;
    }

    @Override
    public SyncComponent clone() {
        DoorComponent c = new DoorComponent();
        c.state = state;
        c.gridX = gridX;
        c.gridY = gridY;
        c.tileIndex = tileIndex;
        return c;
    }

    @Override
    public byte[] toBytes() {
        return new byte[] { (byte) state.ordinal() };
    }

    @Override
    public void fromBytes(byte[] bytes) {
        state = State.values()[bytes[0]];
        animTimer = 0;
    }
}
