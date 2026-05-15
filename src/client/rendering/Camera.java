package client.rendering;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;

public class Camera {
    private Matrix4f projectionMatrix;
    private Matrix4f viewMatrix;
    public Vector2f position;
    public float rotation;

    // Screen-space viewport (where this camera renders on screen)
    public float viewportX;
    public float viewportY;
    public float viewportWidth;
    public float viewportHeight;

    // World-space projection dimensions (how much world is visible)
    public float worldWidth;
    public float worldHeight;
    public float scale;

    public String name;

    public float getWidth() {
        return worldWidth;
    }

    public float getHeight() {
        return worldHeight;
    }

    public Camera() {
        this("default");
    }

    public Camera(String name) {
        this(name, 1.0f);
    }

    public Camera(String name, float scale) {
        this.name = name;
        this.position = new Vector2f(0, 0);
        this.rotation = 0.0f;
        this.viewportX = 0;
        this.viewportY = 0;
        this.viewportWidth = 1280;
        this.viewportHeight = 720;
        this.worldWidth = this.viewportWidth * scale;
        this.worldHeight = this.viewportHeight * scale;
        this.viewMatrix = new Matrix4f();
        this.scale = scale;

        updateProjection();
    }


    public void setSize(int width, int height) {
        this.viewportWidth = width;
        this.viewportHeight = height;
        this.worldWidth = this.viewportWidth * scale;
        this.worldHeight = this.viewportHeight * scale;
        updateProjection();
        this.viewMatrix = new Matrix4f();
    }

    public void setViewport(float x, float y, float width, float height) {
        this.viewportX = x;
        this.viewportY = y;
        this.viewportWidth = width;
        this.viewportHeight = height;
    }

    public void setWorldSize(float width, float height) {
        this.worldWidth = width * scale;
        this.worldHeight = height * scale;
        updateProjection();
    }

    private void updateProjection() {
        this.projectionMatrix = new Matrix4f().ortho(0, worldWidth, worldHeight, 0, -10, 10);
    }

    public Matrix4f getProjectionViewMatrix() {
        return new Matrix4f(projectionMatrix).mul(
            viewMatrix.identity().translate(-position.x, -position.y, 0)).rotateZ((float) Math.toRadians(rotation));
    }

    public void bind(int shaderProgram) {
        int pvLoc = glGetUniformLocation(shaderProgram, "u_ProjectionView");
        float[] pvArr = new float[16];
        this.getProjectionViewMatrix().get(pvArr);
        glUniformMatrix4fv(pvLoc, false, pvArr);
    }

    public boolean containsScreenPoint(float screenX, float screenY) {
        return screenX >= viewportX && screenX < viewportX + viewportWidth &&
            screenY >= viewportY && screenY < viewportY + viewportHeight;
    }

    // Adjust position relative to camera viewport
    public Vector2f toWorldPosition(Vector2f position) {
        float adjustedX = position.x - viewportX;
        float adjustedY = viewportHeight - (position.y - viewportY);

        int[] viewport = {0, 0, (int) viewportWidth, (int) viewportHeight};
        Vector3f worldSpace = new Vector3f();

        Vector3f result = getProjectionViewMatrix().unproject(
            adjustedX,
            adjustedY,
            0.0f,
            viewport,
            worldSpace
        );

        return new Vector2f(result.x, result.y);
    }
}
