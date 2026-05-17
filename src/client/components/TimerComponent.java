package client.components;

import framework.engine.Component;

public class TimerComponent implements Component {
    public float duration;
    public float elapsed = 0.0f;
    public boolean triggered = false; // If something triggered after the timer
    public Runnable function; // A generic function  can be used to trigger something when timer is done
    
    public TimerComponent(float duration, Runnable function){
        this.duration = duration;
        this.function = function;
    }
}
