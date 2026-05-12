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
    public void syncFromBytes(byte[] bytes) {
        PlayerStateComponent.Sync sync = PlayerStateComponent.Sync.fromBytes(bytes);
        sync.apply(this);
    }

    public static class Sync {
        private final State previous;
        private final State current;

        public Sync(State previous, State current) {
            this.previous = previous;
            this.current = current;
        }

        public void apply(PlayerStateComponent sc) {
            sc.previous = previous;
            sc.current = current;
        }

        public byte[] toBytes() {
            ByteBuffer buf = ByteBuffer.allocate(8);

            buf.putInt(previous.ordinal());
            buf.putInt(current.ordinal());

            return buf.array();
        }

        public static PlayerStateComponent.Sync fromBytes(byte[] bytes) {
            ByteBuffer buf = ByteBuffer.wrap(bytes);

            State x = State.values()[buf.getInt()];
            State y = State.values()[buf.getInt()];

            return new PlayerStateComponent.Sync(x, y);
        }

        public static PlayerStateComponent.Sync extract(PlayerStateComponent sc) {
            return new PlayerStateComponent.Sync(sc.previous, sc.current);
        }
    }
}