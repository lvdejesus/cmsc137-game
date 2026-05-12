package client.components;

import framework.engine.Component;
import client.rendering.Animation;

public class AnimationComponent implements Component {
    public Animation animation;
    public float offset;
    public int startFrame = 0;
    public int endFrame = -1;
    public boolean loop = true;

    public AnimationComponent(Animation animation, float offset, boolean loop) {
        this.animation = animation;
        this.offset = offset;
        this.endFrame = animation.frames.length - 1;
        this.loop = loop;
    }

    public AnimationComponent(Animation animation, float offset, int startFrame, boolean loop) {
        this.animation = animation;
        this.offset = offset;
        this.startFrame = startFrame;
        this.endFrame = animation.frames.length - 1;
        this.loop = loop;
    }

    public AnimationComponent(Animation animation, float offset, int startFrame, int endFrame, boolean loop) {
        this.animation = animation;
        this.offset = offset;
        this.startFrame = startFrame;
        this.endFrame = endFrame;
        this.loop = loop;
    }
}