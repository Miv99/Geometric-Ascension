package com.miv;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Timer;

import map.Map;

/**
 * Created by Miv on 5/23/2017.
 */
public class EntityActions {
    public enum Direction {
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

        public int getDeltaX() {
            return deltaX;
        }

        public int getDeltaY() {
            return deltaY;
        }
    }

    public static void playerEnterNewMapArea(Entity player, Direction directionOfTravel) {
        Mappers.hitbox.get(player).setTravelling(true);
        Mappers.hitbox.get(player).setIgnoreSpeedLimit(true);

        // Make player invincible
        Mappers.hitbox.get(player).setIntangible(true);

        // TODO: make the player accelerate instead of constant speed
        final float playerTravelSpeed = 30f;
        Mappers.hitbox.get(player).setVelocity(directionOfTravel.deltaX * playerTravelSpeed, directionOfTravel.deltaY * playerTravelSpeed);
        Mappers.hitbox.get(player).setTravellingDirection(directionOfTravel);
        Mappers.hitbox.get(player).setTravellingSpeed(playerTravelSpeed);
    }

    public static void playerEnterNewFloor(final PooledEngine engine, final Entity player, final Map map, final int newFloor) {
        // Make player invincible
        Mappers.hitbox.get(player).setIntangible(true);

        // Fade screen to white
        map.getMain().getHud().fadeToColor(new Color(1f, 1f, 1f, 1f), 3.5f, new Timer.Task() {
            @Override
            public void run() {
                map.enterNewFloor(engine, newFloor);

                // Make player no longer invincible
                Mappers.hitbox.get(player).setIntangible(false);
            }
        });
    }
}
