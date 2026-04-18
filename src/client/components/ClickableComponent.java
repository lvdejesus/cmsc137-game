package client.components;

import framework.engine.Component;
import org.joml.primitives.AABBf;


public class ClickableComponent implements Component {
    public AABBf boundingBox;
    public ClickHandler onClick;

    public ClickableComponent(AABBf boundingBox, ClickHandler onClick) {
        this.boundingBox = boundingBox;
        this.onClick = onClick;
    }

    @FunctionalInterface
    public interface ClickHandler {
        void onClick(float x, float y);
    }
}
