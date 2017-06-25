package com.miv;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;

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

    public static void playerEnterNewMapArea(PooledEngine engine, Entity player, Map map, Direction directionOfTravel) {
        // Make player invincible
        // TODO: make the player accelerate instead of constant speed
        Mappers.hitbox.get(player).setVelocity(directionOfTravel.deltaX * 50f, directionOfTravel.deltaY * 50f);
        // TODO: wait a few seconds (until old map area is gone from camera)
        map.enterNewArea(engine, (int)map.getFocus().x + directionOfTravel.deltaX, (int)map.getFocus().y + directionOfTravel.deltaY);
        // Make player no longer invincible
    }

    public static void playerEnterNewFloor(PooledEngine engine, Entity player, Map map, int newFloor) {
        // Make player invincible
        //TODO: random animations; make player fade out to 0 alpha or something???
        map.enterNewFloor(newFloor);
        // Make player no longer invincible
    }
}
