package client.components;

import framework.engine.Component;
import client.rendering.Animation;

public class AnimationComponent implements Component {
    public Animation animation;
    public float offset;

    public AnimationComponent(Animation animation, float offset) {
        this.animation = animation;
        this.offset = offset;
    }
}
