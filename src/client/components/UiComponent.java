package client.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.joml.Vector2f;
import org.joml.Vector4f;

import framework.engine.Component;

public class UiComponent implements Component {
    
    public List<String> states = new ArrayList<>();
    public int currentState = 0;
    public int targetState =0;

    public String shaderVert = "res/shaders/default.vert";
    public String shaderFrag = "res/shaders/default.frag";
    
    // For tweening
    public final Vector2f targetPosistion = new Vector2f();
    public float targetRotation = 0f;
    public float lerpSpeed = 10f;
    public Vector4f targetTint = new Vector4f(1,1,1,1);

    // Interface for custom functionality on state reached per ui element
    public interface UiStateListener {
        void onStateChanged(int idx, String state);
    }

    public UiStateListener onStateChanged;

    public UiComponent(String... states){
        if (states !=null){
            Collections.addAll(this.states,states);
        }
        if (this.states.isEmpty()) this.states.add("default");
    }
}
