package client.components;

import framework.engine.SyncComponent;

import java.nio.ByteBuffer;

public class HealthComponent implements SyncComponent {
    public float currentHealth = 5;
    public float maxHealth = 5;

    public HealthComponent(float currentHealth, float maxHealth) {
        this.currentHealth = maxHealth;
        this.maxHealth = currentHealth;
    }

    public HealthComponent(float maxHealth) {
        this.currentHealth = maxHealth;
        this.maxHealth = maxHealth;
    }

    public boolean isAlive() {
        return currentHealth > 0;
    }

    public void damage(float amount) {
        currentHealth -= amount;
        if (currentHealth < 0) {
            currentHealth = 0;
        }
    }

    public float getHealth() {
        return currentHealth;
    }

    @Override
    public void fromBytes(byte[] bytes) {
        ByteBuffer buf = ByteBuffer.wrap(bytes);

        currentHealth = buf.getFloat();
        maxHealth = buf.getFloat();
    }

    @Override
    public byte[] toBytes() {
        ByteBuffer buf = ByteBuffer.allocate(8);

        buf.putFloat(currentHealth);
        buf.putFloat(maxHealth);

        return buf.array();
    }

    @Override
    public boolean isEqual(SyncComponent other) {
        if (!(other instanceof HealthComponent c)) return false;

        return c.currentHealth == currentHealth && c.maxHealth == maxHealth;
    }

    @Override
    public void copyFrom(SyncComponent other) {
        if (!(other instanceof HealthComponent c)) return;

        maxHealth = c.maxHealth;
        currentHealth = c.currentHealth;
    }

    @Override
    public SyncComponent clone() {
        return new HealthComponent(currentHealth, maxHealth);
    }
}