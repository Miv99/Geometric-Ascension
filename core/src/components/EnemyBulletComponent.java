package components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by Miv on 5/23/2017.
 */
public class EnemyBulletComponent implements Component, Pool.Poolable {
    private float damage;

    @Override
    public void reset() {
        damage = 0;
    }

    public float getDamage() {
        return damage;
    }

    public EnemyBulletComponent setDamage(float damage) {
        this.damage = damage;
        return this;
    }
}