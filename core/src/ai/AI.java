package ai;

import com.badlogic.ashley.core.Entity;
import com.miv.Mappers;

import components.HitboxComponent;
import utils.Point;

/**
 * Created by Miv on 6/20/2017.
 */
public abstract class AI {
    public static enum AIType {
        SIMPLE_FOLLOW_TARGET,
        SIMPLE_STALK_TARGET,
        SIMPLE_WANDER;
    }

    protected HitboxComponent selfHitbox;
    protected Point targetPos;

    public AI(Entity self, Entity target) {
        selfHitbox = Mappers.hitbox.get(self);
        targetPos = Mappers.hitbox.get(target).getOrigin();
    }

    public abstract void update(float deltaTime);
}
