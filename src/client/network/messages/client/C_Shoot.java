
package client.network.messages.client;

import client.network.messages.Message;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class C_Shoot implements Message {
    private final float px;
    private final float py;

    // Velocity Inheritance
    private final float pvx;
    private final float pvy;

    public C_Shoot(float px, float py, float pvx, float pvy) {
        this.px = px;
        this.py = py;
        this.pvx = pvx;
        this.pvy = pvy;
    }

    public float getPx() { return px; }
    public float getPy() { return py; }
    public float getPvx() { return pvx; }
    public float getPvy() { return pvy; }

    public void serialize(DataOutputStream out) throws IOException {
        out.writeFloat(px);
        out.writeFloat(py);
        out.writeFloat(pvx);
        out.writeFloat(pvy);
    }

    public static C_Shoot deserialize(DataInputStream in) throws IOException {
        float px = in.readFloat();
        float py = in.readFloat();
        float pvx = in.readFloat();
        float pvy = in.readFloat();
        return new C_Shoot(px, py, pvx, pvy);
    }
}