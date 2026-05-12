package client.entities;

import client.systems.client.Context;
import framework.engine.Engine;
import framework.engine.Entity;

public class Prefab {
    protected final Entity<Context> entity;

    protected Prefab(Engine<Context> engine) {
        this.entity = engine.createEntity();
    }

    public Entity<Context> getEntity() {
        return entity;
    }
}
