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

    public AttackPattern() {
        attackParts = new ArrayList<AttackPart>();
    }

    // Randomly distribute pixel points to various aspects of attack pattern bullets
    private float[] getAttackPatternStatModifiers(float pp) {
        float speedMultiplier = pp/MathUtils.random(MIN_BULLET_SPEED_CONSTANT, MAX_BULLET_SPEED_CONSTANT);
        float fireRateMultiplier = pp/MathUtils.random(MIN_FIRE_RATE_CONSTANT, MAX_FIRE_RATE_CONSTANT);
        float bulletDamageMultiplier = pp/MathUtils.random(MIN_BULLET_DAMAGE_CONSTANT, MAX_BULLET_DAMAGE_CONSTANT);
        float bulletRadiusMultiplier = pp/MathUtils.random(MIN_BULLET_RADIUS_CONSTANT, MAX_BULLET_RADIUS_CONSTANT);

        return new float[] {speedMultiplier, fireRateMultiplier, bulletDamageMultiplier, bulletRadiusMultiplier};
    }

    public void addRandomAttackPatternStatModifiers(float pp) {
        totalPpInStatModifiers += pp;

        float[] sm = getAttackPatternStatModifiers(totalPpInStatModifiers);
        modify(sm[0], sm[1], sm[2], sm[3]);
    }

    /**
     * @see {@link AttackPattern#getAttackPatternStatModifiers(float)}
     */
    private void modify(float speedMultiplier, float fireRateMultiplier, float bulletDamageMultiplier, float bulletRadiusMultiplier) {
        // Modify the attack pattern according to pp distribution
        for(AttackPart a : attackParts) {
            a.setSpeed(a.getOriginalSpeed() * speedMultiplier);
            a.setDelay(a.getOriginalDelay() * fireRateMultiplier);
            a.setDamage(a.getOriginalDamage() * bulletDamageMultiplier);
            a.setRadius(a.getOriginalRadius() * bulletRadiusMultiplier);
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
