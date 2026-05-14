package client.entities;

import client.systems.client.Context;
import framework.engine.Engine;
import framework.engine.Entity;

public abstract class Prefab {
    protected final Entity<Context> entity;

    protected Prefab(Engine<Context> engine) {
        this.entity = engine.createEntity();
    }

    public Entity<Context> getEntity() {
        return entity;
    }

    public void spawnCommon() {
    }

    public final void spawnClient() {
        spawnCommon();
        spawnClientInternal();
    }

    public void spawnClientInternal() {
    }

    public final void spawnServer() {
        spawnCommon();
        spawnServerInternal();
    }

    public void spawnServerInternal() {
    }
}
