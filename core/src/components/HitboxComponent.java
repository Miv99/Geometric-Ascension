package components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.miv.AttackPart;
import com.miv.AttackPattern;
import com.miv.EntityActions;
import com.miv.Mappers;
import com.miv.Options;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import ai.AI;
import map.EntityCreationData;
import map.Map;
import utils.CircleHitbox;
import utils.Point;
import utils.Utils;

/**
 * A hitbox consists of an array of circles.
 * Collision detection is done using the {@link Circle#overlaps(Circle)} method.
 * Created by Miv on 5/23/2017.
 */
public class HitboxComponent implements Component, Pool.Poolable {
    public static class SubEntityStats {
        public float maxSpeed;
        public EntityCreationData aiData;
    }

    // Position of hitbox
    private Point origin;
    // Velocity of hitbox, in meters/frame
    private Vector2 velocity;
    // Acceleration of hitbox, in meters/frame^2
    private Vector2 acceleration;
    // Time until acceleration stops
    private float accelerationTime;
    // Circle positions are relative to components.HitboxComponent#origin
    private ArrayList<CircleHitbox> circles;
    // If true, the hitbox will not make contact with anything
    private boolean intangible;
    // In radians
    private float lastFacedAngle;
    private float targetAngle;
    // For shooting
    private float aimingAngle;
    // If the hitbox is firing bullets
    private boolean isShooting;
    /**
     * Circles to be removed from {@link HitboxComponent#circles} by {@link systems.MovementSystem#update(float)} every frame
     */
    private ArrayList<CircleHitbox> circleRemovalQueue;
    // Distance from origin where enemies and players' gravity effects are maximized
    private float gravitationalRadius;

    private boolean disabledMovement;

    // If true, hitbox will not push away other hitboxes and will not be pushed away by other hitboxes
    private boolean ignoreGravity;

    // Indicates whether or not the entity is travelling to a new location, ignoring all obstacles
    private boolean travelling;
    // Time spent travelling so far
    private float travellingTime;
    // Random crap for player travelling to new map areas
    private boolean travellingFlag;
    private boolean travellingFromSameMapArea;
    private float travellingDirectionX;
    private float travellingDirectionY;
    private float travellingVelocityX;
    private float travellingVelocityY;
    private Point travellingDestination;
    private Point travellingMapAreaDestination;

    private boolean ignoreSpeedLimit;
    private float maxSpeed;
    private float baseMaxSpeed;

    private SubEntityStats subEntityStats;

    public HitboxComponent() {
        origin = new Point();
        velocity = new Vector2();
        acceleration = new Vector2();
        circles = new ArrayList<CircleHitbox>();
        circleRemovalQueue = new ArrayList<CircleHitbox>();
    }

    @Override
    public void reset() {
        origin.x = 0;
        origin.y = 0;
        velocity.set(0, 0);
        acceleration.set(0, 0);
        accelerationTime = 0;
        circles.clear();
        gravitationalRadius = 0;
        circleRemovalQueue.clear();
        isShooting = false;
        intangible = false;
        ignoreSpeedLimit = false;
        maxSpeed = 0;
        travelling = false;
        travellingTime = 0;
        travellingFlag = false;
        travellingDirectionX = 0;
        travellingDirectionY = 0;
        travellingVelocityX = 0;
        travellingVelocityY = 0;
        travellingDestination = null;
        travellingMapAreaDestination = null;
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
                        ap.fire(engine, parent, player, origin.x + c.x, origin.y + c.y, aimingAngle + attackPattern.getAngleOffset());
                        fired[index] = true;
                    } else {
                        break;
                    }
                    index++;
                }

                if (c.getTime() >= attackPattern.getDuration()) {
                    // Compensates for lag but will only fire one bullet max no matter how big the lag
                    c.resetAttackPattern();
                    c.setTime(c.getTime() % attackPattern.getDuration());
                }
            }
        }
    }

    public void recenterOriginalCirclePositions() {
        // Find left/top/right/bottom bounds of the group of circles
        CircleHitbox c1 = circles.get(0);

        float left = c1.getOriginalPosX() - c1.radius, right = c1.getOriginalPosX() + c1.radius,
                top = c1.getOriginalPosY() + c1.radius, bottom = c1.getOriginalPosY() - c1.radius;
        for(int i = 1; i < circles.size(); i++) {
            CircleHitbox c = circles.get(i);
            if(c.getOriginalPosX() - c.radius < left) {
                left = c.getOriginalPosX() - c.radius;
            }
            if(c.getOriginalPosX() + c.radius > right) {
                right = c.getOriginalPosX() + c.radius;
            }
            if(c.getOriginalPosY() - c.radius < bottom) {
                bottom = c.getOriginalPosY() - c.radius;
            }
            if(c.getOriginalPosY() + c.radius > top) {
                top = c.getOriginalPosY() + c.radius;
            }
        }

        //TODO: bug: bounds change --> entity teleports for some reason

        // Shift all circles by center of bounds
        float deltaX = (left + right)/2f;
        float deltaY = (bottom + top)/2f;
        for(CircleHitbox c : circles) {
            c.setOriginalPosX(c.getOriginalPosX() - deltaX);
            c.setOriginalPosY(c.getOriginalPosY() - deltaY);
        }

        float c1XBeforeRotation = c1.x;
        float c1YBeforeRotation = c1.y;

        setLastFacedAngle(lastFacedAngle);

        // Shift origin by the change in the circles' positions to maintain the same world position
        origin.x -= c1.x - c1XBeforeRotation;
        origin.y -= c1.y - c1YBeforeRotation;

        gravitationalRadius = Math.max((Math.abs(left) + Math.abs(right))/2f, (Math.abs(top) + Math.abs(bottom))/2f) + Options.GRAVITATIONAL_RADIUS_PADDING;
    }

    public void calculateGravitationalRadius() {
        // Find left/top/right/bottom bounds of the group of circles
        CircleHitbox c1 = circles.get(0);

        float left = c1.getOriginalPosX() - c1.radius, right = c1.getOriginalPosX() + c1.radius,
                top = c1.getOriginalPosY() + c1.radius, bottom = c1.getOriginalPosY() - c1.radius;
        for(int i = 1; i < circles.size(); i++) {
            CircleHitbox c = circles.get(i);
            if(c.getOriginalPosX() - c.radius < left) {
                left = c.getOriginalPosX() - c.radius;
            }
            if(c.getOriginalPosX() + c.radius > right) {
                right = c.getOriginalPosX() + c.radius;
            }
            if(c.getOriginalPosY() - c.radius < bottom) {
                bottom = c.getOriginalPosY() - c.radius;
            }
            if(c.getOriginalPosY() + c.radius > top) {
                top = c.getOriginalPosY() + c.radius;
            }
        }

        gravitationalRadius = Math.max((Math.abs(left) + Math.abs(right))/2f, (Math.abs(top) + Math.abs(bottom))/2f) + Options.GRAVITATIONAL_RADIUS_PADDING;
    }

    public Entity splitIntoSubEntities(PooledEngine engine, Entity self) {
        // Disable subentity splitting for player
        if(Mappers.player.has(self)) {
            return null;
        }

        // Get all circles connected to the first circle hitbox
        HashSet<CircleHitbox> cSet = new HashSet<CircleHitbox>();
        addConnectedCircles(0, cSet);

        if(cSet.size() != circles.size()) {
            ArrayList<CircleHitbox> subEntityCircles = new ArrayList<CircleHitbox>();
            ArrayList<CircleHitbox> removalQueue = new ArrayList<CircleHitbox>();

            float totalRadius1 = 0f;
            float totalRadius2 = 0f;

            // Turn set of smaller circles into subentity
            for(CircleHitbox c : circles) {
                if(!cSet.contains(c)) {
                    totalRadius1 += c.radius;
                } else {
                    totalRadius2 += c.radius;
                }
            }
            if(totalRadius1 < totalRadius2) {
                for(CircleHitbox c : circles) {
                    if(!cSet.contains(c)) {
                        subEntityCircles.add(c);
                        removalQueue.add(c);
                    }
                }
            } else {
                for(CircleHitbox c : circles) {
                    if(cSet.contains(c)) {
                        subEntityCircles.add(c);
                        removalQueue.add(c);
                    }
                }
            }
            for(CircleHitbox c : removalQueue) {
                circles.remove(c);
            }

            //TODO: add to this as more fields added to subentity stats
            Entity e = Utils.cloneEnemy(engine, self, subEntityCircles, (subEntityStats == null || subEntityStats.aiData == null));
            if(subEntityStats != null) {
                Mappers.hitbox.get(e).setMaxSpeed(subEntityStats.maxSpeed);
                if(subEntityStats.aiData != null) {
                    e.add(Map.createAIComponent(engine, e, subEntityStats.aiData, Mappers.ai.get(self).getAi().getTarget()));
                }
            }
            Mappers.hitbox.get(e).recenterOriginalCirclePositions();
            return e;
        } else {
            return null;
        }
    }

    private void addConnectedCircles(int cIndex, HashSet<CircleHitbox> set) {
        if(cIndex < circles.size()) {
            CircleHitbox c = circles.get(cIndex);
            set.add(c);
            for (int a = 0; a < circles.size(); a++) {
                CircleHitbox c2 = circles.get(a);
                // Add 1f to distance check to account for inaccuracies
                if (!set.contains(c2) && !c.equals(c2) && Utils.getDistance(c.x, c.y, c2.x, c2.y) < c.radius + c2.radius + 1f) {
                    set.add(c2);
                    addConnectedCircles(a, set);
                }
            }
        }
    }

    public float getTotalMissingHealth() {
        float missing = 0;
        for(CircleHitbox c : circles) {
            missing += c.getMaxHealth() - c.getHealth();
        }
        return missing;
    }

    public float getTotalHealingCostInPp() {
        return getTotalMissingHealth()/Options.HEALTH_PER_PP_HEALING_COST_RATIO;
    }

    public void healWeakestCircle(float health) {
        CircleHitbox weakest = circles.get(0);
        for(CircleHitbox c : circles) {
            if(c.getHealth() < weakest.getHealth()) {
                weakest = c;
            }
        }
        weakest.setHealth(weakest.getHealth() + health);
    }

    /**
     * @param pp amount of pp put into healing
     * @return the leftover pp
     */
    public float heal(float pp) {
        float newPP = pp;
        for(CircleHitbox c : circles) {
            newPP = heal(c, newPP);
            if(newPP <= 0) {
                return 0;
            }
        }
        return newPP;
    }

    /**
     * @param pp amount of pp put into healing
     * @return the leftover pp
     */
    public float heal(CircleHitbox circle, float pp) {
        float missing = circle.getMaxHealth() - circle.getHealth();
        float ppCost = missing/Options.HEALTH_PER_PP_HEALING_COST_RATIO;
        if(pp >= ppCost) {
            circle.setHealth(circle.getMaxHealth());
            return pp - ppCost;
        } else {
            circle.setHealth(circle.getHealth() + pp * Options.HEALTH_PER_PP_HEALING_COST_RATIO);
            return 0;
        }
    }

    /**
     * Clears all circles without calling {@link HitboxComponent#removeCircle(PooledEngine, Entity, CircleHitbox, boolean)}
     */
    public void clearCircles() {
        circles.clear();
    }

    /**
     * For player only
     */
    public void update(float deltaTime) {
        float currentRotationAngle = lastFacedAngle;

        // Lerp rotation to target angle
        while(targetAngle - currentRotationAngle > MathUtils.PI || targetAngle - currentRotationAngle < -MathUtils.PI) {
            if (targetAngle - currentRotationAngle > MathUtils.PI) {
                targetAngle -= MathUtils.PI2;
            } else if (targetAngle - currentRotationAngle < -MathUtils.PI) {
                targetAngle += MathUtils.PI2;
            }
        }
        currentRotationAngle += (targetAngle - currentRotationAngle) * 0.995f * deltaTime;
        setLastFacedAngle(currentRotationAngle);
    }

    public void setTargetAngle(float targetAngle) {
        this.targetAngle = targetAngle;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public void setVelocity(float x, float y) {
        velocity.set(x, y);

        if(!ignoreSpeedLimit) {
            velocity.limit(maxSpeed);
        }
    }

    public Vector2 getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(float x, float y, float time) {
        acceleration.set(x, y);
        accelerationTime = time;
    }

    public float getAccelerationTime() {
        return accelerationTime;
    }

    public void setAccelerationTime(float accelerationTime) {
        this.accelerationTime = accelerationTime;
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

    public float getLastFacedAngle() {
        return lastFacedAngle;
    }

    public void setLastFacedAngle(float lastFacedAngle) {
        this.lastFacedAngle = lastFacedAngle;

        // Revolve all circles around (0, 0)
        for(int i = 0; i < circles.size(); i++) {
            CircleHitbox c = circles.get(i);
            c.setPosition(c.getOriginalPosX() * MathUtils.cos(lastFacedAngle) - c.getOriginalPosY() * MathUtils.sin(lastFacedAngle),
                    c.getOriginalPosX() * MathUtils.sin(lastFacedAngle) + c.getOriginalPosY() * MathUtils.cos(lastFacedAngle));
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

    public void addCircle(CircleHitbox circle, boolean setOriginal) {
        circles.add(circle);
        if(setOriginal) {
            circle.setOriginalPosX(circle.x);
            circle.setOriginalPosY(circle.y);
        }

        Utils.setAuraBuffsForAllCircles(circles);
        recalculateSpeedBoost();
    }

    public void queueCircleRemoval(CircleHitbox circle) {
        circleRemovalQueue.add(circle);
    }

    public ArrayList<CircleHitbox> getCircleRemovalQueue() {
        return circleRemovalQueue;
    }

    public void clearCircleRemovalQueue() {
        circleRemovalQueue.clear();
    }

    public ArrayList<Entity> removeCircle(PooledEngine engine, Entity self, CircleHitbox c, boolean transferPpToBosses) {
        circles.remove(c);

        if(circles.size() > 0) {
            ArrayList<Entity> subEntities = new ArrayList<Entity>();

            Entity subEntity = splitIntoSubEntities(engine, self);
            while(subEntity != null) {
                subEntities.add(subEntity);
                subEntity = splitIntoSubEntities(engine, self);
            }
            recenterOriginalCirclePositions();

            // Transfer part of pp to all other boss entities' circles' attack patterns evenly
            if(transferPpToBosses && c.getAttackPattern() != null) {
                float pp = c.getAttackPattern().getTotalPpInStatModifiers();

                ImmutableArray<Entity> bosses = engine.getEntitiesFor(Family.all(BossComponent.class).get());
                int circlesWithAttackPatternCount = 0;
                for(Entity e : bosses) {
                    ArrayList<CircleHitbox> circles = Mappers.hitbox.get(e).circles;
                    for(CircleHitbox circle : circles) {
                        if(circle.getAttackPattern() != null) {
                            circlesWithAttackPatternCount++;
                        }
                    }
                }

                for(Entity e : bosses) {
                    ArrayList<CircleHitbox> circles = Mappers.hitbox.get(e).circles;
                    for(CircleHitbox circle : circles) {
                        if(circle.getAttackPattern() != null) {
                            circle.getAttackPattern().addRandomAttackPatternStatModifiers(pp/circlesWithAttackPatternCount * Options.BOSS_PP_TRANSFER_PERCENT);
                        }
                    }
                }
            }

            // Recalculate aura buffs
            Utils.setAuraBuffsForAllCircles(circles);
            // Recalculate speed boost
            recalculateSpeedBoost();

            return subEntities;
        }

        return null;
    }

    private void recalculateSpeedBoost() {
        float speedBoostFromSpecializations = 0f;
        for(CircleHitbox c : circles) {
            speedBoostFromSpecializations += c.getSpeedBoost();
        }
        maxSpeed = baseMaxSpeed + speedBoostFromSpecializations;
    }

    public float getGravitationalRadius() {
        return gravitationalRadius;
    }

    public boolean isIgnoreSpeedLimit() {
        return ignoreSpeedLimit;
    }

    public void setIgnoreSpeedLimit(boolean ignoreSpeedLimit) {
        this.ignoreSpeedLimit = ignoreSpeedLimit;
    }

    public boolean isTravelling() {
        return travelling;
    }

    public void setTravelling(boolean travelling) {
        this.travelling = travelling;
    }

    public float getTravellingTime() {
        return travellingTime;
    }

    public void setTravellingTime(float travellingTime) {
        this.travellingTime = travellingTime;
    }

    public boolean isTravellingFlag() {
        return travellingFlag;
    }

    public void setTravellingFlag(boolean travellingFlag) {
        this.travellingFlag = travellingFlag;
    }

    public void setTravellingDestination(Point travellingDestination) {
        this.travellingDestination = travellingDestination;
    }

    public boolean isIgnoreGravity() {
        return ignoreGravity;
    }

    public void setIgnoreGravity(boolean ignoreGravity) {
        this.ignoreGravity = ignoreGravity;
    }

    public SubEntityStats getSubEntityStats() {
        return subEntityStats;
    }

    public void setSubEntityStats(SubEntityStats subEntityStats) {
        this.subEntityStats = subEntityStats;
    }

    public boolean isTravellingFromSameMapArea() {
        return travellingFromSameMapArea;
    }

    public void setTravellingFromSameMapArea(boolean travellingFromSameMapArea) {
        this.travellingFromSameMapArea = travellingFromSameMapArea;
    }

    public boolean isDisabledMovement() {
        return disabledMovement;
    }

    public void setDisabledMovement(boolean disabledMovement) {
        this.disabledMovement = disabledMovement;
    }

    public float getTravellingDirectionX() {
        return travellingDirectionX;
    }

    public void setTravellingDirectionX(float travellingDirectionX) {
        this.travellingDirectionX = travellingDirectionX;
    }

    public float getTravellingDirectionY() {
        return travellingDirectionY;
    }

    public void setTravellingDirectionY(float travellingDirectionY) {
        this.travellingDirectionY = travellingDirectionY;
    }

    public float getTravellingVelocityX() {
        return travellingVelocityX;
    }

    public void setTravellingVelocityX(float travellingVelocityX) {
        this.travellingVelocityX = travellingVelocityX;
    }

    public float getTravellingVelocityY() {
        return travellingVelocityY;
    }

    public void setTravellingVelocityY(float travellingVelocityY) {
        this.travellingVelocityY = travellingVelocityY;
    }

    public Point getTravellingMapAreaDestination() {
        return travellingMapAreaDestination;
    }

    public void setTravellingMapAreaDestination(Point travellingMapAreaDestination) {
        this.travellingMapAreaDestination = travellingMapAreaDestination;
    }

    public float getAimingAngle() {
        return aimingAngle;
    }

    public void setAimingAngle(float aimingAngle) {
        this.aimingAngle = aimingAngle;
    }

    public float getBaseMaxSpeed() {
        return baseMaxSpeed;
    }

    public void setMaxSpeed(float baseMaxSpeed) {
        this.baseMaxSpeed = baseMaxSpeed;
        recalculateSpeedBoost();
    }
}
