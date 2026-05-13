
package client.network.messages.client;

import client.network.messages.Message;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class C_Shoot implements Message {
    private final int senderId;
    private final float px;
    private final float py;
    private final float angle;

    // Velocity Inheritance
    private final float pvx;
    private final float pvy;

    public C_Shoot(int senderId, float px, float py, float angle, float pvx, float pvy) {
        this.senderId = senderId;
        this.px = px;
        this.py = py;
        this.angle = angle;
        this.pvx = pvx;
        this.pvy = pvy;
    }

    public int getSenderId() { return senderId; }
    public float getPx() { return px; }
    public float getPy() { return py; }
    public float getAngle() { return angle; }
    public float getPvx() { return pvx; }
    public float getPvy() { return pvy; }

    public void serialize(DataOutputStream out) throws IOException {
        out.writeInt(senderId);
        out.writeFloat(px);
        out.writeFloat(py);
        out.writeFloat(angle);
        out.writeFloat(pvx);
        out.writeFloat(pvy);
    }

    public static C_Shoot deserialize(DataInputStream in) throws IOException {
        int senderId = in.readInt();
        float px = in.readFloat();
        float py = in.readFloat();
        float angle = in.readFloat();
        float pvx = in.readFloat();
        float pvy = in.readFloat();
        return new C_Shoot(senderId, px, py, angle, pvx, pvy);
    }
}