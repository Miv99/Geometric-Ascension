package com.miv;

/**
 * Created by Miv on 6/9/2017.
 */
public class Options {
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
}
