package client.network.messages.client;

import client.components.player.PlayerStateComponent;
import client.network.messages.Message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class C_PlayerState implements Message {
    private final float x;
    private final float y;
    private final float rotation;
    private final PlayerStateComponent.State previous;
    private final PlayerStateComponent.State current;
    private final float mx;
    private final float my;

    public C_PlayerState(float x, float y, float rotation, PlayerStateComponent.State previous, PlayerStateComponent.State current, float mx, float my) {
        this.x = x;
        this.y = y;
        this.rotation = rotation;
        this.previous = previous;
        this.current = current;
        this.mx = mx;
        this.my = my;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getRotation() {
        return rotation;
    }

    public PlayerStateComponent.State getPrevious() {
        return previous;
    }

    public PlayerStateComponent.State getCurrent() {
        return current;
    }

    public float getMx() {
        return mx;
    }

    public float getMy() {
        return my;
    }

    public void serialize(DataOutputStream out) throws IOException {
        out.writeFloat(x);
        out.writeFloat(y);
        out.writeFloat(rotation);
        out.writeInt(previous.ordinal());
        out.writeInt(current.ordinal());
        out.writeFloat(mx);
        out.writeFloat(my);
    }

    public static C_PlayerState deserialize(DataInputStream in) throws IOException {
        float x = in.readFloat();
        float y = in.readFloat();
        float rotation = in.readFloat();
        PlayerStateComponent.State previous = PlayerStateComponent.State.values()[in.readInt()];
        PlayerStateComponent.State current = PlayerStateComponent.State.values()[in.readInt()];
        float mx = in.readFloat();
        float my = in.readFloat();

        return new C_PlayerState(x, y, rotation, previous, current, mx, my);
    }
}