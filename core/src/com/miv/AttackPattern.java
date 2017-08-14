package com.miv;

import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;

import map.Map;

/**
 * An attack pattern consists of multiple {@link AttackPart} that have a {@link AttackPart#delay} time value. After that much time has passed, the attack part is fired,
 * spawning a bullet. After {@link AttackPattern#duration} seconds pass, the fields in {@link utils.CircleHitbox} relevant to AttackPattern (time and which attack parts have been fired)
 * are reset.
 * Created by Miv on 5/24/2017.
 */
public class AttackPattern {
    private static final float MIN_BULLET_SPEED_CONSTANT = Map.INITIAL_MAP_AREA_PIXEL_POINTS * 0.8f;
    private static final float MAX_BULLET_SPEED_CONSTANT = Map.INITIAL_MAP_AREA_PIXEL_POINTS * 1.2f;

    private static final float MIN_FIRE_RATE_CONSTANT = Map.INITIAL_MAP_AREA_PIXEL_POINTS * 0.8f;
    private static final float MAX_FIRE_RATE_CONSTANT = Map.INITIAL_MAP_AREA_PIXEL_POINTS * 1.2f;

    private static final float MIN_BULLET_DAMAGE_CONSTANT = Map.INITIAL_MAP_AREA_PIXEL_POINTS * 0.9f;
    private static final float MAX_BULLET_DAMAGE_CONSTANT = Map.INITIAL_MAP_AREA_PIXEL_POINTS * 1.5f;

    private static final float MIN_BULLET_RADIUS_CONSTANT = Map.INITIAL_MAP_AREA_PIXEL_POINTS * 0.5f;
    private static final float MAX_BULLET_RADIUS_CONSTANT = Map.INITIAL_MAP_AREA_PIXEL_POINTS * 1.5f;

    /**
     * MUST BE IN ASCENDING ORDER BY {@link AttackPart#delay}
     */
    private ArrayList<AttackPart> attackParts;
    // Duration of attack pattern in seconds before it repeats
    private float duration;
    // Total damage of all bullets in all attack parts
    private float totalDamage;
    private float totalRadius;

    private float totalPpInStatModifiers;
    private float speedPpDivisor;
    private float fireRatePpDivisor;
    private float bulletDamagePpDivisor;
    private float bulletRadiusPpDivisor;

    public AttackPattern() {
        attackParts = new ArrayList<AttackPart>();
    }

    public AttackPattern addRandomAttackPatternStatModifiers(float pp) {
        totalPpInStatModifiers += pp;

        // First time stat modifiers are retrieved, generate random values for divisors
        // Any other time they are retrieved, use old random values but with added pp
        if(speedPpDivisor == 0) {
            speedPpDivisor = MathUtils.random(MIN_BULLET_SPEED_CONSTANT, MAX_BULLET_SPEED_CONSTANT);
            fireRatePpDivisor = MathUtils.random(MIN_FIRE_RATE_CONSTANT, MAX_FIRE_RATE_CONSTANT);
            bulletDamagePpDivisor = MathUtils.random(MIN_BULLET_DAMAGE_CONSTANT, MAX_BULLET_DAMAGE_CONSTANT);
            bulletRadiusPpDivisor = MathUtils.random(MIN_BULLET_RADIUS_CONSTANT, MAX_BULLET_RADIUS_CONSTANT);
        }
        float speedMultiplier = totalPpInStatModifiers / speedPpDivisor;
        float fireRateMultiplier = totalPpInStatModifiers / fireRatePpDivisor;
        float bulletDamageMultiplier = totalPpInStatModifiers / bulletDamagePpDivisor;
        // Bullet radius not scaled to pp
        float bulletRadiusMultiplier = (MIN_BULLET_RADIUS_CONSTANT + MAX_BULLET_RADIUS_CONSTANT) / (2f * bulletRadiusPpDivisor);

        modify(speedMultiplier, fireRateMultiplier, bulletDamageMultiplier, bulletRadiusMultiplier);

        return this;
    }

    private void modify(float speedMultiplier, float fireRateMultiplier, float bulletDamageMultiplier, float bulletRadiusMultiplier) {
        // Modify the attack pattern according to pp distribution
        for(AttackPart a : attackParts) {
            a.setSpeed(a.getOriginalSpeed() * speedMultiplier, false);
            a.setDelay(a.getOriginalDelay() * fireRateMultiplier, false);
            a.setDamage(a.getOriginalDamage() * bulletDamageMultiplier, false);
            a.setRadius(a.getOriginalRadius() * bulletRadiusMultiplier, false);
        }
    }

    public AttackPattern clone() {
        AttackPattern ap = new AttackPattern();
        ap.attackParts = new ArrayList<AttackPart>();
        for(AttackPart a : attackParts) {
            ap.attackParts.add(a.clone());
        }
        ap.duration = duration;
        ap.totalDamage = totalDamage;
        ap.totalRadius = totalRadius;
        ap.speedPpDivisor = speedPpDivisor;
        ap.bulletDamagePpDivisor = bulletDamagePpDivisor;
        ap.bulletRadiusPpDivisor = bulletRadiusPpDivisor;
        ap.fireRatePpDivisor = fireRatePpDivisor;
        ap.totalPpInStatModifiers = totalPpInStatModifiers;
        return ap;
    }

    public float getDuration() {
        return duration;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    public ArrayList<AttackPart> getAttackParts() {
        return attackParts;
    }

    public void addAttackPart(AttackPart attackPart) {
        attackParts.add(attackPart);
        totalDamage += attackPart.getDamage();
        totalRadius += attackPart.getRadius();

        // Sanity check to make sure attackParts is in correct order
        for(int i = 0; i < attackParts.size(); i++) {
            if(attackParts.get(i).getDelay() > attackPart.getDelay()) {
                System.out.println("you messed up #035230523");
            }
        }
    }

    public float getTotalDamage() {
        return totalDamage;
    }

    public float getTotalRadius() {
        return totalRadius;
    }

    public float getTotalPpInStatModifiers() {
        return totalPpInStatModifiers;
    }
}
