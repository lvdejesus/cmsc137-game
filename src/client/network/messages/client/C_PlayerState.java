package client.network.messages.client;

import client.components.player.PlayerStateComponent;
import client.network.messages.Message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class C_PlayerState implements Message {
    public final float x;
    public final float y;
    public final float rotation;
    public final PlayerStateComponent.State previous;
    public final PlayerStateComponent.State current;
    public final float mx;
    public final float my;
    public final float vx;
    public final float vy;

    public C_PlayerState(float x, float y, float rotation, PlayerStateComponent.State previous, PlayerStateComponent.State current, float mx, float my, float vx, float vy) {
        this.x = x;
        this.y = y;
        this.rotation = rotation;
        this.previous = previous;
        this.current = current;
        this.mx = mx;
        this.my = my;
        this.vx = vx;
        this.vy = vy;
    }

    public void serialize(DataOutputStream out) throws IOException {
        out.writeFloat(x);
        out.writeFloat(y);
        out.writeFloat(rotation);
        out.writeInt(previous.ordinal());
        out.writeInt(current.ordinal());
        out.writeFloat(mx);
        out.writeFloat(my);
        out.writeFloat(vx);
        out.writeFloat(vy);
    }

    public static C_PlayerState deserialize(DataInputStream in) throws IOException {
        float x = in.readFloat();
        float y = in.readFloat();
        float rotation = in.readFloat();
        PlayerStateComponent.State previous = PlayerStateComponent.State.values()[in.readInt()];
        PlayerStateComponent.State current = PlayerStateComponent.State.values()[in.readInt()];
        float mx = in.readFloat();
        float my = in.readFloat();
        float vx = in.readFloat();
        float vy = in.readFloat();

        return new C_PlayerState(x, y, rotation, previous, current, mx, my, vx, vy);
    }
}