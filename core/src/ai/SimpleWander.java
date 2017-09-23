package ai;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.MathUtils;

import map.EntityCreationData;
import utils.Point;
import utils.Utils;

/**
 * Acceleration is randomly changed on a randomized interval.
 * Entity is confined to a given radius.
 * Acceleration is limited to a given range.
 * Created by redherring303 on 6/22/2017
 */
public class SimpleWander extends AI {
    private float wanderRadius;
    private float interval;
    private float minInterval;
    private float maxInterval;
    private boolean waitingForReset;
    private float minAcceleration;
    private float maxAcceleration;
    private Point initialPos;

    private float time;

    public SimpleWander(Entity self, RotationBehaviorParams rotationBehaviorParams, float wanderRadius, float minInterval, float maxInterval, float minAcceleration, float maxAcceleration) {
        super(self, self, rotationBehaviorParams);
        this.wanderRadius = wanderRadius;
        this.minInterval = minInterval;
        this.maxInterval = maxInterval;
        this.minAcceleration = minAcceleration;
        this.maxAcceleration = maxAcceleration;
        this.wanderRadius = wanderRadius;
        initialPos = new Point(selfHitbox.getOrigin());
        waitingForReset = true;
    }

    @Override
    public void update(float deltaTime) {
        if(waitingForReset) {
            // Calculate angle of travel
            float angle;
            // If hitbox origin is outside the wander radius from initial position, min/max angles are bounded by the tangent lines
            // from current position to the circle created by the initial position and wander radius
            float distanceFromOriginToInitial = Utils.getDistance(selfPos, initialPos);
            if(distanceFromOriginToInitial > wanderRadius) {
                float a1 = MathUtils.atan2(initialPos.y - selfPos.y, initialPos.x - selfPos.x);
                float a2 = (float)Math.asin((double)wanderRadius/distanceFromOriginToInitial);
                angle = MathUtils.random(a1 - a2, a1 + a2);
            }
            // Else, random angle with min of 0 and max of 2pi
            else {
                angle = MathUtils.random(MathUtils.PI2);
            }

            float accelerationMagnitude = MathUtils.random(minAcceleration, maxAcceleration);

            interval = MathUtils.random(minInterval, maxInterval);
            selfHitbox.setAcceleration(accelerationMagnitude * MathUtils.cos(angle), accelerationMagnitude * MathUtils.sin(angle), interval);

            waitingForReset = false;
        }

        // Face player
        rotationBehavior.update(deltaTime);

        if(time > interval) {
            waitingForReset = true;
            time = 0;
        }
        time += deltaTime;
    }

    @Override
    public AI clone(Entity newSelf) {
        return new SimpleWander(newSelf, rotationBehaviorParams, wanderRadius, minInterval, maxInterval, minAcceleration, maxAcceleration);
    }

    @Override
    public void saveToEntityCreationData(EntityCreationData ecd) {
        ecd.setAiType(AIType.SIMPLE_WANDER);
        ecd.setSimpleWanderMaxAcceleration(maxAcceleration);
        ecd.setSimpleWanderMaxInterval(maxInterval);
        ecd.setSimpleWanderMinAcceleration(minAcceleration);
        ecd.setSimpleWanderRadius(wanderRadius);
        ecd.setSimpleWanderMinInterval(minInterval);
    }
}