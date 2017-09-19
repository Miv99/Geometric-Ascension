package com.miv;

import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;

import map.Map;
import screens.PlayerBuilder;
import utils.CircleHitbox;
import utils.Utils;

/**
 * An attack pattern consists of multiple {@link AttackPart} that have a {@link AttackPart#delay} time value. After that much time has passed, the attack part is fired,
 * spawning a bullet. After {@link AttackPattern#duration} seconds pass, the fields in {@link utils.CircleHitbox} relevant to AttackPattern (time and which attack parts have been fired)
 * are reset.
 * Created by Miv on 5/24/2017.
 */
public class AttackPattern {
    private static final float MIN_BULLET_SPEED_MULTIPLIER = 0.8f;
    private static final float MAX_BULLET_SPEED_MULTIPLIER = 1.2f;

    private static final float MIN_FIRE_INTERVAL_MULTIPLIER = 0.4f;
    private static final float MAX_FIRE_INTERVAL_MULTIPLIER = 0.8f;

    private static final float MIN_BULLET_DAMAGE_MULTIPLIER = 0.9f;
    private static final float MAX_BULLET_DAMAGE_MULTIPLIER = 1.5f;

    private static final float MIN_BULLET_RADIUS_MULTIPLIER = 0.5f;
    private static final float MAX_BULLET_RADIUS_MULTIPLIER = 1.5f;

    /**
     * MUST BE IN ASCENDING ORDER BY {@link AttackPart#delay}
     */
    private ArrayList<AttackPart> attackParts;
    // Duration of attack pattern in seconds before it repeats
    private float duration;
    private float originalDuration;
    // Total damage of all bullets in all attack parts
    private float totalDamage;
    private float totalRadius;
    // In radians
    private float angleOffset;

    // Used for info display in player builder
    private String type;
    private int level;

    private float totalPpInStatModifiers;
    private float speedPpMultiplier;
    private float fireIntervalPpMultiplier;
    private float bulletDamagePpMultiplier;
    private float bulletRadiusPpMultiplier;

    private float lifestealMultiplier;

    public AttackPattern() {
        attackParts = new ArrayList<AttackPart>();
    }

    public AttackPattern addRandomAttackPatternStatModifiers(float pp) {
        totalPpInStatModifiers += pp;

        // First time stat modifiers are retrieved, generate random values for divisors
        // Any other time they are retrieved, use old random values but with added pp
        if(speedPpMultiplier == 0) {
            speedPpMultiplier = MathUtils.random(MIN_BULLET_SPEED_MULTIPLIER, MAX_BULLET_SPEED_MULTIPLIER);
            fireIntervalPpMultiplier = MathUtils.random(MIN_FIRE_INTERVAL_MULTIPLIER, MAX_FIRE_INTERVAL_MULTIPLIER);
            bulletDamagePpMultiplier = MathUtils.random(MIN_BULLET_DAMAGE_MULTIPLIER, MAX_BULLET_DAMAGE_MULTIPLIER);
            bulletRadiusPpMultiplier = MathUtils.random(MIN_BULLET_RADIUS_MULTIPLIER, MAX_BULLET_RADIUS_MULTIPLIER);
        }
        calculateAndSetModificationMultipliers();

        return this;
    }

    private void calculateAndSetModificationMultipliers() {
        float fireIntervalMultiplier = totalPpInStatModifiers / Map.INITIAL_MAP_AREA_PIXEL_POINTS * fireIntervalPpMultiplier;
        float bulletDamageMultiplier = totalPpInStatModifiers / Map.INITIAL_MAP_AREA_PIXEL_POINTS * bulletDamagePpMultiplier;
        // Bullet radius and speed not scaled to pp
        float bulletRadiusMultiplier = bulletRadiusPpMultiplier;
        float speedMultiplier = speedPpMultiplier;

        modify(speedMultiplier, fireIntervalMultiplier, bulletDamageMultiplier, bulletRadiusMultiplier);
    }

    private void modify(float speedMultiplier, float fireIntervalMultiplier, float bulletDamageMultiplier, float bulletRadiusMultiplier) {
        // Modify the attack pattern according to pp distribution
        for(AttackPart a : attackParts) {
            a.setSpeed(a.getOriginalSpeed() * speedMultiplier, false);
            a.setDelay(a.getOriginalDelay() * fireIntervalMultiplier, false);
            a.setDamage(a.getOriginalDamage() * bulletDamageMultiplier, false);
            a.setRadius(a.getOriginalRadius() * bulletRadiusMultiplier, false);
        }
        duration = originalDuration * fireIntervalMultiplier;
    }

    private void modify(float speedMultiplier, float fireIntervalMultiplier, float bulletDamageMultiplier, float bulletRadiusMultiplier, float lifestealMultiplier) {
        // Modify the attack pattern according to pp distribution
        for(AttackPart a : attackParts) {
            a.setSpeed(a.getOriginalSpeed() * speedMultiplier, false);
            a.setDelay(a.getOriginalDelay() * fireIntervalMultiplier, false);
            a.setDamage(a.getOriginalDamage() * bulletDamageMultiplier, false);
            a.setRadius(a.getOriginalRadius() * bulletRadiusMultiplier, false);
            a.setLifestealPercent(lifestealMultiplier);
        }
        duration = originalDuration * fireIntervalMultiplier;
        this.lifestealMultiplier = lifestealMultiplier;
    }

    public AttackPattern clone() {
        AttackPattern ap = new AttackPattern();
        ap.attackParts = new ArrayList<AttackPart>();
        for(AttackPart a : attackParts) {
            ap.attackParts.add(a.clone());
        }
        ap.duration = duration;
        ap.originalDuration = originalDuration;
        ap.totalDamage = totalDamage;
        ap.totalRadius = totalRadius;
        ap.angleOffset = angleOffset;
        ap.speedPpMultiplier = speedPpMultiplier;
        ap.bulletDamagePpMultiplier = bulletDamagePpMultiplier;
        ap.bulletRadiusPpMultiplier = bulletRadiusPpMultiplier;
        ap.fireIntervalPpMultiplier = fireIntervalPpMultiplier;
        ap.totalPpInStatModifiers = totalPpInStatModifiers;
        ap.level = level;
        ap.type = type;
        ap.lifestealMultiplier = lifestealMultiplier;
        return ap;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) return false;
        // Compares stats before modifications
        AttackPattern ap = (AttackPattern)obj;
        if(ap.duration != duration || ap.totalDamage != totalDamage || ap.totalRadius != totalRadius || attackParts.size() != ap.attackParts.size()) return false;
        for(int i = 0; i < attackParts.size(); i++) {
            AttackPart a1 = attackParts.get(i);
            AttackPart a2 = ap.attackParts.get(i);
            if(!a1.equals(a2)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns stats in a readable format.
     */
    public String getStringFormattedStats() {
        String s = "Level: " + (level + 1) + "\n"
                + "Type: " + type + "\n"
                + "DPS: " + PlayerBuilder.formatNumber(getDPS()) + "\n"
                + "Attack speed: " + PlayerBuilder.formatNumber(1f/duration) + "\n"
                + "Bullet speed: " + PlayerBuilder.formatNumber(getAverageBulletSpeed()) + "\n"
                + "Bullet size: " + PlayerBuilder.formatNumber(getAverageBulletRadius()) + "\n"
                + "\nOffset: " + (int)(Math.round(Utils.normalizeAngleIn180Range(angleOffset) * MathUtils.radiansToDegrees)) + " degrees\n"
                + "\nNext upgrade cost: " + PlayerBuilder.formatNumber(calculateNextUpgradeCost()) + "pp\n";
        return s;
    }

    public float getAverageBulletSpeed() {
        float avg = 0f;
        for(AttackPart ap : attackParts) {
            avg += ap.getSpeed();
        }
        return avg/attackParts.size();
    }

    public float getAverageBulletRadius() {
        float avg = 0f;
        for(AttackPart ap : attackParts) {
            avg += ap.getRadius();
        }
        return avg/attackParts.size();
    }

    public float getDPS() {
        float totalDamage = 0f;
        for(AttackPart ap : attackParts) {
            totalDamage += ap.getDamage();
        }
        return totalDamage/duration;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getLevel() {
        return level;
    }

    public void reapplySpecializationModifiers(CircleHitbox parent) {
        CircleHitbox.Specialization specialization = parent.getSpecialization();

        // Reapply modifiers
        modify(specialization.getInitialBulletSpeedMultiplier(), specialization.getInitialFireIntervalMultiplier(),
                specialization.getInitialBulletDamageMultiplier(), specialization.getInitialBulletRadiusMultiplier(),
                specialization.getInitialLifestealMultiplier() + ((parent.getLevel() - (specialization.getDepth() * 5)) * specialization.getDeltaLifestealMultiplier()));

        // Change colors
        for(AttackPart ap : attackParts) {
            ap.setColor(specialization.getHitboxTextureType());
        }
    }

    /**
     * Used for when a circle changes attack pattern but needs to
     * keep some fields from the original attack pattern
     */
    public void transferEssentialFieldsTo(AttackPattern target) {
        target.angleOffset = angleOffset;
    }

    public void upgrade(CircleHitbox parent) {
        level++;

        // Increase stats
        for(AttackPart a : attackParts) {
            a.setOriginalSpeed(a.getOriginalSpeed() + Options.ATTACK_PATTERN_DELTA_PLAYER_BULLET_SPEED);
            a.setOriginalDelay(a.getOriginalDelay() * Options.ATTACK_PATTERN_DELTA_PLAYER_FIRE_INTERVAL_MULTIPLIER);
            a.setOriginalDamage(a.getOriginalDamage() + Options.ATTACK_PATTERN_DELTA_PLAYER_DAMAGE);
            a.setOriginalRadius(a.getOriginalRadius() + Options.ATTACK_PATTERN_DELTA_PLAYER_BULLET_RADIUS);
        }
        originalDuration *=  Options.ATTACK_PATTERN_DELTA_PLAYER_FIRE_INTERVAL_MULTIPLIER;

        reapplySpecializationModifiers(parent);
    }

    public float calculateNextUpgradeCost() {
        return (float)Math.pow(level + 1, Options.ATTACK_PATTERN_UPGRADE_EXPONENT) + Map.INITIAL_MAP_AREA_PIXEL_POINTS/2f;
    }

    public float getAngleOffset() {
        return angleOffset;
    }

    public float getDuration() {
        return duration;
    }

    public void setDuration(float duration, boolean setOriginal) {
        this.duration = duration;
        if(setOriginal) {
            originalDuration = duration;
        }
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

    public float getSpeedPpMultiplier() {
        return speedPpMultiplier;
    }

    public float getFireIntervalPpMultiplier() {
        return fireIntervalPpMultiplier;
    }

    public float getBulletDamagePpMultiplier() {
        return bulletDamagePpMultiplier;
    }

    public float getBulletRadiusPpMultiplier() {
        return bulletRadiusPpMultiplier;
    }

    public float getLifestealMultiplier() {
        return lifestealMultiplier;
    }

    public void setAngleOffset(float angleOffset) {
        this.angleOffset = angleOffset;
    }
}
