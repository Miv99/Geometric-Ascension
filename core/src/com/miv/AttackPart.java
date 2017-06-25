package com.miv;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

import components.EnemyBulletComponent;
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

    // For radius scaling with damage
    private static final float LOWER_RADIUS = 10f;
    private static final float UPPER_RADIUS = 35f;
    private static final float LOWER_DAMAGE = 10f;
    private static final float UPPER_DAMAGE = 30f;

    // Time in seconds relative to the start of the attack pattern until this attack part is fired
    private float delay;
    // Name of bullet's sprite
    private String bulletSpriteName;
    // Origin of the bullet with respect to the origin of the parent entity
    private float originX;
    private float originY;
    // Speed of bullet
    private float speed;
    private float radius;
    private float damage;

    // Determines the direction of the bullet
    private AttackPartAngleDeterminant attackPartAngleDeterminant;

    // Determined by attackPartAngleDeterminant
    private float angleInRadians;

    /**
     * @param originAngle - The lastFacedAngle of the parent's hitbox, in radians
     */
    public void fire(PooledEngine engine, Entity parent, Entity player, float originX, float originY, float originAngle) {
        Color color = null;

        Entity e = engine.createEntity();
        if(Mappers.player.has(parent)) {
            e.add(engine.createComponent(PlayerBulletComponent.class).setDamage(damage));
            color = RenderSystem.PLAYER_BULLET_COLOR;
        } else if(Mappers.enemy.has(parent)) {
            e.add(engine.createComponent(EnemyBulletComponent.class).setDamage(damage));
            color = RenderSystem.ENEMY_BULLET_COLOR;
        }

        float angle = 0;
        if(attackPartAngleDeterminant == AttackPartAngleDeterminant.AIM_RELATIVE_TO_PARENT_ROTATION) {
            angle = originAngle + angleInRadians;
        } else if(attackPartAngleDeterminant == AttackPartAngleDeterminant.AIM_AT_PLAYER) {
            Point playerPos = Mappers.hitbox.get(player).getOrigin();
            angle = MathUtils.atan2(playerPos.y - originY, playerPos.x - originX) + angleInRadians;
        } else {
            angle = angleInRadians;
        }

        HitboxComponent hitbox = engine.createComponent(HitboxComponent.class);
        hitbox.setOrigin(originX, originY);
        hitbox.setMaxSpeed(speed);
        hitbox.setVelocity(speed * MathUtils.cos(angle), -speed * MathUtils.sin(angle));
        CircleHitbox circleHitbox = new CircleHitbox();
        circleHitbox.setColor(color);
        circleHitbox.setRadius(radius);
        hitbox.addCircle(circleHitbox);
        e.add(hitbox);

        engine.addEntity(e);
    }

    /**
     * Used mostly for enemy bullets
     */
    public AttackPart scaleRadiusToDamage() {
        setRadius(((damage - LOWER_DAMAGE)/(UPPER_DAMAGE - LOWER_DAMAGE)) * (UPPER_RADIUS - LOWER_RADIUS) + LOWER_RADIUS);
        return this;
    }

    public float getDelay() {
        return delay;
    }

    public AttackPart setDelay(float delay) {
        this.delay = delay;
        return this;
    }

    public String getBulletSpriteName() {
        return bulletSpriteName;
    }

    public AttackPart setBulletSpriteName(String bulletSpriteName) {
        this.bulletSpriteName = bulletSpriteName;
        return this;
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

    public float getSpeed() {
        return speed;
    }

    public AttackPart setSpeed(float speed) {
        this.speed = speed;
        return this;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public float getDamage() {
        return damage;
    }

    public AttackPart setDamage(float damage) {
        this.damage = damage;
        return this;
    }
}
