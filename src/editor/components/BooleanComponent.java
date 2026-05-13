package editor.components;

import framework.engine.Component;

public class BooleanComponent implements Component {
    public boolean value;

    public BooleanComponent(boolean value) {
        this.value = value;
    }
}