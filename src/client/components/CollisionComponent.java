package client.components;

import framework.engine.Component;
import org.joml.primitives.AABBf;

public class CollisionComponent implements Component {
    public AABBf boundingBox;
    
    public CollisionComponent(AABBf boundingBox) {
        this.boundingBox = boundingBox;
    }
}