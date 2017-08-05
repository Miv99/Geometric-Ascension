package com.miv;

import map.Map;

/**
 * Created by Miv on 6/9/2017.
 */
public class Options {
    public static final float PP_GAIN_MULTIPLIER = 1.3f/Map.INITIAL_MAP_AREA_PIXEL_POINTS;
    // Affects bonus pp gain from clearing a map area
    public static final float BONUS_PP_MULTIPLIER = 0.3f;
    public static final float GLOBAL_MOVEMENT_SPEED_MULTIPLIER = 30f;

    /**
     * The minimum distance from an entity's origin to another's hitbox's {@link components.HitboxComponent#gravitationalRadius} and/or map area border that the repelling force begins to take effect.
     * The repelling force works in {@link systems.MovementSystem} by adding to each hitbox's velocity, with the amount added increasing
     * exponentially as distance between the two hitbox origins decreases. The magnitude of the velocity added is capped at GRAVITY_SPEED_CAP meters/frame.
     */
    public static final float GRAVITY_DROP_OFF_DISTANCE = 500f;
    // Distance from the edge of the farthest CircleHitbox in a hitbox from the origin that the repelling forces of gravity take maximum effect
    public static final float GRAVITATIONAL_RADIUS_PADDING = 10f;
    public static final float GRAVITY_SPEED_CAP = 100f;
    public static final float GRAVITATIONAL_CONSTANT = 2000f;




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
