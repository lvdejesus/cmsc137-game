package client.components;

import framework.engine.Component;
import org.joml.primitives.AABBf;


public class ClickableComponent implements Component {
    public AABBf boundingBox;
    public float z;

    public ClickableComponent(AABBf boundingBox) {
        this(boundingBox, 0.0f);
    }

    public ClickableComponent(AABBf boundingBox, float z) {
        this.boundingBox = boundingBox;
        this.z = z;
    }
}
