package game.components;

import framework.engine.Component;
import game.rendering.Animation;

public class AnimationComponent implements Component {
    public Animation animation;
    public float offset;

    public AnimationComponent(Animation animation, float offset) {
        this.animation = animation;
        this.offset = offset;
    }
}
