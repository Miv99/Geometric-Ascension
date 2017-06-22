package utils;

import com.badlogic.ashley.core.Entity;

/**
 * Created by Miv on 6/21/2017.
 */
public abstract class OnCollisionEvent {
    /**
     * @param self - the entity that has this OnCollisionEvent
     * @param other - the entity that is colliding with self
     */
    public abstract void onCollision(Entity self, Entity other);
}
