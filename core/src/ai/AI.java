package ai;

import com.badlogic.ashley.core.Entity;
import com.miv.Mappers;

import components.HitboxComponent;
import map.EntityCreationData;
import utils.Point;

/**
 * Created by Miv on 6/20/2017.
 */
public abstract class AI {
    public static enum AIType {
        NONE,
        SIMPLE_FOLLOW_TARGET,
        SIMPLE_STALK_TARGET,
        SIMPLE_WANDER;
    }

    protected AI subEntityAI;

    protected HitboxComponent selfHitbox;
    protected Point targetPos;

    protected Entity self;
    protected Entity target;

    public AI(Entity self, Entity target) {
        this.self = self;
        this.target = target;
        selfHitbox = Mappers.hitbox.get(self);
        targetPos = Mappers.hitbox.get(target).getOrigin();
    }

    public abstract void update(float deltaTime);

    public abstract AI clone(Entity newSelf);

    public void setSubEntityAI(AI subEntityAI) {
        this.subEntityAI = subEntityAI;
    }

    public AI getSubEntityAI() {
        if(subEntityAI == null) {
            return this;
        } else {
            return subEntityAI;
        }
    }
}
