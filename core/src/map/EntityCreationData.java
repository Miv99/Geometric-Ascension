package map;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.math.Circle;

import java.util.ArrayList;

import ai.AI;
import components.EnemyComponent;
import components.PlayerComponent;
import utils.CircleHitbox;

/**
 * Class used by {@link map.MapArea} that contains the data to create an entity upon entering the MapArea.
 * Cannot be used to create player entities.
 * Created by Miv on 5/23/2017.
 */
public class EntityCreationData {
    private float maxHealth;
    private boolean isEnemy;
    private boolean isBoss;
    private ArrayList<CircleHitbox> circleHitboxes;
    private float spawnX;
    private float spawnY;
    private float maxSpeed;

    private AI.AIType aiType;
    // For SimpleStalkTarget
    private float simpleStalkMinSpeedDistance;
    private float simpleStalkMaxSpeedDistance;
    // For SimpleWander
    private float simpleWanderRadius;
    private float simpleWanderMinInterval;
    private float simpleWanderMaxInterval;
    private float simpleWanderMinAcceleration;
    private float simpleWanderMaxAcceleration;

    public EntityCreationData() {
        circleHitboxes = new ArrayList<CircleHitbox>();
    }

    // For convenience
    public EntityCreationData(boolean isBoss) {
        if(isBoss) {
            setIsEnemy(true);
            setIsBoss(true);
        }
        circleHitboxes = new ArrayList<CircleHitbox>();
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(float maxHealth) {
        this.maxHealth = maxHealth;
    }

    public boolean isEnemy() {
        return isEnemy;
    }

    public void setIsEnemy(boolean isEnemy) {
        this.isEnemy = isEnemy;
    }

    public boolean isBoss() {
        return isBoss;
    }

    public void setIsBoss(boolean isBoss) {
        this.isBoss = isBoss;
    }

    public ArrayList<CircleHitbox> getCircleHitboxes() {
        return circleHitboxes;
    }

    public void setCircleHitboxes(ArrayList<CircleHitbox> circleHitboxes) {
        this.circleHitboxes = circleHitboxes;
    }

    public float getSpawnX() {
        return spawnX;
    }

    public float getSpawnY() {
        return spawnY;
    }

    public void setSpawnPosition(float x, float y) {
        spawnX = x;
        spawnY = y;
    }

    public AI.AIType getAiType() {
        return aiType;
    }

    public void setAiType(AI.AIType aiType) {
        this.aiType = aiType;
    }

    public float getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(float maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public float getSimpleStalkMinSpeedDistance() {
        return simpleStalkMinSpeedDistance;
    }

    public void setSimpleStalkMinSpeedDistance(float simpleStalkMinSpeedDistance) {
        this.simpleStalkMinSpeedDistance = simpleStalkMinSpeedDistance;
    }

    public float getSimpleStalkMaxSpeedDistance() {
        return simpleStalkMaxSpeedDistance;
    }

    public void setSimpleStalkMaxSpeedDistance(float simpleStalkMaxSpeedDistance) {
        this.simpleStalkMaxSpeedDistance = simpleStalkMaxSpeedDistance;
    }

    public float getSimpleWanderMinInterval() {
        return simpleWanderMinInterval;
    }

    public void setSimpleWanderMinInterval(float simpleWanderMinInterval) {
        this.simpleWanderMinInterval = simpleWanderMinInterval;
    }

    public float getSimpleWanderMaxInterval() {
        return simpleWanderMaxInterval;
    }

    public void setSimpleWanderMaxInterval(float simpleWanderMaxInterval) {
        this.simpleWanderMaxInterval = simpleWanderMaxInterval;
    }

    public float getSimpleWanderMinAcceleration() {
        return simpleWanderMinAcceleration;
    }

    public void setSimpleWanderMinAcceleration(float simpleWanderMinAcceleration) {
        this.simpleWanderMinAcceleration = simpleWanderMinAcceleration;
    }

    public float getSimpleWanderMaxAcceleration() {
        return simpleWanderMaxAcceleration;
    }

    public void setSimpleWanderMaxAcceleration(float simpleWanderMaxAcceleration) {
        this.simpleWanderMaxAcceleration = simpleWanderMaxAcceleration;
    }

    public float getSimpleWanderRadius() {
        return simpleWanderRadius;
    }

    public void setSimpleWanderRadius(float simpleWanderRadius) {
        this.simpleWanderRadius = simpleWanderRadius;
    }
}
