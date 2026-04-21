package client.rendering;

import org.joml.Matrix4f;
import org.joml.Vector2f;

import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;

public class Camera {
    private Matrix4f projectionMatrix;
    private Matrix4f viewMatrix;
    public Vector2f position;
    public float rotation;
    public int width;
    public int height;

    public Camera() {
        this.position = new Vector2f(0, 0);
        this.rotation = 0.0f;
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
        this.projectionMatrix = new Matrix4f().ortho(0, width, height, 0, -1, 1);
        this.viewMatrix = new Matrix4f();
    }

    public Matrix4f getProjectionViewMatrix() {
        return new Matrix4f(projectionMatrix).mul(
                viewMatrix.identity()
                        .rotateZ((float) Math.toRadians(rotation))
                        .translate(-position.x, -position.y, 0));
    }

    public void bind(int shaderProgram) {
        int pvLoc = glGetUniformLocation(shaderProgram, "u_ProjectionView");
        float[] pvArr = new float[16];
        this.getProjectionViewMatrix().get(pvArr);
        glUniformMatrix4fv(pvLoc, false, pvArr);
    }
}
