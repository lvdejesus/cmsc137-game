package client.systems.client;

import client.components.NetworkIdComponent;
import client.network.NetworkSpawnManager;
import framework.engine.ComponentMapper;
import framework.engine.Engine;
import framework.engine.IteratingEntitySystem;
import client.components.bullet.BulletComponent;

import java.util.logging.Level;
import java.util.logging.Logger;

public class BulletSystem extends IteratingEntitySystem<Context> {
    private ComponentMapper<BulletComponent> bm;
    private ComponentMapper<NetworkIdComponent> nicm;

    private NetworkSpawnManager nsm;

    public BulletSystem(NetworkSpawnManager nsm) {
        super(BulletComponent.class, NetworkIdComponent.class);

        this.nsm = nsm;
    }

    @Override
    public void setEngine(Engine<Context> engine) {
        super.setEngine(engine);
        this.engine = engine;
        this.bm = engine.getMapper(BulletComponent.class);
        this.nicm = engine.getMapper(NetworkIdComponent.class);
    }

    @Override
    public void processEntity(int id, Context ctx) {
        BulletComponent bullet = bm.get(id);

        bullet.age += ctx.deltaTime;

        if (bullet.age >= bullet.lifetime) {
            nsm.despawn(id);
        }
    }
}