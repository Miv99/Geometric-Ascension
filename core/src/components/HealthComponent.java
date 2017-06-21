package components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by Miv on 5/23/2017.
 */
public class HealthComponent implements Component, Pool.Poolable {
    private int health;
    private int maxHealth;

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

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }
}
