import static org.lwjgl.opengl.GL33.*;

public class SpriteBatch {
    private final int MAX_SPRITES = 1000;
    private final int VERTICES_PER_SPRITE = 4;
    private final int ELEMENTS_PER_VERTEX = 9; // x,y,z, u,v, r,g,b,a

    private float[] vertexArray = new float[MAX_SPRITES * VERTICES_PER_SPRITE * ELEMENTS_PER_VERTEX];
    private int spriteCount = 0;
    private int vao, vbo;

    static int GLOBAL_SCALE = 2;

    public SpriteBatch() {
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, (long) vertexArray.length * 4, GL_DYNAMIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 9 * 4, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 9 * 4, 3 * 4);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(2, 4, GL_FLOAT, false, 9 * 4, 5 * 4);
        glEnableVertexAttribArray(2);

        setupIndices();
    }

    private void setupIndices() {
        int[] indices = new int[MAX_SPRITES * 6];
        int v = 0;
        for (int i = 0; i < indices.length; i += 6) {
            indices[i + 0] = v + 0;
            indices[i + 1] = v + 1;
            indices[i + 2] = v + 2;
            indices[i + 3] = v + 2;
            indices[i + 4] = v + 3;
            indices[i + 5] = v + 0;
            v += 4;
        }

        int ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
    }

    public void draw(Texture tex, float x, float y, float z, float rot, float sx, float sy, float r, float g, float b,
            float a) {
        if (spriteCount >= MAX_SPRITES)
            flush();

        float cos = (float) Math.cos(Math.toRadians(rot));
        float sin = (float) Math.sin(Math.toRadians(rot));

        // Corner offsets for a centered quad
        float[][] corners = { { -0.5f, 0.5f }, { 0.5f, 0.5f }, { 0.5f, -0.5f }, { -0.5f, -0.5f } };
        float[] uvs = { tex.u1, tex.v1, tex.u2, tex.v1, tex.u2, tex.v2, tex.u1, tex.v2 };

        int offset = spriteCount * VERTICES_PER_SPRITE * ELEMENTS_PER_VERTEX;
        for (int i = 0; i < 4; i++) {
            float px = corners[i][0] * sx * GLOBAL_SCALE;
            float py = corners[i][1] * sy * GLOBAL_SCALE;

            vertexArray[offset++] = (px * cos - py * sin) + x;
            vertexArray[offset++] = (px * sin + py * cos) + y;
            vertexArray[offset++] = z;

            vertexArray[offset++] = uvs[i * 2];
            vertexArray[offset++] = uvs[i * 2 + 1];

            vertexArray[offset++] = r;
            vertexArray[offset++] = g;
            vertexArray[offset++] = b;
            vertexArray[offset++] = a;
        }
        spriteCount++;
    }

    public void flush() {
        if (spriteCount == 0)
            return;
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertexArray);

        glBindVertexArray(vao);
        glDrawElements(GL_TRIANGLES, spriteCount * 6, GL_UNSIGNED_INT, 0);
        spriteCount = 0;
    }
}
