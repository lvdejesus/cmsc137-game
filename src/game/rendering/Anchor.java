package game.rendering;

public enum Anchor {
    TOP_LEFT(0.0f, 0.0f),
    TOP(0.5f, 0.0f),
    TOP_RIGHT(1.0f, 0.0f),

    LEFT(0.0f, 0.5f),
    CENTER(0.5f, 0.5f),
    RIGHT(1.0f, 0.5f),

    BOTTOM_LEFT(0.0f, 1.0f),
    BOTTOM(0.5f, 1.0f),
    BOTTOM_RIGHT(1.0f, 1.0f);

    private final float xOffset;
    private final float yOffset;

    Anchor(float xOffset, float yOffset) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
    }

    public float getXOffset() {
        return xOffset;
    }

    public float getYOffset() {
        return yOffset;
    }
}