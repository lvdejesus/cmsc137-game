package client.components.player;
import framework.engine.Component;

// Used to mark a entity as being player controlled
public class PlayerTagComponent implements Component{
    public double shootTimer = 0.0f;
    public float fireRate = 10.0f;
}
