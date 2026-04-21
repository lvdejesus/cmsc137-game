package client.rendering;

public class Texture {
    public float u1, v1, u2, v2;

    // Original size
    public int width, height;

    public Texture(float u1, float v1, float u2, float v2, int w, int h) {
        this.u1 = u1;
        this.v1 = v1;
        this.u2 = u2;
        this.v2 = v2;
        this.width = w;
        this.height = h;
    }
}
