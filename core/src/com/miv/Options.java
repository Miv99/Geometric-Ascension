package com.miv;

/**
 * Created by Miv on 6/9/2017.
 */
public class Options {
    // Doesn't actually do anything right now
    public static final float FPS = 30f;

    // Strings used for storing corresponding values inside Preference files
    public static final String MUSIC_VOLUME_STRING = "musicVolume";
    public static final String SOUND_VOLUME_STRING = "soundVolume";
    public static final String MASTER_VOLUME_STRING = "masterVolume";
    public static final String MOVEMENT_DRAG_ARROW_MAX_DISTANCE_STRING = "movementDragArrowMaxDistance";

    public static float MUSIC_VOLUME = 0.5f;
    public static float SOUND_VOLUME = 0.5f;
    public static float MASTER_VOLUME = 0.5f;
    // Maximum distance dragged on the screen before the player reaches maximum speed
    public static float MOVEMENT_DRAG_ARROW_MAX_DISTANCE = 200f;

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
}
