package com.miv;

import map.Map;

/**
 * Created by Miv on 6/9/2017.
 */
public class Options {
    // Change in a player bullet's raw stat when attack pattern is levelled up
    // Multipliers not taken into account yet
    public static final float ATTACK_PATTERN_DELTA_PLAYER_BULLET_SPEED = 0.5f;
    public static final float ATTACK_PATTERN_DELTA_PLAYER_FIRE_INTERVAL_MULTIPLIER = 0.983f;
    public static final float ATTACK_PATTERN_DELTA_PLAYER_DAMAGE = 0.5f;
    public static final float ATTACK_PATTERN_DELTA_PLAYER_BULLET_RADIUS = 1f;
    // Change in a player circle's raw stat when it is levelled up
    public static final float CIRCLE_DELTA_MAX_HEALTH = 10f;
    public static final float PLAYER_BASE_MAX_SPEED = 5f;

    // Determines growth of cost of upgrading a circle
    public static final float CIRCLE_UPGRADE_EXPONENT = 1.6f;
    public static final float ATTACK_PATTERN_UPGRADE_EXPONENT = 1.45f;
    // Determines growth of cost of adding a circle in player builder
    public static final float CIRCLE_CREATION_EXPONENT = 1.4f;
    // Determines amount of pp returned from deleting a circle
    public static final float CIRCLE_DELETION_PP_RETURN_MULTIPLIER = 0.5f;

    // Maximum distance from an aura source's circumference that another circle can be before it stops receiving aura buffs
    public static final float CIRCLE_AURA_RANGE = 20f;
    public static final float MIN_DAMAGE_TAKEN_MULTIPLIER = 0.5f;

    // Determines amount of pp enemies give
    public static final float PP_GAIN_MULTIPLIER = 1.3f/Map.INITIAL_MAP_AREA_PIXEL_POINTS;
    // Affects bonus pp gain from clearing a map area
    public static final float BONUS_PP_MULTIPLIER = 0.5f;
    //TODO: change this for hard mode
    public static final float GLOBAL_MOVEMENT_SPEED_MULTIPLIER = 30f;
    // How much a boss attack pattern's pp in stat modifiers is split evenly among other bosses' attack pattern's stat modifiers upon circle death
    public static final float BOSS_PP_TRANSFER_PERCENT = 0.35f;
    // How much health is healed per pp spent
    public static final float HEALTH_PER_PP_HEALING_COST_RATIO = 15f;
    /**
     * The minimum distance from an entity's origin to another's hitbox's {@link components.HitboxComponent#gravitationalRadius} and/or map area border that the repelling force begins to take effect.
     * The repelling force works in {@link systems.MovementSystem} by adding to each hitbox's velocity, with the amount added increasing
     * exponentially as distance between the two hitbox origins decreases. The magnitude of the velocity added is capped at GRAVITY_SPEED_CAP meters/frame.
     */
    public static final float GRAVITY_DROP_OFF_DISTANCE = 500f;
    // Distance from the edge of the farthest CircleHitbox in a hitbox from the origin that the repelling forces of gravity take maximum effect
    public static final float GRAVITATIONAL_RADIUS_PADDING = 10f;
    public static final float GRAVITY_SPEED_CAP = 100f;
    public static final float GRAVITATIONAL_CONSTANT = 1600f;
    // How many times stronger gravity is for pp orbs than for other objects
    public static final float PP_ORB_GRAVITATIONAL_CONSTANT_MULTIPLIER = 5f;

    public static final float BULLET_BUBBLE_POP_VOLUME = 1.5f;
    public static final float PLAYER_BUBBLE_POP_VOLUME = 3f;
    public static final float ENEMY_BUBBLE_POP_VOLUME = 3f;

    // -----------------------------------------------------------------------------Player customization------------------------------------------------------
    public static final float INITIAL_PLAYER_CUSTOMIZATION_RADIUS = 250f;
    public static final float DEFAULT_NEW_CIRCLE_RADIUS = 40f;
    public static final float DEFAULT_NEW_CIRCLE_MAX_HEALTH = 25f;

    // How much max health is gained per pp put into the stat when customizing player
    // Should be less than HEALTH_PER_PP_HEALING_COST_RATIO
    public static final float MAX_HEALTH_PER_PP_CUSTOMIZATION_COST_RATIO = 2f;
    // How much radius changes by (in terms of +/- % of original) per pp put into the stat
    public static final float RADIUS_PERCENT_PER_PP_CUSTOMIZATION_COST_RATIO = 3f;
    // --------------------------------------------------------------------------------------------------------------------------------------------------------


    // Strings used for storing corresponding values inside Preference files
    public static final String MUSIC_VOLUME_STRING = "musicVolume";
    public static final String SOUND_VOLUME_STRING = "soundVolume";
    public static final String MASTER_VOLUME_STRING = "masterVolume";
    public static final String MOVEMENT_DRAG_ARROW_MAX_DISTANCE_STRING = "movementDragArrowMaxDistance";
    public static final String SHOW_PLAYER_HEALTH_BARS_STRING = "showPlayerHealthBars";
    public static final String SHOW_ENEMY_HEALTH_BARS_STRING = "showEnemyHealthBars";
    public static final String SHOW_PP_GAIN_FLOATING_TEXT_STRING = "showPpGainFloatingText";

    public static float MUSIC_VOLUME = 0.5f;
    public static float SOUND_VOLUME = 0.5f;
    public static float MASTER_VOLUME = 0.5f;
    // Maximum distance dragged on the screen before the player reaches maximum speed
    public static float MOVEMENT_DRAG_ARROW_MAX_DISTANCE = 50f;
    // Maximum distance from analog stick before a touchDrag is registered as a touchUp
    public static float MOVEMENT_DRAG_ARROW_CANCEL_DISTANCE = 200f;
    //TODO: set these to false in production
    public static boolean SHOW_PLAYER_HEALTH_BARS = true;
    public static boolean SHOW_ENEMY_HEALTH_BARS = true;
    public static boolean SHOW_PP_GAIN_FLOATING_TEXT = true;
}
