package ai;

import java.util.Timer;
import java.util.TimerTask;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.MathUtils;
import com.miv.Options;

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

    public SimpleWander(Entity self, float wanderRadius, float minInterval, float maxInterval, float minAcceleration, float maxAcceleration) {
        super(self, self);
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
            Point selfPos = selfHitbox.getOrigin();

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

            // Face player
            selfHitbox.setLastFacedAngle(MathUtils.atan2(targetPos.y - selfPos.y, targetPos.x - selfPos.x));

            selfHitbox.setAcceleration(accelerationMagnitude * MathUtils.cos(angle), accelerationMagnitude * MathUtils.sin(angle));

            interval = MathUtils.random(minInterval, maxInterval);
            waitingForReset = false;
        }

        if(time > interval) {
            waitingForReset = true;
            time = 0;
        }
        time += deltaTime;
    }
}