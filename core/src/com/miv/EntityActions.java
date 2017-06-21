package com.miv;

import com.badlogic.ashley.core.Entity;

import map.Map;

/**
 * Created by Miv on 5/23/2017.
 */
public class EntityActions {
    public static enum Direction {
        UP(0, 1),
        DOWN(0, -1),
        LEFT(-1, 0),
        RIGHT(1, 0);

        private int deltaX;
        private int deltaY;
        Direction(int deltaX, int deltaY) {
            this.deltaX = deltaX;
            this.deltaY = deltaY;
        }
    }

    public void playerEnterNewMapArea(Entity player, Map map, Direction directionOfTravel) {
        // TODO: make the player accelerate instead of constant speed
        Mappers.hitbox.get(player).setVelocity(directionOfTravel.deltaX * 50f, directionOfTravel.deltaY * 50f);
        // TODO: wait a few seconds (until old map area is gone from camera) then change map.focus and generate new MapArea if one doesn't already exist
    }
}
