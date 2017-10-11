package com.miv;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.math.MathUtils;

import components.EnemyBulletComponent;
import components.ExpirationComponent;
import components.HitboxComponent;
import components.PlayerBulletComponent;
import systems.RenderSystem;
import utils.CircleHitbox;
import utils.Point;

/**
 * @see AttackPattern
 * Created by Miv on 5/25/2017.
 */
public class AttackPart {
    public static enum AttackPartAngleDeterminant {
        // angleInDegrees is the angle offset added to the angle from the parent entity to the player entity
        AIM_AT_PLAYER,
        // angleInDegrees is the angle offset added to the parent entity's current rotation
        AIM_RELATIVE_TO_PARENT_ROTATION,
        // angleInDegrees is the absolute angle of travel of the bullet
        NONE
    }

    // Time in seconds relative to the start of the attack pattern until this attack part is fired
    private float delay;
    // Origin of the bullet with respect to the origin of the parent entity
    private float originX;
    private float originY;

    private float speed;
    private float radius;
    private float damage;
    private float lifestealPercent;
    // Determines strength of attraction to player entities
    private float playerAttractionLerpFactor;

    private float originalDelay;
    private float originalSpeed;
    private float originalRadius;
    private float originalDamage;

    private boolean randomizeAngle;
    private float minAngleInRadians;
    private float maxAngleInRadians;

    // Determines the direction of the bullet
    private AttackPartAngleDeterminant attackPartAngleDeterminant;

    // Determined by attackPartAngleDeterminant
    private float angleInRadians;

    // Color determined by HitboxTextureType since textures are generated only through HitboxTextureType values
    // null value defaults color to either PLAYER_BULLET or ENEMY_BULLET's, depending on the entity that this bullet comes from
    private RenderSystem.HitboxTextureType color = null;

    /**
     * @param originAngle - The lastFacedAngle of the parent's hitbox, in radians
     */
    public void fire(PooledEngine engine, Entity parent, Entity player, float originX, float originY, float originAngle, float mapAreaRadius) {
        RenderSystem.HitboxTextureType hitboxTextureType;

        Entity e = engine.createEntity();
        if(parent != null && Mappers.player.has(parent)) {
            e.add(engine.createComponent(PlayerBulletComponent.class).setDamage(damage).setLifestealMultiplier(parent, lifestealPercent).setPlayerAttractionLerpFactor(playerAttractionLerpFactor));
            hitboxTextureType = RenderSystem.HitboxTextureType.PLAYER_BULLET;
        } else {
            e.add(engine.createComponent(EnemyBulletComponent.class).setDamage(damage).setLifestealMultiplier(parent, lifestealPercent).setPlayerAttractionLerpFactor(playerAttractionLerpFactor));
            hitboxTextureType = RenderSystem.HitboxTextureType.ENEMY_BULLET;
        }

        float angle = 0;
        if(attackPartAngleDeterminant == AttackPartAngleDeterminant.AIM_RELATIVE_TO_PARENT_ROTATION) {
            if(randomizeAngle) {
                angle = originAngle + MathUtils.random(minAngleInRadians, maxAngleInRadians);
            } else {
                angle = originAngle + angleInRadians;
            }
        } else if(attackPartAngleDeterminant == AttackPartAngleDeterminant.AIM_AT_PLAYER) {
            Point playerPos = Mappers.hitbox.get(player).getOrigin();
            if(randomizeAngle) {
                angle = MathUtils.atan2(playerPos.y - originY, playerPos.x - originX) + MathUtils.random(minAngleInRadians, maxAngleInRadians);
            } else {
                angle = MathUtils.atan2(playerPos.y - originY, playerPos.x - originX) + angleInRadians;
            }
        } else {
            if(randomizeAngle) {
                angle = MathUtils.random(minAngleInRadians, maxAngleInRadians);
            } else {
                angle = angleInRadians;
            }
        }

        HitboxComponent hitbox = engine.createComponent(HitboxComponent.class);
        hitbox.setOrigin(originX, originY);
        hitbox.setMaxSpeed(speed);
        hitbox.setVelocity(speed * MathUtils.cos(angle), speed * MathUtils.sin(angle));
        CircleHitbox circleHitbox = new CircleHitbox();
        circleHitbox.setHitboxTextureType(hitboxTextureType);
        if(color != null) {
            circleHitbox.setColor(color);
        }
        circleHitbox.setRadius(radius);
        hitbox.addCircle(circleHitbox, true);
        e.add(hitbox);

        // Bullet expires in the time it takes to travel radius*2 of the map area if it follows the player
        if(playerAttractionLerpFactor != 0) {
            e.add(engine.createComponent(ExpirationComponent.class).setTime(mapAreaRadius * 2f / (speed * Options.GLOBAL_MOVEMENT_SPEED_MULTIPLIER)));
        }

        engine.addEntity(e);
    }

    public AttackPart clone() {
        AttackPart ap = new AttackPart();
        ap.setOriginX(originX);
        ap.setOriginY(originY);
        ap.setDelay(delay, false);
        ap.setSpeed(speed, false);
        ap.setRadius(radius, false);
        ap.setDamage(damage, false);
        ap.randomizeAngle = randomizeAngle;
        ap.minAngleInRadians = minAngleInRadians;
        ap.maxAngleInRadians = maxAngleInRadians;
        ap.attackPartAngleDeterminant = attackPartAngleDeterminant;
        ap.angleInRadians = angleInRadians;
        ap.originalDamage = originalDamage;
        ap.originalDelay = originalDelay;
        ap.originalRadius = originalRadius;
        ap.originalSpeed = originalSpeed;
        ap.lifestealPercent = lifestealPercent;
        ap.color = color;
        ap.playerAttractionLerpFactor = playerAttractionLerpFactor;
        return ap;
    }

    @Override
    public boolean equals(Object obj) {
        // Compares stats before modifications
        AttackPart ap = (AttackPart)obj;
        if(ap.originalSpeed != originalSpeed || ap.originalRadius != originalRadius || ap.originalDelay != originalDelay
                || ap.originalDamage != originalDamage || ap.originX != originX || ap.originY != originY || ap.playerAttractionLerpFactor != playerAttractionLerpFactor) return false;
        return true;
    }

    public float getPlayerAttractionLerpFactor() {
        return playerAttractionLerpFactor;
    }

    public void setPlayerAttractionLerpFactor(float playerAttractionLerpFactor) {
        this.playerAttractionLerpFactor = playerAttractionLerpFactor;
    }

    public float getDelay() {
        return delay;
    }

    public AttackPart setDelay(float delay) {
        this.delay = delay;
        originalDelay = delay;
        return this;
    }

    public AttackPart setDelay(float delay, boolean setOriginal) {
        this.delay = delay;
        if(setOriginal) {
            originalDelay = delay;
        }
        return this;
    }

    public void setColor(RenderSystem.HitboxTextureType color) {
        this.color = color;
    }

    public float getOriginX() {
        return originX;
    }

    public AttackPart setOriginX(float originX) {
        this.originX = originX;
        return this;
    }

    public float getOriginY() {
        return originY;
    }

    public AttackPart setOriginY(float originY) {
        this.originY = originY;
        return this;
    }

    public AttackPartAngleDeterminant getAttackPartAngleDeterminant() {
        return attackPartAngleDeterminant;
    }

    public AttackPart setAttackPartAngleDeterminant(AttackPartAngleDeterminant attackPartAngleDeterminant) {
        this.attackPartAngleDeterminant = attackPartAngleDeterminant;
        return this;
    }

    public float getAngleInRadians() {
        return angleInRadians;
    }

    public AttackPart setAngleInRadians(float angleInRadians) {
        this.angleInRadians = angleInRadians;
        return this;
    }

    public AttackPart setAngleInDegrees(float angleInDegrees) {
        this.angleInRadians = angleInDegrees * MathUtils.degreesToRadians;
        return this;
    }

    public AttackPart setRandomAngleInRadians(float minAngle, float maxAngle) {
        randomizeAngle = true;
        minAngleInRadians = minAngle;
        maxAngleInRadians = maxAngle;
        return this;
    }

    public AttackPart setRandomAngleInDegrees(float minAngle, float maxAngle) {
        randomizeAngle = true;
        minAngleInRadians = minAngle * MathUtils.degreesToRadians;
        maxAngleInRadians = maxAngle * MathUtils.degreesToRadians;
        return this;
    }

    public float getSpeed() {
        return speed;
    }

    public AttackPart setSpeed(float speed) {
        this.speed = speed;
        originalSpeed = speed;
        return this;
    }

    public AttackPart setSpeed(float speed, boolean setOriginal) {
        this.speed = speed;
        if(setOriginal) {
            originalSpeed = speed;
        }
        return this;
    }

    public float getRadius() {
        return radius;
    }

    public AttackPart setRadius(float radius) {
        this.radius = radius;
        originalRadius = radius;
        return this;
    }

    public AttackPart setRadius(float radius, boolean setOriginal) {
        this.radius = radius;
        if(setOriginal) {
            originalRadius = radius;
        }
        return this;
    }

    public float getDamage() {
        return damage;
    }

    public AttackPart setDamage(float damage) {
        this.damage = damage;
        originalDamage = damage;
        return this;
    }

    public AttackPart setDamage(float damage, boolean setOriginal) {
        this.damage = damage;
        if(setOriginal) {
            originalDamage = damage;
        }
        return this;
    }

    public boolean isRandomizeAngle() {
        return randomizeAngle;
    }

    public float getMinAngleInRadians() {
        return minAngleInRadians;
    }

    public float getMaxAngleInRadians() {
        return maxAngleInRadians;
    }

    public float getOriginalDelay() {
        return originalDelay;
    }

    public void setOriginalDelay(float originalDelay) {
        this.originalDelay = originalDelay;
    }

    public float getOriginalSpeed() {
        return originalSpeed;
    }

    public void setOriginalSpeed(float originalSpeed) {
        this.originalSpeed = originalSpeed;
    }

    public float getOriginalRadius() {
        return originalRadius;
    }

    public void setOriginalRadius(float originalRadius) {
        this.originalRadius = originalRadius;
    }

    public float getOriginalDamage() {
        return originalDamage;
    }

    public void setOriginalDamage(float originalDamage) {
        this.originalDamage = originalDamage;
    }

    public float getLifestealPercent() {
        return lifestealPercent;
    }

    public void setLifestealPercent(float lifestealPercent) {
        this.lifestealPercent = lifestealPercent;
    }
}
