package utils;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
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

    // How much pp the player gains by killing this circle
    private float ppGain;

    public CircleHitbox() {}

    public CircleHitbox(boolean isEnemy) {
        if(isEnemy) {
            setHitboxTextureType(RenderSystem.HitboxTextureType.ENEMY);
        }
    }

    /**
     * True if the attack part has been fired off.
     * Is reset (all fields set to false) by {@link components.HitboxComponent#update(PooledEngine, Entity, Entity, float)}
     * after each iteration of the attack pattern
     */
    private boolean[] fired;

    public void randomizeAttackPatternTime() {
        resetAttackPattern();
        time = MathUtils.random(0, attackPattern.getDuration());

        ArrayList<AttackPart> aps = attackPattern.getAttackParts();
        for(int i = 0; i < attackPattern.getAttackParts().size(); i++) {
            if(aps.get(i).getDelay() <= time) {
                fired[i] = true;
            }
        }
    }

    public void resetAttackPattern() {
        for(int i = 0; i < fired.length; i++) {
            fired[i] = false;
        }

        time = 0;
    }

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
        fired = new boolean[attackPattern.getAttackParts().size()];
        time = 0;
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

    public float getPpGain() {
        return ppGain;
    }

    public void setPpGain(float ppGain) {
        this.ppGain = ppGain;
    }
}