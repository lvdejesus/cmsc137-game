
package client.systems.player;
import client.systems.Context;

import client.components.player.PlayerStateComponent;


import framework.engine.Engine;
import framework.engine.IteratingEntitySystem;
import framework.engine.ComponentMapper;


public class PlayerStateSystem extends IteratingEntitySystem<Context> {
    
    private ComponentMapper<PlayerStateComponent> sm;
    
    @Override
    public void setEngine(Engine<Context> engine){
        super.setEngine(engine);
        this.sm = engine.getMapper(PlayerStateComponent.class);
    }

    @Override
    public void processEntity(int id, Context ctx){
        PlayerStateComponent state = sm.get(id);
        switch (state.current) {
            case IDLE:
                
                break;
        
            case MOVING:
                break;
        }
    }

}
