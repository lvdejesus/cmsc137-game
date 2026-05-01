package client.components;

import framework.engine.Component;
import client.rendering.Animation;

public class AnimationComponent implements Component {
    public Animation[] animations;
    public int currentAnim;
    public float offset;

    public AnimationComponent(Animation[] animations, float offset) {
        this.animations = animations;
        this.currentAnim = 0; // Default to down
        this.offset = offset;
    }
}
