package ai;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.MathUtils;

import map.EntityCreationData;
import utils.Point;

/**
 * Velocity is at maximum speed of the entity's hitbox, facing towards the target's origin.
 * The entity's speed is lower the closer it is to the target.
 * Created by Miv on 6/20/2017.
 */
public class SimpleStalkTarget extends AI {
    private float minSpeedDistance;
    private float maxSpeedDistance;
    private float minSpeed;

    /**
     * @param minSpeedDistance - distance from target at which the entity's speed reaches its minimum
     * @param maxSpeedDistance - minimum distance from target at which the entity achieves maximum speed
     */
    public SimpleStalkTarget(Entity self, Entity target, float minSpeedDistance, float maxSpeedDistance, float minSpeed) {
        super(self, target);
        this.minSpeedDistance = minSpeedDistance;
        this.maxSpeedDistance = maxSpeedDistance;
        this.minSpeed = minSpeed;
    }

    @Override
    public void update(float deltaTime) {
        // Get angle from self to target
        Point selfPos = selfHitbox.getOrigin();
        float angle = MathUtils.atan2(targetPos.y - selfPos.y, targetPos.x - selfPos.x);
        float speed;
        float distance = (float)Math.sqrt((targetPos.y - selfPos.y)*(targetPos.y - selfPos.y) + (targetPos.x - selfPos.x)*(targetPos.x - selfPos.x));
        if(distance >= maxSpeedDistance) {
            speed = selfHitbox.getMaxSpeed();
        } else if(distance <= minSpeedDistance) {
            speed = minSpeed;
        } else {
            speed = ((distance - minSpeedDistance)/(maxSpeedDistance - minSpeedDistance)) * (selfHitbox.getMaxSpeed() - minSpeed) + minSpeed;
        }
        float velocityX = speed * MathUtils.cos(angle);
        float velocityY = speed * MathUtils.sin(angle);

        selfHitbox.setVelocity(velocityX, velocityY);
        selfHitbox.setLastFacedAngle(angle);
    }

    @Override
    public AI clone(Entity newSelf) {
        return new SimpleStalkTarget(newSelf, target, minSpeedDistance, maxSpeedDistance, minSpeed);
    }

    @Override
    public void saveToEntityCreationData(EntityCreationData ecd) {
        ecd.setAiType(AIType.SIMPLE_STALK_TARGET);
        ecd.setSimpleStalkMaxSpeedDistance(maxSpeedDistance);
        ecd.setSimpleStalkMinSpeedDistance(minSpeedDistance);
    }
}
