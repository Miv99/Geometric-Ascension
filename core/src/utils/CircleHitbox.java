package utils;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Circle;
import com.miv.AttackPart;
import com.miv.AttackPattern;

import java.util.ArrayList;

import systems.RenderSystem;

/**
 * Created by Miv on 6/5/2017.
 */
public class CircleHitbox extends Circle {
    private RenderSystem.HitboxTextureType hitboxTextureType;
    private float maxHealth;
    private float health;
    private AttackPattern attackPattern;
    // Time in seconds since the last iteration of the attack pattern started
    private transient float time;
    // Original circle position with the hitbox facing angle 0
    // Used to prevent inaccuracies when rotating hitbox multiple times
    // Must be in the same order as circles
    private float originalPosX;
    private float originalPosY;

    /**
     * True if the attack part has been fired off.
     * Is reset (all fields set to false) by {@link components.HitboxComponent#update(PooledEngine, Entity, Entity, float)}
     * after each iteration of the attack pattern
     */
    private boolean[] fired;

    public RenderSystem.HitboxTextureType getHitboxTextureType() {
        return hitboxTextureType;
    }

    public CircleHitbox setHitboxTextureType(RenderSystem.HitboxTextureType hitboxTextureType) {
        this.hitboxTextureType = hitboxTextureType;
        return this;
    }

    public AttackPattern getAttackPattern() {
        return attackPattern;
    }

    public void setAttackPattern(AttackPattern attackPattern) {
        this.attackPattern = attackPattern;
        fired = new boolean[attackPattern.getAttackParts().length];
    }

    public float getTime() {
        return time;
    }

    public void setTime(float time) {
        this.time = time;
    }

    public boolean[] getFired() {
        return fired;
    }

    public void setFired(boolean[] fired) {
        this.fired = fired;
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(float maxHealth) {
        this.maxHealth = maxHealth;
    }

    public float getHealth() {
        return health;
    }

    public void setHealth(float health) {
        this.health = health;
    }

    public float getOriginalPosX() {
        return originalPosX;
    }

    public void setOriginalPosX(float originalPosX) {
        this.originalPosX = originalPosX;
    }

    public float getOriginalPosY() {
        return originalPosY;
    }

    public void setOriginalPosY(float originalPosY) {
        this.originalPosY = originalPosY;
    }
}