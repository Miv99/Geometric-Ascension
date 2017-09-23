package utils;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.math.MathUtils;
import com.miv.Mappers;
import com.miv.Options;

import java.util.ArrayList;

import components.AIComponent;
import components.BossComponent;
import components.EnemyComponent;
import components.PpOrbComponent;
import components.HitboxComponent;
import systems.RenderSystem;

/**
 * Created by Miv on 6/26/2017.
 */
public class Utils {
    public class FixedStack<T> {
        private T[] stack;
        private int size;
        private int top;
        private int popBalance = 0;//its used to see if all the elements have been popped

        public FixedStack(T[] stack) {
            this.stack = stack;
            this.top = 0;
            this.size = stack.length;
        }

        public void push(T obj) {
            if (top == stack.length)top = 0;
            stack[top] = obj;
            top++;
            if (popBalance < size - 1)popBalance++;
        }

        public T pop() {

            if (top - 1 < 0)top = size;
            top--;
            T ob = stack[top];
            popBalance--;
            return ob;
        }

        public void clear() {
            top = 0;
        }

        public int size() {
            return size;
        }

        public boolean poppedAll() {
            if (popBalance == -1)return true;
            return false;
        }
    }

    /**
     * Checks if c overlaps with any circle in circles
     */
    public static boolean overlaps(CircleHitbox c, ArrayList<CircleHitbox> circles) {
        for(CircleHitbox a : circles) {
            if(c.overlaps(a)) {
                return true;
            }
        }
        return false;
    }

    public static float getDistance(Point p1, Point p2) {
        return (float)Math.sqrt((p1.x - p2.x)*(p1.x - p2.x) + (p1.y - p2.y)*(p1.y - p2.y));
    }

    public static float getDistance(Point p1, int x2, int y2) {
        return (float)Math.sqrt((p1.x - x2)*(p1.x - x2) + (p1.y - y2)*(p1.y - y2));
    }

    public static float getDistance(Point p1, float x2, float y2) {
        return (float)Math.sqrt((p1.x - x2)*(p1.x - x2) + (p1.y - y2)*(p1.y - y2));
    }

    public static float getDistance(float x1, float y1, float x2, float y2) {
        return (float)Math.sqrt((x1 - x2)*(x1 - x2) + (y1 - y2)*(y1 - y2));
    }

    /**
     * Returns the same angle in the range [0, 360) degrees.
     * Angle returned is in radians.
     */
    public static float normalizeAngle(float angleInRadians) {
        if(angleInRadians < 0) {
            return normalizeAngle(angleInRadians + MathUtils.PI2);
        } else if(angleInRadians > MathUtils.PI2) {
            return normalizeAngle(angleInRadians - MathUtils.PI2);
        } else {
            return angleInRadians;
        }
    }

    /**
     * Returns the same angle but in the range (-180, 180] degrees.
     * Angle returned is in radians.
     */
    public static float normalizeAngleIn180Range(float angleInRadians) {
        if(angleInRadians < -MathUtils.PI) {
            return normalizeAngleIn180Range(angleInRadians + MathUtils.PI2);
        } else if(angleInRadians > MathUtils.PI) {
            return normalizeAngleIn180Range(angleInRadians - MathUtils.PI2);
        } else {
            return angleInRadians;
        }
    }

    public static Entity cloneEnemy(PooledEngine engine, Entity original, ArrayList<CircleHitbox> circles, boolean cloneAI) {
        Entity e = engine.createEntity();

        HitboxComponent hitbox = engine.createComponent(HitboxComponent.class);
        HitboxComponent originalHitbox = Mappers.hitbox.get(original);
        hitbox.setSubEntityStats(originalHitbox.getSubEntityStats());
        hitbox.setMaxSpeed(originalHitbox.getMaxSpeed());
        hitbox.setOrigin(originalHitbox.getOrigin().x, originalHitbox.getOrigin().y);
        hitbox.setIsShooting(true);
        hitbox.setVelocity(originalHitbox.getVelocity().x, originalHitbox.getVelocity().y);
        hitbox.setAcceleration(originalHitbox.getAcceleration().x, originalHitbox.getAcceleration().y, originalHitbox.getAccelerationTime());
        for(CircleHitbox c : circles) {
            hitbox.addCircle(c, false);
        }
        hitbox.setLastFacedAngle(originalHitbox.getLastFacedAngle());
        hitbox.setAimingAngle(originalHitbox.getAimingAngle());
        e.add(hitbox);

        if(cloneAI && Mappers.ai.has(original)) {
            e.add(engine.createComponent(AIComponent.class).setAi(Mappers.ai.get(original).getAi().getSubEntityAI().clone(e)));
        }

        e.add(engine.createComponent(EnemyComponent.class));

        if(Mappers.boss.has(e)) {
            e.add(engine.createComponent(BossComponent.class));
        }

        return e;
    }

    /**
     * The aura giver also receives aura buffs
     */
    public static void setAuraBuffsForAllCircles(ArrayList<CircleHitbox> circles) {
        for(CircleHitbox c : circles) {
            c.removeAuraBuffs();
        }

        for(CircleHitbox c : circles) {
            if(c.getSpecialization().hasAura()) {
                for(CircleHitbox c2 : circles) {
                    if(getDistance(c.x, c.y, c2.x, c2.y) < c.radius + c2.radius + Options.CIRCLE_AURA_RANGE) {
                        c2.receiveAuraBuffs(c);
                    }
                }
            }
        }
    }

    public static void spawnPpOrbs(PooledEngine engine, float x, float y, float maxExplosionRange, int orbCount, float totalPp) {
        float totalOrbsRadii = 25f * orbCount;

        // Generate array of floats of size orbCount that determines how much of total pp each orb is worth
        float[] ppPercents = new float[orbCount];
        float min = 1f/(orbCount * 2.2f);
        float max = 1f - (min*orbCount);
        float sum = 0f;
        for(int i = 0; i < orbCount; i++) {
            ppPercents[i] = MathUtils.random(min, max);
            sum += ppPercents[i];
        }
        for(int i = 0; i < orbCount; i++) {
            ppPercents[i] /= sum;
        }

        for(int i = 0; i < orbCount; i++) {
            // Scale orb radius to orb's percent of total pp
            float orbRadius = totalOrbsRadii * ppPercents[i];
            float travelDistance = MathUtils.random(orbRadius, maxExplosionRange);
            travelDistance /= 24f;

            Entity e = engine.createEntity();

            HitboxComponent hitbox = engine.createComponent(HitboxComponent.class);
            hitbox.setOrigin(x, y);
            CircleHitbox c = new CircleHitbox();
            c.setRadius(orbRadius);
            c.setHitboxTextureType(RenderSystem.HitboxTextureType.PP_ORB);
            c.setPpGain(totalPp * ppPercents[i]);
            hitbox.addCircle(c, true);
            hitbox.calculateGravitationalRadius();
            e.add(hitbox);

            e.add(engine.createComponent(PpOrbComponent.class));

            // Set velocity and deceleration for orbs such that it stops right at travelDistance
            float angle = MathUtils.random(MathUtils.PI2);
            float timeToDestination = MathUtils.random(1f, 2f);
            float initialSpeed = MathUtils.random(30, 65);
            hitbox.setMaxSpeed(9999f);
            hitbox.setVelocity(initialSpeed * MathUtils.cos(angle), initialSpeed * MathUtils.sin(angle));
            float acceleration = (2f * (travelDistance - (initialSpeed * timeToDestination)))/(timeToDestination * timeToDestination);

            // Ensure acceleration is always in the opposite direction of velocity
            float a = 1f;
            float b = 1f;
            if((hitbox.getVelocity().x > 0 && acceleration * MathUtils.cos(angle) > 0) || (hitbox.getVelocity().x < 0 && acceleration * MathUtils.cos(angle) < 0)) {
                a = -1f;
            }
            if((hitbox.getVelocity().y > 0 && acceleration * MathUtils.sin(angle) > 0) || (hitbox.getVelocity().y < 0 && acceleration * MathUtils.sin(angle) < 0)) {
                b = -1f;
            }
            hitbox.setAcceleration(acceleration * MathUtils.cos(angle) * a, acceleration * MathUtils.sin(angle) * b, timeToDestination/2f);

            engine.addEntity(e);
        }
    }
}
