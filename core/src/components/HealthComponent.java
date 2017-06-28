package components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by Miv on 5/23/2017.
 */
public class HealthComponent implements Component, Pool.Poolable {
    private float health;
    private float maxHealth;

    public HealthComponent() {

    }

    public HealthComponent(int maxHealth) {
        health = maxHealth;
        this.maxHealth = maxHealth;
    }

    @Override
    public void reset() {
        health = 0;
        maxHealth = 0;
    }

    public float getHealth() {
        return health;
    }

    public void setHealth(float health) {
        this.health = health;
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(float maxHealth) {
        this.maxHealth = maxHealth;
    }
}
