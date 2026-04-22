package game.components;

import framework.engine.Component;
import org.joml.primitives.AABBf;


public class ClickableComponent implements Component {
    public AABBf boundingBox;

    public ClickableComponent(AABBf boundingBox) {
        this.boundingBox = boundingBox;
    }
}
