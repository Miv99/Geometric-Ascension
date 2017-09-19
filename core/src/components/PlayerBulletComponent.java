package components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by Miv on 5/23/2017.
 */
public class PlayerBulletComponent implements Component, Pool.Poolable {
    private float damage;
    // For lifesteal
    private Entity entityToBeHealed;
    private float lifestealMultiplier;

    @Override
    public void reset() {
        damage = 0;
        entityToBeHealed = null;
        lifestealMultiplier = 0;
    }

    public float getDamage() {
        return damage;
    }

    public float getLifestealMultiplier() {
        return lifestealMultiplier;
    }

    public Entity getEntityToBeHealed() {
        return entityToBeHealed;
    }

    public PlayerBulletComponent setDamage(float damage) {
        this.damage = damage;
        return this;
    }

    public PlayerBulletComponent setLifestealMultiplier(Entity entityToBeHealed, float lifestealMultiplier) {
        this.entityToBeHealed = entityToBeHealed;
        this.lifestealMultiplier = lifestealMultiplier;
        return this;
    }
}
