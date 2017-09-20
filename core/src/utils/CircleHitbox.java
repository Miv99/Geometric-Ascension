package utils;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.miv.AttackPart;
import com.miv.AttackPattern;
import com.miv.Options;

import java.util.ArrayList;

import map.Map;
import screens.PlayerBuilder;
import systems.RenderSystem;

/**
 * Created by Miv on 6/5/2017.
 */
public class CircleHitbox extends Circle {
    private static final float DAMAGE_DELTA_HEALTH_MULTIPLIER = 0.4f;
    private static final float HEALTH_DELTA_HEALTH_MULTIPLIER = 2f;
    public enum Specialization {
        // Circles shouldn't be losing maximum health upon upgrading specialization or levelling up
        // so initialHealthMultiplier is always > 1 and deltaHealthMultiplier is always > 0

        NONE(RenderSystem.HitboxTextureType.PLAYER, "None", "PLAYER_DEFAULT_1", Options.DEFAULT_NEW_CIRCLE_RADIUS, 1f, 0f, 1f, ""),
        DAMAGE(RenderSystem.HitboxTextureType.PLAYER_DAMAGE_SPECIALIZATION, "Damage", "PLAYER_DEFAULT_1", NONE,
                Options.DEFAULT_NEW_CIRCLE_RADIUS, 1f, 0f, DAMAGE_DELTA_HEALTH_MULTIPLIER,
                1f, 1f, 1.5f, 1f,
                "Sacrifices maximum health for increased damage"),
        HEALTH(RenderSystem.HitboxTextureType.PLAYER_HEALTH_SPECIALIZATION, "Health", "PLAYER_DEFAULT_1", NONE,
                Options.DEFAULT_NEW_CIRCLE_RADIUS * 1.5f, 1.25f, 0f, HEALTH_DELTA_HEALTH_MULTIPLIER,
                1f, 1f, 0.5f, 1f,
                "Sacrifices damage for increased maximum health"),
        UTILITY(RenderSystem.HitboxTextureType.PLAYER_UTILITY_SPECIALIZATION, "Utility", "PLAYER_DEFAULT_1", NONE, Options.DEFAULT_NEW_CIRCLE_RADIUS * 0.75f, 1f, 0f, 1f,
                0.5f, 0.1f,
                "Increases your maximum speed"),

        /**
         * All specializations, {@link CircleHitbox#upgrade()}, {@link AttackPattern#reapplySpecializationModifiers(CircleHitbox)},
         * {@link CircleHitbox#receiveAuraBuffs(CircleHitbox)}, and {@link PlayerBuilder#upgradeCircle(CircleHitbox)} are designed around the assumption
         * that circles upgrade specialization every 5 levels
         */

        // 30 degree spread, 8 bullets
        SHOTGUN(RenderSystem.HitboxTextureType.PLAYER_DAMAGE_SPECIALIZATION, "Shotgun", "PLAYER_SHOTGUN", DAMAGE,
                Options.DEFAULT_NEW_CIRCLE_RADIUS, 1f, 0f, DAMAGE_DELTA_HEALTH_MULTIPLIER,
                1f, 2f, 1/8f * 2.5f, 0.75f,
                "Fires 8 bullets at once with a medium spread"),
        // 45 degree spread, 12 bullets
        HIGH_SPREAD_SHOTGUN(RenderSystem.HitboxTextureType.PLAYER_DAMAGE_SPECIALIZATION, "Shotgun (L)", "PLAYER_HIGH_SPREAD_SHOTGUN", SHOTGUN,
                Options.DEFAULT_NEW_CIRCLE_RADIUS, 1f, 0f, DAMAGE_DELTA_HEALTH_MULTIPLIER,
                1.25f, 1.4f, 1/12f * 3.5f * 1.4f, 0.85f,
                "Fires 12 bullets at once with a high spread"),
        // 15 degree spread, 6 bullets
        FOCUSED_SHOTGUN(RenderSystem.HitboxTextureType.PLAYER_DAMAGE_SPECIALIZATION, "Shotgun (S)", "PLAYER_FOCUSED_SHOTGUN", SHOTGUN,
                Options.DEFAULT_NEW_CIRCLE_RADIUS, 1f, 0f, DAMAGE_DELTA_HEALTH_MULTIPLIER,
                1.5f, 2.85f, 1/6f * 3.5f, 0.5f,
                "Fires 6 bullets at once with a low spread"),
        SNIPER(RenderSystem.HitboxTextureType.PLAYER_DAMAGE_SPECIALIZATION, "Sniper", "PLAYER_SNIPER", DAMAGE,
                Options.DEFAULT_NEW_CIRCLE_RADIUS * 0.625f, 1f, 0f, DAMAGE_DELTA_HEALTH_MULTIPLIER,
                3f, 1.3f, 1.75f, 0.4f,
                "Fires a single small but fast and high-damage bullet"),
        STRONG_SNIPER(RenderSystem.HitboxTextureType.PLAYER_DAMAGE_SPECIALIZATION, "Sniper (L)", "PLAYER_STRONG_SNIPER", SNIPER,
                Options.DEFAULT_NEW_CIRCLE_RADIUS * 0.625f, 1f, 0f, DAMAGE_DELTA_HEALTH_MULTIPLIER,
                5f * 2f, 0.65f, 2.75f * 2f * 1.2f, 0.8f,
                "Increases the bullet size, speed, and damage but lowers fire rrate"),
        RAPID_SNIPER(RenderSystem.HitboxTextureType.PLAYER_DAMAGE_SPECIALIZATION, "Sniper (S)", "PLAYER_RAPID_SNIPER", SNIPER,
                Options.DEFAULT_NEW_CIRCLE_RADIUS * 0.625f, 1f, 0f, DAMAGE_DELTA_HEALTH_MULTIPLIER,
                5f * 1.3f, 2.6f, 2.75f/2f, 0.25f,
                "Fires faster at the cost of a smaller bullet size"),
        // 10 degree spread
        MACHINE_GUN(RenderSystem.HitboxTextureType.PLAYER_DAMAGE_SPECIALIZATION, "Machine gun", "PLAYER_MACHINE_GUN", DAMAGE,
                Options.DEFAULT_NEW_CIRCLE_RADIUS * 1.25f, 1f, 0f, DAMAGE_DELTA_HEALTH_MULTIPLIER,
                1.25f, 0.5f, 0.75f, 1f,
                "Rapidly fires a flurry of bullets at low spread"),
        // 20 degree spread
        UNSTABLE_MACHINE_GUN(RenderSystem.HitboxTextureType.PLAYER_DAMAGE_SPECIALIZATION, "Unstable MG", "PLAYER_UNSTABLE_MACHINE_GUN", MACHINE_GUN,
                Options.DEFAULT_NEW_CIRCLE_RADIUS * 1.25f, 1f, 0f, DAMAGE_DELTA_HEALTH_MULTIPLIER,
                1.75f, 0.35f, 1.25f, 1f,
                "Fires faster than the machine gun but increases spread"),
        // 10 degree spread, 2 bullets
        DUAL_MACHINE_GUNS(RenderSystem.HitboxTextureType.PLAYER_DAMAGE_SPECIALIZATION, "Dual MGs", "PLAYER_DUAL_MACHINE_GUNS", MACHINE_GUN,
                Options.DEFAULT_NEW_CIRCLE_RADIUS * 1.25f, 1f, 0f, DAMAGE_DELTA_HEALTH_MULTIPLIER,
                1.5f, 0.55f, 1.25f/2f, 0.7f,
                "Fires 2 bullets at once instead of 1"),

        MEAT_SHIELD(RenderSystem.HitboxTextureType.PLAYER_HEALTH_SPECIALIZATION, "Meat shield", "PLAYER_DEFAULT_1", HEALTH,
                Options.DEFAULT_NEW_CIRCLE_RADIUS * 2.5f, 5f, 5f, HEALTH_DELTA_HEALTH_MULTIPLIER,
                1f, 1f, 0.75f, 1f,
                "Greatly increases health and size\nAllows circles of other types to overlap with this one"),
        HEALTH_AURA(RenderSystem.HitboxTextureType.PLAYER_HEALTH_AURA_SPECIALIZATION, "Health aura", "PLAYER_DEFAULT_1", MEAT_SHIELD,
                Options.DEFAULT_NEW_CIRCLE_RADIUS * 2.5f + 25f, 10f, 0, HEALTH_DELTA_HEALTH_MULTIPLIER,
                1f, 1f, 0.5f, 1f,
                1.5f, 0f, 1f,
                0.15f, 0f, 0f,
                "Increases the maximum health of circles near this one"),
        LIFESTEAL(RenderSystem.HitboxTextureType.PLAYER_LIFESTEAL_SPECIALIZATION, "Lifesteal", "PLAYER_DEFAULT_1", HEALTH,
                Options.DEFAULT_NEW_CIRCLE_RADIUS, 1.25f, 0f, HEALTH_DELTA_HEALTH_MULTIPLIER,
                1f, 1f, 0.75f, 1f,
                0.05f, 0.015f,
                "Heals the weakest circle by damaging enemies"),
        LIFESTEAL_AURA(RenderSystem.HitboxTextureType.PLAYER_LIFESTEAL_AURA_SPECIALIZATION, "LS aura", "PLAYER_DEFAULT_1", LIFESTEAL,
                Options.DEFAULT_NEW_CIRCLE_RADIUS, 1.25f, 0f, HEALTH_DELTA_HEALTH_MULTIPLIER,
                1f, 1.5f, 1.5f, 1f,
                0.125f, 0f,
                1f, 0.01f, 1f,
                0f, 0.0025f, 0f,
                "Grants nearby circles a weak lifesteal"),

        THRUSTERS(RenderSystem.HitboxTextureType.PLAYER_UTILITY_SPECIALIZATION, "Thrusters", "PLAYER_DEFAULT_1", UTILITY,
                Options.DEFAULT_NEW_CIRCLE_RADIUS, 1f, 0f, 1f,
                1.5f, 0.2f,
                "Further increases your maximum speed"),
        DAMAGE_MITIGATOR_AURA(RenderSystem.HitboxTextureType.PLAYER_UTILITY_SPECIALIZATION, "Def. aura", "PLAYER_DEFAULT_1", UTILITY,
                Options.DEFAULT_NEW_CIRCLE_RADIUS, 1f, 0f, 1f,
                1f, 1f, 1f, 1f,
                1f, 0f, 0.95f,
                0f, 0f, -0.05f,
                "Grants nearby circles damage reduction, up to\na maximum of 50% damage reduction per circle");

        Array<Specialization> children = new Array<Specialization>();

        RenderSystem.HitboxTextureType hitboxTextureType;
        String stringRepresentation;
        String attackPatternName;
        Specialization parentSpecialization = null;
        String description;

        /**
         * Initial stats of a circle on specialization change
         */
        float initialRadius;
        float initialMaxHealthMultiplier;
        // Change in radius per circle level
        float deltaRadius;
        // Multiplier for the increase of maximum health per circle level
        float deltaMaxHealthMultiplier;
        /**
         * Initial attack pattern multiplier values that are set when a circle's specialization changes
         */
        float initialBulletSpeedMultiplier;
        float initialFireIntervalMultiplier;
        float initialBulletDamageMultiplier;
        float initialBulletRadiusMultiplier;

        float initialLifestealMultiplier;
        float deltaLifestealMultiplier;

        float initialMaxHealthMultiplierAura = 1f;
        float initialLifestealMultiplierAura;
        float initialDamageTakenMultiplierAura = 1f;

        float deltaMaxHealthMultiplierAura;
        float deltaLifestealMultiplierAura;
        float deltaDamageTakenMultiplierAura;

        float initialRawSpeedBoost;
        float deltaRawSpeedBoost;

        Specialization(RenderSystem.HitboxTextureType hitboxTextureType, String stringRepresentation, String attackPatternName, float initialRadius, float initialMaxHealthMultiplier, float deltaRadius, float deltaMaxHealthMultiplier, String description) {
            this.hitboxTextureType = hitboxTextureType;
            this.attackPatternName = attackPatternName;
            this.stringRepresentation = stringRepresentation;
            this.initialRadius = initialRadius;
            this.initialMaxHealthMultiplier = initialMaxHealthMultiplier;
            this.deltaRadius = deltaRadius;
            this.deltaMaxHealthMultiplier = deltaMaxHealthMultiplier;

            initialBulletSpeedMultiplier = 1f;
            initialFireIntervalMultiplier = 1f;
            initialBulletDamageMultiplier = 1f;
            initialBulletRadiusMultiplier = 1f;
            this.description = description;
        }

        Specialization(RenderSystem.HitboxTextureType hitboxTextureType, String stringRepresentation, String attackPatternName, Specialization parentSpecialization, float initialRadius, float initialMaxHealthMultiplier, float deltaRadius, float deltaMaxHealthMultiplier,
                       float initialRawSpeedBoost, float deltaRawSpeedBoost, String description) {
            parentSpecialization.children.add(this);
            this.hitboxTextureType = hitboxTextureType;
            this.attackPatternName = attackPatternName;
            this.stringRepresentation = stringRepresentation;
            this.parentSpecialization = parentSpecialization;
            this.initialRadius = initialRadius;
            this.initialMaxHealthMultiplier = initialMaxHealthMultiplier;
            this.deltaRadius = deltaRadius;
            this.deltaMaxHealthMultiplier = deltaMaxHealthMultiplier;

            this.initialRawSpeedBoost = initialRawSpeedBoost;
            this.deltaRawSpeedBoost = deltaRawSpeedBoost;

            initialBulletSpeedMultiplier = 1f;
            initialFireIntervalMultiplier = 1f;
            initialBulletDamageMultiplier = 1f;
            initialBulletRadiusMultiplier = 1f;
            this.description = description;
        }

        Specialization(RenderSystem.HitboxTextureType hitboxTextureType, String stringRepresentation, String attackPatternName, Specialization parentSpecialization, float initialRadius, float initialMaxHealthMultiplier, float deltaRadius, float deltaMaxHealthMultiplier,
                       float initialBulletSpeedMultiplier, float initialFireIntervalMultiplier, float initialBulletDamageMultiplier, float initialBulletRadiusMultiplier, String description) {
            parentSpecialization.children.add(this);
            this.hitboxTextureType = hitboxTextureType;
            this.attackPatternName = attackPatternName;
            this.initialRadius = initialRadius;
            this.initialMaxHealthMultiplier = initialMaxHealthMultiplier;
            this.deltaRadius = deltaRadius;
            this.deltaMaxHealthMultiplier = deltaMaxHealthMultiplier;
            this.stringRepresentation = stringRepresentation;
            this.parentSpecialization = parentSpecialization;
            this.initialBulletSpeedMultiplier = initialBulletSpeedMultiplier;
            this.initialFireIntervalMultiplier = initialFireIntervalMultiplier;
            this.initialBulletDamageMultiplier = initialBulletDamageMultiplier;
            this.initialBulletRadiusMultiplier = initialBulletRadiusMultiplier;
            this.description = description;
        }

        Specialization(RenderSystem.HitboxTextureType hitboxTextureType, String stringRepresentation, String attackPatternName, Specialization parentSpecialization, float initialRadius, float initialMaxHealthMultiplier, float deltaRadius, float deltaMaxHealthMultiplier,
                       float initialBulletSpeedMultiplier, float initialFireIntervalMultiplier, float initialBulletDamageMultiplier, float initialBulletRadiusMultiplier,
                       float initialLifestealMultiplier, float deltaLifestealMultiplier, String description) {
            parentSpecialization.children.add(this);
            this.hitboxTextureType = hitboxTextureType;
            this.attackPatternName = attackPatternName;
            this.initialRadius = initialRadius;
            this.initialMaxHealthMultiplier = initialMaxHealthMultiplier;
            this.deltaRadius = deltaRadius;
            this.deltaMaxHealthMultiplier = deltaMaxHealthMultiplier;
            this.stringRepresentation = stringRepresentation;
            this.parentSpecialization = parentSpecialization;
            this.initialBulletSpeedMultiplier = initialBulletSpeedMultiplier;
            this.initialFireIntervalMultiplier = initialFireIntervalMultiplier;
            this.initialBulletDamageMultiplier = initialBulletDamageMultiplier;
            this.initialBulletRadiusMultiplier = initialBulletRadiusMultiplier;
            this.initialLifestealMultiplier = initialLifestealMultiplier;
            this.deltaLifestealMultiplier = deltaLifestealMultiplier;
            this.description = description;
        }

        Specialization(RenderSystem.HitboxTextureType hitboxTextureType, String stringRepresentation, String attackPatternName, Specialization parentSpecialization, float initialRadius, float initialMaxHealthMultiplier, float deltaRadius, float deltaMaxHealthMultiplier,
                       float initialBulletSpeedMultiplier, float initialFireIntervalMultiplier, float initialBulletDamageMultiplier, float initialBulletRadiusMultiplier,
                       float initialMaxHealthMultiplierAura, float initialLifestealMultiplierAura, float initialDamageTakenMultiplierAura,
                       float deltaMaxHealthMultiplierAura, float deltaLifestealMultiplierAura, float deltaDamageTakenMultiplierAura, String description) {
            parentSpecialization.children.add(this);
            this.hitboxTextureType = hitboxTextureType;
            this.attackPatternName = attackPatternName;
            this.initialRadius = initialRadius;
            this.initialMaxHealthMultiplier = initialMaxHealthMultiplier;
            this.deltaRadius = deltaRadius;
            this.deltaMaxHealthMultiplier = deltaMaxHealthMultiplier;
            this.stringRepresentation = stringRepresentation;
            this.parentSpecialization = parentSpecialization;
            this.initialBulletSpeedMultiplier = initialBulletSpeedMultiplier;
            this.initialFireIntervalMultiplier = initialFireIntervalMultiplier;
            this.initialBulletDamageMultiplier = initialBulletDamageMultiplier;
            this.initialBulletRadiusMultiplier = initialBulletRadiusMultiplier;

            this.initialMaxHealthMultiplierAura = initialMaxHealthMultiplierAura;
            this.initialLifestealMultiplierAura = initialLifestealMultiplierAura;
            this.initialDamageTakenMultiplierAura = initialDamageTakenMultiplierAura;
            this.deltaMaxHealthMultiplierAura = deltaMaxHealthMultiplierAura;
            this.deltaLifestealMultiplierAura = deltaLifestealMultiplierAura;
            this.deltaDamageTakenMultiplierAura = deltaDamageTakenMultiplierAura;
            this.description = description;
        }

        Specialization(RenderSystem.HitboxTextureType hitboxTextureType, String stringRepresentation, String attackPatternName, Specialization parentSpecialization, float initialRadius, float initialMaxHealthMultiplier, float deltaRadius, float deltaMaxHealthMultiplier,
                       float initialBulletSpeedMultiplier, float initialFireIntervalMultiplier, float initialBulletDamageMultiplier, float initialBulletRadiusMultiplier,
                       float initialLifestealMultiplier, float deltaLifestealMultiplier,
                       float initialMaxHealthMultiplierAura, float initialLifestealMultiplierAura, float initialDamageTakenMultiplierAura,
                       float deltaMaxHealthMultiplierAura, float deltaLifestealMultiplierAura, float deltaDamageTakenMultiplierAura, String description) {
            parentSpecialization.children.add(this);
            this.hitboxTextureType = hitboxTextureType;
            this.attackPatternName = attackPatternName;
            this.initialRadius = initialRadius;
            this.initialMaxHealthMultiplier = initialMaxHealthMultiplier;
            this.deltaRadius = deltaRadius;
            this.deltaMaxHealthMultiplier = deltaMaxHealthMultiplier;
            this.stringRepresentation = stringRepresentation;
            this.parentSpecialization = parentSpecialization;
            this.initialBulletSpeedMultiplier = initialBulletSpeedMultiplier;
            this.initialFireIntervalMultiplier = initialFireIntervalMultiplier;
            this.initialBulletDamageMultiplier = initialBulletDamageMultiplier;
            this.initialBulletRadiusMultiplier = initialBulletRadiusMultiplier;
            this.initialLifestealMultiplier = initialLifestealMultiplier;
            this.deltaLifestealMultiplier = deltaLifestealMultiplier;

            this.initialMaxHealthMultiplierAura = initialMaxHealthMultiplierAura;
            this.initialLifestealMultiplierAura = initialLifestealMultiplierAura;
            this.initialDamageTakenMultiplierAura = initialDamageTakenMultiplierAura;
            this.deltaMaxHealthMultiplierAura = deltaMaxHealthMultiplierAura;
            this.deltaLifestealMultiplierAura = deltaLifestealMultiplierAura;
            this.deltaDamageTakenMultiplierAura = deltaDamageTakenMultiplierAura;
            this.description = description;
        }

        public RenderSystem.HitboxTextureType getHitboxTextureType() {
            return hitboxTextureType;
        }

        public boolean hasAura() {
            return hasLifestealAura() || hasDamageTakenAura() || hasMaxHealthAura();
        }

        public boolean hasLifestealAura() {
            return initialLifestealMultiplierAura != 0 || deltaLifestealMultiplierAura != 0;
        }

        public boolean hasDamageTakenAura() {
            return initialDamageTakenMultiplierAura != 1 || deltaDamageTakenMultiplierAura != 0;
        }

        public boolean hasMaxHealthAura() {
            return initialMaxHealthMultiplierAura != 1 || deltaMaxHealthMultiplierAura != 0;
        }

        public float getInitialRawSpeedBoost() {
            return initialRawSpeedBoost;
        }

        public float getDeltaRawSpeedBoost() {
            return deltaRawSpeedBoost;
        }

        public float getInitialLifestealMultiplier() {
            return initialLifestealMultiplier;
        }

        public float getDeltaLifestealMultiplier() {
            return deltaLifestealMultiplier;
        }

        public String getDescription() {
            return description;
        }

        public Array<Specialization> getChildren() {
            return children;
        }

        public String getAttackPatternName() {
            return attackPatternName;
        }

        public String getStringRepresentation() {
            return stringRepresentation;
        }

        public Specialization getParentSpecialization() {
            if(parentSpecialization == null) {
                return this;
            }
            return parentSpecialization;
        }

        public Specialization getRootSpecialization() {
            if(parentSpecialization == null || parentSpecialization == NONE) {
                return this;
            } else {
                Specialization sp = parentSpecialization;
                while (sp.parentSpecialization != null && sp.parentSpecialization != NONE) {
                    sp = sp.parentSpecialization;
                }
                return sp;
            }
        }

        public int getDepth() {
            if(parentSpecialization == null) {
                return 0;
            } else {
                int depth = 0;
                Specialization sp = parentSpecialization;
                while (sp.parentSpecialization != null) {
                    sp = sp.parentSpecialization;
                    depth++;
                }
                return depth;
            }
        }

        public float getInitialBulletSpeedMultiplier() {
            return initialBulletSpeedMultiplier;
        }

        public float getInitialFireIntervalMultiplier() {
            return initialFireIntervalMultiplier;
        }

        public float getInitialBulletDamageMultiplier() {
            return initialBulletDamageMultiplier;
        }

        public float getInitialBulletRadiusMultiplier() {
            return initialBulletRadiusMultiplier;
        }
    }

    private RenderSystem.HitboxTextureType hitboxTextureType;
    // Max health before multipliers from specialization
    private float baseMaxHealth;
    private float maxHealth;
    private float health;
    // How much extra speed is added to the parent entity
    private float speedBoost;
    private AttackPattern attackPattern;
    // Time in seconds since the last iteration of the attack pattern started
    private transient float time;
    // Original circle position with the hitbox facing angle 0
    // Used to prevent inaccuracies when rotating hitbox multiple times
    // Must be in the same order as circles
    // Set by HitboxComponent
    private float originalPosX;
    private float originalPosY;

    // Used by player builder
    private transient float unsavedCreationCost;
    private transient float unsavedUpgradeCost;
    private transient boolean invalidPosition;

    // Used for player builder
    private Specialization specialization = Specialization.NONE;
    private int level;
    private boolean specializationAvailable;

    // Total pp put into upgrades
    private float totalUpgradesPp;

    // Buffs from auras
    private float damageTakenMultiplierFromAura = 1f;
    private float lifestealMultiplierFromAura;
    private float maxHealthMultiplierFromAura = 1f;

    // Color determined by HitboxTextureType since textures are generated only through HitboxTextureType values
    // null value defaults color to hitboxTextureType's
    private RenderSystem.HitboxTextureType color = null;

    // How much pp the player gains by killing this circle
    private float ppGain;

    public CircleHitbox() {}

    public CircleHitbox(RenderSystem.HitboxTextureType textureType, AttackPattern attackPattern, float x, float y, float radius, float health, float ppGain) {
        setHitboxTextureType(textureType);
        setAttackPattern(attackPattern);
        setPosition(x, y);
        setRadius(radius);
        setMaxHealth(health);
        setHealth(health);
        setPpGain(ppGain);
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

    /**
     * @param pp amount of pp put into healing
     * @return the leftover pp
     */
    public float heal(float pp) {
        float missing = getMaxHealth() - getHealth();
        float ppCost = missing/Options.HEALTH_PER_PP_HEALING_COST_RATIO;
        if(pp >= ppCost) {
            setHealth(getMaxHealth());
            return pp - ppCost;
        } else {
            setHealth(getHealth() + pp * Options.HEALTH_PER_PP_HEALING_COST_RATIO);
            return 0;
        }
    }

    public CircleHitbox clone() {
        CircleHitbox c = new CircleHitbox();
        c.x = x;
        c.y = y;
        c.radius = radius;
        c.setHitboxTextureType(hitboxTextureType);
        c.setBaseMaxHealth(baseMaxHealth);
        c.setMaxHealth(maxHealth);
        c.setHealth(health);
        if(attackPattern != null) {
            c.setAttackPattern(attackPattern.clone());
        }
        c.setTime(time);
        c.setOriginalPosX(originalPosX);
        c.setOriginalPosY(originalPosY);
        c.setPpGain(ppGain);
        c.setUnsavedCreationCost(unsavedCreationCost);
        c.setSpecializationAvailable(specializationAvailable);
        c.specialization = specialization;
        c.level = level;
        c.totalUpgradesPp = totalUpgradesPp;
        c.damageTakenMultiplierFromAura = damageTakenMultiplierFromAura;
        c.lifestealMultiplierFromAura = lifestealMultiplierFromAura;
        c.maxHealthMultiplierFromAura = maxHealthMultiplierFromAura;
        c.color = color;
        return c;
    }

    public void removeAuraBuffs() {
        damageTakenMultiplierFromAura = 1f;
        lifestealMultiplierFromAura = 0f;
        maxHealthMultiplierFromAura = 1f;
    }

    public void receiveAuraBuffs(CircleHitbox source) {
        Specialization sp = source.getSpecialization();

        if(sp.hasDamageTakenAura()) {
            damageTakenMultiplierFromAura -= (1f - (sp.initialDamageTakenMultiplierAura + ((source.level - (sp.getDepth() * 5)) * sp.deltaDamageTakenMultiplierAura)));
            damageTakenMultiplierFromAura = Math.max(damageTakenMultiplierFromAura, Options.MIN_DAMAGE_TAKEN_MULTIPLIER);
        }

        lifestealMultiplierFromAura += sp.initialLifestealMultiplierAura + ((source.level - (sp.getDepth() * 5)) * sp.deltaLifestealMultiplierAura);

        maxHealthMultiplierFromAura += (sp.initialMaxHealthMultiplierAura + ((source.level - (sp.getDepth() * 5)) * sp.deltaMaxHealthMultiplierAura)) - 1f;
    }

    public String getAttackPatternFormattedStats() {
        if(attackPattern == null) {
            return "No weapon equipped";
        } else {
            return attackPattern.getStringFormattedStats();
        }
    }

    public String getStringFormattedStats() {
        String s = "Level: " + (level + 1) + "\n"
                + "Specialization: " + specialization.getStringRepresentation() + "\n"
                + "Max health: " + (int)Math.ceil(maxHealth) + "\n";
        if(maxHealthMultiplierFromAura != 1) {
            s += "  " + PlayerBuilder.formatNumber(maxHealthMultiplierFromAura) + "x from aura(s)\n";
        }
        s += "Health: " + (int)Math.ceil(health) + " (" + String.format("%.1f", (health/maxHealth) * 100f) + "%)\n"
                + "Radius: " + (int)radius + "\n";
        if(speedBoost > 0) {
            s += "Speed: +" + PlayerBuilder.formatNumber(speedBoost) + "\n";
        } else if(speedBoost < 0) {
            s += "Speed: " + PlayerBuilder.formatNumber(speedBoost) + "\n";
        }
        if(attackPattern != null && attackPattern.getLifestealMultiplier() != 0) {
            s += "Lifesteal: " + PlayerBuilder.formatNumber((attackPattern.getLifestealMultiplier() + lifestealMultiplierFromAura) * 100f) + "%\n";
            if(lifestealMultiplierFromAura != 0) {
                s += "  " + PlayerBuilder.formatNumber(lifestealMultiplierFromAura * 100f) + "%  from aura(s)\n";
            }
        } else if(attackPattern != null && lifestealMultiplierFromAura != 0) {
            s += "\nLifesteal from aura(s): " + PlayerBuilder.formatNumber((attackPattern.getLifestealMultiplier() + lifestealMultiplierFromAura) * 100f) + "%\n";
            if(damageTakenMultiplierFromAura != 1) {
                s += "Def. from aura(s): " + PlayerBuilder.formatNumber(Math.min(1f/damageTakenMultiplierFromAura, 1f/Options.MIN_DAMAGE_TAKEN_MULTIPLIER)) + "x\n";
            }
        } else if(damageTakenMultiplierFromAura != 1) {
            s += "\nDef. from aura(s): " + PlayerBuilder.formatNumber(Math.min(1f/damageTakenMultiplierFromAura, 1f/Options.MIN_DAMAGE_TAKEN_MULTIPLIER)) + "x\n";
        }


        if(specialization.hasAura()) {
            s += "\n";
        }
        if(specialization.hasMaxHealthAura()) {
            s += "Emitting " + PlayerBuilder.formatNumber(specialization.initialMaxHealthMultiplierAura + ((level - (specialization.getDepth() * 5)) * specialization.deltaMaxHealthMultiplierAura)) + "x health aura\n";
        }
        if(specialization.hasDamageTakenAura()) {
            s += "Emitting " + PlayerBuilder.formatNumber(1f/(specialization.initialDamageTakenMultiplierAura + ((level - (specialization.getDepth() * 5)) * specialization.deltaDamageTakenMultiplierAura))) + "x def. aura\n";
        }
        if(specialization.hasLifestealAura()) {
            s += "Emitting " + PlayerBuilder.formatNumber((specialization.initialLifestealMultiplierAura + ((level - (specialization.getDepth() * 5)) * specialization.deltaLifestealMultiplierAura)) * 100f) + "% lifesteal aura\n";
        }

        s += "\nNext upgrade cost: " + PlayerBuilder.formatNumber(calculateNextUpgradeCost()) + "pp\n";

        return s;
    }

    public float calculateNextUpgradeCost() {
        return (float)Math.pow(level + 1, Options.CIRCLE_UPGRADE_EXPONENT) + Map.INITIAL_MAP_AREA_PIXEL_POINTS/2f;
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
        if(this.attackPattern != null) {
            this.attackPattern.transferEssentialFieldsTo(attackPattern);
        }

        this.attackPattern = attackPattern;
        if(attackPattern != null) {
            fired = new boolean[attackPattern.getAttackParts().size()];
            attackPattern.reapplySpecializationModifiers(this);
        }
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

    public int getLevel() {
        return level;
    }

    public void upgrade() {
        totalUpgradesPp += calculateNextUpgradeCost();
        unsavedUpgradeCost += calculateNextUpgradeCost();

        level++;

        // Increase stats
        float newRadius = radius;
        if(specialization != null) {
            newRadius = specialization.initialRadius + ((level - (specialization.getDepth() * 5)) * specialization.deltaRadius);
        }
        setBaseMaxHealth(baseMaxHealth + (Options.CIRCLE_DELTA_MAX_HEALTH * specialization.deltaMaxHealthMultiplier));
        setRadius(newRadius);
        setSpeedBoost(specialization.initialRawSpeedBoost + ((level - (specialization.getDepth() * 5)) * specialization.deltaRawSpeedBoost));

        if((level + 1) % 5 == 0 && specialization.getChildren().size > 0) {
            specializationAvailable = true;
        }

        if(attackPattern != null) {
            attackPattern.reapplySpecializationModifiers(this);
        }
    }

    public void changeSpecialization(Specialization newSpecialization) {
        specializationAvailable = false;

        this.specialization = newSpecialization;
        setBaseMaxHealth(baseMaxHealth);
        setRadius(newSpecialization.initialRadius);
        setSpeedBoost(specialization.initialRawSpeedBoost + ((level - (specialization.getDepth() * 5)) * specialization.deltaRawSpeedBoost));

        if(attackPattern != null) {
            attackPattern.reapplySpecializationModifiers(this);
        }
        setColor(newSpecialization.getHitboxTextureType());
    }

    public void upgradeAttackPattern() {
        totalUpgradesPp += attackPattern.calculateNextUpgradeCost();
        unsavedUpgradeCost += attackPattern.calculateNextUpgradeCost();
        attackPattern.upgrade(this);
    }

    public float getTotalUpgradesPp() {
        return totalUpgradesPp;
    }

    public Specialization getSpecialization() {
        return specialization;
    }

    public float getSpeedBoost() {
        return speedBoost;
    }

    public void setSpeedBoost(float speedBoost) {
        this.speedBoost = speedBoost;
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(float maxHealth) {
        if(maxHealth > this.maxHealth) {
            float difference = this.maxHealth - health;
            this.maxHealth = maxHealth;
            setHealth(maxHealth + difference);
        } else {
            this.maxHealth = maxHealth;
        }

        if(health > this.maxHealth) {
            health = this.maxHealth;
        }
    }

    public float getHealth() {
        return health;
    }

    public void setHealth(float health) {
        this.health = health;
        if(this.health > maxHealth) {
            this.health = maxHealth;
        }
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

    public float getTotalHealingCostInPp() {
        return (maxHealth - health)/ Options.HEALTH_PER_PP_HEALING_COST_RATIO;
    }

    public float getUnsavedCreationCost() {
        return unsavedCreationCost;
    }

    public void setUnsavedCreationCost(float unsavedCreationCost) {
        this.unsavedCreationCost = unsavedCreationCost;
    }

    public boolean isInvalidPosition() {
        return invalidPosition;
    }

    public void setInvalidPosition(boolean invalidPosition) {
        this.invalidPosition = invalidPosition;
    }

    public float getUnsavedUpgradeCost() {
        return unsavedUpgradeCost;
    }

    public void setUnsavedUpgradeCost(float unsavedUpgradeCost) {
        this.unsavedUpgradeCost = unsavedUpgradeCost;
    }

    public float getBaseMaxHealth() {
        return baseMaxHealth;
    }

    public void setBaseMaxHealth(float baseMaxHealth) {
        this.baseMaxHealth = baseMaxHealth;
        if(specialization == null) {
            setMaxHealth(baseMaxHealth * (maxHealthMultiplierFromAura));
        } else {
            setMaxHealth(baseMaxHealth * (specialization.initialMaxHealthMultiplier + maxHealthMultiplierFromAura));
        }
    }

    public boolean isSpecializationAvailable() {
        return specializationAvailable;
    }

    public void setSpecializationAvailable(boolean specializationAvailable) {
        this.specializationAvailable = specializationAvailable;
    }

    public void setColor(RenderSystem.HitboxTextureType color) {
        this.color = color;
    }

    public RenderSystem.HitboxTextureType getColor() {
        return color;
    }
}