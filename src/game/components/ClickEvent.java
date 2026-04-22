package game.components;

import framework.engine.Component;

public class ClickEvent implements Component {
    public float x;
    public float y;

    public ClickEvent(float x, float y) {
        this.x = x;
        this.y= y;
    }
}
