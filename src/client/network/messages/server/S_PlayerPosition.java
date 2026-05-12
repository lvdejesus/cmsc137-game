package client.network.messages.server;

import client.network.messages.Message;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class S_PlayerPosition implements Message {
    private final int senderId;
    private final float x, y, rotation;

    public S_PlayerPosition(int senderId, float x, float y, float rotation) {
        this.senderId = senderId;
        this.x = x;
        this.y = y;
        this.rotation = rotation;
    }

    public int getSenderId() { return senderId; }
    public float getX() { return x; }
    public float getY() { return y; }
    public float getRotation() { return rotation; }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        out.writeInt(senderId);
        out.writeFloat(x);
        out.writeFloat(y);
        out.writeFloat(rotation);
    }

    public static S_PlayerPosition deserialize(DataInputStream in) throws IOException {
        int senderId = in.readInt();
        float x = in.readFloat();
        float y = in.readFloat();
        float rotation = in.readFloat();
        System.out.println("S_PlayerPosition{senderId=" + senderId + ", x=" + x + ", y=" + y + ", rot=" + rotation + "}");
        return new S_PlayerPosition(senderId, x, y, rotation);
    }
}