package ai;

import java.util.Timer;
import java.util.TimerTask;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.MathUtils;
import com.miv.Options;

import utils.Point;

/**
 * Acceleration is randomly changed on a randomized interval.
 * Entity is confined to a given radius.
 * Acceleration is limited to a given range.
 * Created by redherring303 on 6/22/2017
 */
public class SimpleWander extends AI {
    private float radius;
    private float interval;
    private float minInterval;
    private float maxInterval;
    private boolean waitingForReset;
    private float minAcceleration;
    private float maxAcceleration;
    private Timer timer = new Timer();

    private float time;

    public SimpleWander(Entity self, float radius, float minInterval, float maxInterval, float minAcceleration, float maxAcceleration) {
        super(self, self);
        this.radius = radius;
        this.minInterval = minInterval;
        this.maxInterval = maxInterval;
        this.minAcceleration = minAcceleration;
        this.maxAcceleration = maxAcceleration;
        waitingForReset = true;
    }

    @Override
    public void update(float deltaTime) {
        if(waitingForReset) {
            Point selfPos = selfHitbox.getOrigin();

            float randomHorizontalAccelerationToBeSet = minAcceleration + ((float) Math.random() * maxAcceleration);
            float randomVerticalAccelerationToBeSet = minAcceleration + ((float) Math.random() * maxAcceleration);

            float distance = (float) Math.sqrt((selfPos.x - (selfPos.x + selfHitbox.getVelocity().x + randomHorizontalAccelerationToBeSet) * (selfPos.x - (selfPos.x + selfHitbox.getVelocity().x + randomHorizontalAccelerationToBeSet)) + ((selfPos.y - (selfPos.y + selfHitbox.getVelocity().y + randomVerticalAccelerationToBeSet)) * (selfPos.y - (selfPos.y + selfHitbox.getVelocity().y + randomVerticalAccelerationToBeSet)))));

            if (distance > radius) {
                // TODO: ensure acceleration applied keeps entity within radius
                randomHorizontalAccelerationToBeSet = randomVerticalAccelerationToBeSet = minAcceleration;
            }

            final float randomHorizontalAccelerationToBeSetFinal = randomHorizontalAccelerationToBeSet;
            final float randomVerticalAccelerationToBeSetFinal = randomVerticalAccelerationToBeSet;

            selfHitbox.setAcceleration(randomHorizontalAccelerationToBeSetFinal, randomVerticalAccelerationToBeSetFinal);

            interval = MathUtils.random(minInterval, maxInterval);
            waitingForReset = false;
        }

        if(time > interval) {
            waitingForReset = true;
        }
        time++;
    }
}