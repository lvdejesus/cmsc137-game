package client.components;

import framework.engine.SyncComponent;

import java.nio.ByteBuffer;

public class PlayerKeysComponent implements SyncComponent {
    public int lastKeyCount = -1;
    public int keyCount = 0;
    public static final int MAX_KEYS = 3;

    public PlayerKeysComponent() {
    }

    public PlayerKeysComponent(int keyCount) {
        this.keyCount = keyCount;
    }

    public void addKey() {
        if (keyCount < MAX_KEYS) {
            keyCount++;
        }
    }

    public int consumeKeys(int count) {
        int toConsume = Math.min(count, keyCount);
        keyCount -= toConsume;
        return toConsume;
    }

    public int getKeys() {
        return keyCount;
    }

    @Override
    public void fromBytes(byte[] bytes) {
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        keyCount = buf.getInt();
    }

    @Override
    public byte[] toBytes() {
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.putInt(keyCount);
        return buf.array();
    }

    @Override
    public boolean isEqual(SyncComponent other) {
        if (!(other instanceof PlayerKeysComponent c)) return false;
        return c.keyCount == keyCount;
    }

    @Override
    public void copyFrom(SyncComponent other) {
        if (!(other instanceof PlayerKeysComponent c)) return;
        keyCount = c.keyCount;
    }

    @Override
    public SyncComponent clone() {
        return new PlayerKeysComponent(keyCount);
    }
}