package ai;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.MathUtils;

import map.EntityCreationData;
import utils.Point;

/**
 * Velocity is always at maximum speed of the entity's hitbox, facing towards the target's origin
 * Created by Miv on 6/20/2017.
 */
public class SimpleFollowTarget extends AI {
    public SimpleFollowTarget(Entity self, Entity player, RotationBehaviorParams rotationBehaviorParams) {
        super(self, player, rotationBehaviorParams);
    }

    @Override
    public void update(float deltaTime) {
        // Get angle from self to target
        Point selfPos = selfHitbox.getOrigin();
        float angle = MathUtils.atan2(targetPos.y - selfPos.y, targetPos.x - selfPos.x);
        float velocityX = selfHitbox.getMaxSpeed() * MathUtils.cos(angle);
        float velocityY = selfHitbox.getMaxSpeed() * MathUtils.sin(angle);

        selfHitbox.setVelocity(velocityX, velocityY);
        rotationBehavior.update(deltaTime, angle);
    }

    @Override
    public AI clone(Entity newSelf) {
        return new SimpleFollowTarget(newSelf, target, rotationBehaviorParams);
    }

    @Override
    public void saveToEntityCreationData(EntityCreationData ecd) {
        ecd.setAiType(AIType.SIMPLE_FOLLOW_TARGET);
    }
}
