package components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.miv.AttackPart;
import com.miv.AttackPattern;

import java.util.ArrayList;
import java.util.HashMap;

import utils.CircleHitbox;
import utils.Point;

/**
 * A hitbox consists of an array of circles.
 * Collision detection is done using the {@link Circle#overlaps(Circle)} method.
 * Created by Miv on 5/23/2017.
 */
public class HitboxComponent implements Component, Pool.Poolable {
    // Position of hitbox
    private Point origin;
    // Velocity of hitbox, in meters/frame
    private Vector2 velocity;
    // Acceleration of hitbox, in meters/frame^2
    private Vector2 acceleration;
    // Circle positions are relative to components.HitboxComponent#origin
    private ArrayList<CircleHitbox> circles;
    // Original circle positions with the hitbox facing angle 0
    // Used to prevent inaccuracies when rotating hitbox multiple times
    // Must be in the same order as circles
    private ArrayList<Point> originalCirclePositions;
    // If true, the hitbox will not make contact with anything
    private boolean intangible;
    // In radians
    private float lastFacedAngle;
    // If the hitbox is firing bullets
    private boolean isShooting;

    private float maxSpeed;

    public HitboxComponent() {
        origin = new Point();
        velocity = new Vector2();
        acceleration = new Vector2();
        circles = new ArrayList<CircleHitbox>();
        originalCirclePositions = new ArrayList<Point>();
    }

    @Override
    public void reset() {
        origin.x = 0;
        origin.y = 0;
        velocity.set(0, 0);
        acceleration.set(0, 0);
        circles.clear();
        originalCirclePositions.clear();
    }

    public void update(PooledEngine engine, Entity parent, Entity player, float deltaTime) {
        for (CircleHitbox c : circles) {
            AttackPattern attackPattern = c.getAttackPattern();
            if (attackPattern != null) {
                boolean[] fired = c.getFired();
                c.setTime(c.getTime() + deltaTime);

                int index = 0;
                for (AttackPart ap : attackPattern.getAttackParts()) {
                    if (!fired[index] && c.getTime() >= ap.getDelay()) {
                        ap.fire(engine, parent, player, origin.x + c.x, origin.y + c.y, lastFacedAngle);
                        fired[index] = true;
                    } else {
                        break;
                    }
                    index++;
                }

                if (c.getTime() >= attackPattern.getDuration()) {
                    // Compensates for lag but will only fire one bullet max no matter how big the lag
                    c.setTime(c.getTime() % attackPattern.getDuration());
                    for (int i = 0; i < fired.length; i++) {
                        fired[i] = false;
                    }
                }
            }
        }
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public void setVelocity(float x, float y) {
        velocity.set(x, y);
    }

    public Vector2 getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(float x, float y) {
        acceleration.set(x, y);
    }

    public boolean isIntangible() {
        return intangible;
    }

    public void setIntangible(boolean intangible) {
        this.intangible = intangible;
    }

    public Point getOrigin() {
        return origin;
    }

    public void setOrigin(float x, float y) {
        origin.x = x;
        origin.y = y;
    }

    public float getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(float maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public float getLastFacedAngle() {
        return lastFacedAngle;
    }

    public void setLastFacedAngle(float lastFacedAngle) {
        this.lastFacedAngle = lastFacedAngle;

        // Rotate all circles around (0, 0)
        for(int i = 0; i < circles.size(); i++) {
            CircleHitbox c = circles.get(i);
            Point circleOrigin = originalCirclePositions.get(i);
            c.setPosition(circleOrigin.x * MathUtils.cos(-lastFacedAngle) - circleOrigin.y * MathUtils.sin(-lastFacedAngle), circleOrigin.x * MathUtils.sin(-lastFacedAngle) + circleOrigin.y * MathUtils.cos(-lastFacedAngle));
        }
    }

    public boolean isShooting() {
        return isShooting;
    }

    public void setIsShooting(boolean isShooting) {
        this.isShooting = isShooting;
        if(!isShooting) {
            // Reset attack patterns
            for (CircleHitbox c : circles) {
                c.setTime(0);
            }
        }
    }

    public ArrayList<CircleHitbox> getCircles() {
        return circles;
    }

    public void addCircle(CircleHitbox circle) {
        circles.add(circle);
        originalCirclePositions.add(new Point(circle.x, circle.y));
    }
}
