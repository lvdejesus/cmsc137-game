package client.components;

import framework.engine.Component;
import org.joml.Vector2f;

public class BossComponent implements Component {
    public static State[] roots = {State.Lingering, State.Pulse};
    public enum State {
        Lingering,
        Pulse,
        PulseMove,
        PulseHit,
        PulseRotate,
    }

    public State state = State.Pulse;
    public double stateChangeTimer = 0.0f;
    public double stateChangeInterval = 10.0f;

    public double secondaryTimer = 0.0f;
    public int direction = 0;
    public Vector2f target;

    public float cx, cy;

    public BossComponent(float cx, float cy) {
        this.cx = cx;
        this.cy = cy;
    }
}
