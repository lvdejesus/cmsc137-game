package client.components;

import framework.engine.Component;

public class HealthComponent implements Component {
    public float maxHealth = 6;
    public float currentHealth = 6;
    
    public HealthComponent() {
    }
    
    public HealthComponent(float maxHealth) {
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
    }
    
    public boolean isAlive() {
        return currentHealth > 0;
    }
    
    public void damage(float amount) {
        currentHealth -= amount;
    }

    public float getHealth() {
        return currentHealth;
    }
}