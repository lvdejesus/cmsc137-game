package client.systems.client;

import framework.engine.IteratingEntitySystem;
import framework.engine.ComponentMapper;
import framework.engine.Engine;
import client.components.TimerComponent;

public class TimerSystem  extends IteratingEntitySystem<Context>{
    private ComponentMapper<TimerComponent> tm;

    public TimerSystem() {
        super(TimerComponent.class);
    }
    @Override
    public void setEngine(Engine<Context> engine){
        super.setEngine(engine);
        tm = engine.getMapper(TimerComponent.class);    
    }

    @Override
    protected void processEntity(int id, Context ctx) {
        TimerComponent timer = tm.get(id);
        
        // If triggered do nothing
        if(timer.triggered) return;

        //
        timer.elapsed += ctx.deltaTime;

        if(timer.elapsed>= timer.duration){
            timer.triggered = true;
            // Execute function
            if(timer.function != null){
                timer.function.run();
            }
            // Automatically remove this component after use
            engine.removeComponent(id,TimerComponent.class);
        }
    }    
    
}
