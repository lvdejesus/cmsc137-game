package client.components.player;

import framework.engine.SyncComponent;

import java.nio.ByteBuffer;

public class PlayerStateComponent implements SyncComponent {
    public enum State {
        IDLE,
        MOVING,
        TILTL,
        TILTR,
        HARDTILTL,
        HARDTILTR,
    }

    public State current = State.IDLE;
    public State previous = State.IDLE;

    // Update State
    public void set(State next) {
        if (this.current != next) {
            this.previous = this.current;
            this.current = next;
        }
    }

    public String get() { return this.current.toString(); }

    @Override
    public void fromBytes(byte[] bytes) {
        ByteBuffer buf = ByteBuffer.wrap(bytes);

        this.previous = State.values()[buf.getInt()];
        this.current = State.values()[buf.getInt()];
    }

    @Override
    public byte[] toBytes() {
        ByteBuffer buf = ByteBuffer.allocate(8);

        buf.putInt(previous.ordinal());
        buf.putInt(current.ordinal());

        return buf.array();
    }
}