package client.components.player;
import framework.engine.Component;

public class PlayerStateComponent implements Component{
    public enum State {
        IDLE,
        MOVING,
        TILTL,
        TILTR,
        HARDTILTL,
        HARDTILTR,
    }

    public State current = State.IDLE;
    public State previous = State.IDLE;

    // Update State
    public void set(State next){
        if(this.current != next){
            this.previous = this.current;
            this.current = next ;
        }
    }
    
    public String get(){
        return this.current.toString();
    }
}