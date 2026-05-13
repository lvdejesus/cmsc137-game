package editor.components;

import framework.engine.Component;

public class ButtonComponent implements Component {
    public String action;

    public ButtonComponent(String action) {
        this.action = action;
    }
}