package components;

import framework.engine.Component;
import framework.rendering.Texture;

public class RenderComponent implements Component {
    public Texture texture;
    public int z;

    public RenderComponent(Texture texture, int z) {
        this.texture = texture;
        this.z = z;
    }

    public RenderComponent(Texture texture) {
        this(texture, 0);
    }

    public RenderComponent() {
        this(null);
    }
}
