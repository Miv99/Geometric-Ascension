package map;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.miv.Mappers;

import java.util.HashMap;

import components.EnemyComponent;
import components.HitboxComponent;
import components.IgnoreRespawnOnAreaResetComponent;
import utils.Point;

/**
 * Only the MapArea that is being focused on {@link map.Map#focus} has its entities in the engine.
 * Every time a new area is entered, all non-player entities are removed from the engine and stored into {@link EntityCreationData} objects.
 * This results in all non-player entities regaining maximum health after the player leaves the MapArea.
 * Every time the player enters a new point on the world map that isn't in {@link map.Map#areas}, a new MapArea
 * is generated on the spot and populated with entities depending on {@link map.Map#floor}.
 * Created by Miv on 5/23/2017.
 */
public class Map {
    private static final float MAX_CHANCE_OF_STAIRS_AREA = 0.3f;
    /**
     * How much {@link map.Map#chanceOfNextAreaHavingStairs} increases by each time a new MapArea is discovered
     */
    private static final float CHANCE_OF_STAIRS_AREA_INCREMENT = 0.01f;

    private int floor;
    private Point focus;
    // Maps location on the world map to a specific MapArea
    public HashMap<Point, MapArea> areas;
    // Percent chance of the next undiscovered MapArea being having a stairway (0 to MAX_CHANCE_OF_STAIRS_AREA)*100 %
    private float chanceOfNextAreaHavingStairs;

    public Map() {
        areas = new HashMap<Point, MapArea>();
        focus = new Point(0, 0);
        chanceOfNextAreaHavingStairs = 0f;
    }

    public void enterNewFloor(int floor) {
        int lastFloor = this.floor;
        this.floor = floor;
        setFocus(0, 0);
        areas.clear();

        // First MapArea always has stairs leading to the previous floor
        MapArea mapArea = new MapArea(MapArea.MAP_AREA_MIN_SIZE);
        mapArea.addStairs(lastFloor);
        areas.put(new Point(0, 0), mapArea);
    }

    public void enterNewArea(PooledEngine engine, int x, int y) {
        MapArea oldMapArea = areas.get(focus);

        MapArea newMapArea;
        Point newPos = new Point(x, y);
        if(!areas.containsKey(newPos)) {
            newMapArea = generateRandomMapArea();
            areas.put(newPos, newMapArea);
        } else {
            newMapArea = areas.get(new Point(x, y));
        }
        chanceOfNextAreaHavingStairs = 0f;

        /**
         * Store all enemies currently in the engine as {@link map.EntityCreationData} objects inside {@link MapArea#entityCreationDataArrayList}
         */
        for(Entity e : engine.getEntitiesFor(Family.all(EnemyComponent.class, HitboxComponent.class).exclude(IgnoreRespawnOnAreaResetComponent.class).get())) {
            EntityCreationData ecd = new EntityCreationData();
            ecd.setIsEnemy(true);
            if(Mappers.health.has(e)) {
                ecd.setMaxHealth(Mappers.health.get(e).getMaxHealth());
            }
            if(Mappers.boss.has(e)) {
                ecd.setIsBoss(true);
            }
            ecd.setCircleHitboxes(Mappers.hitbox.get(e).circles);
            oldMapArea.entityCreationDataArrayList.add(ecd);
        }

        newMapArea.spawnEntities(engine, this);

        focus.x = x;
        focus.y = y;
    }

    public MapArea generateRandomMapArea() {
        MapArea mapArea;
        if(Math.random() < chanceOfNextAreaHavingStairs) {
            mapArea = new MapArea(MathUtils.random(MapArea.MAP_AREA_MIN_SIZE, MapArea.MAP_AREA_MAX_SIZE));
            mapArea.addStairs(floor + 1);
        } else {
            mapArea = new MapArea(MathUtils.random(MapArea.MAP_AREA_MIN_SIZE, MapArea.MAP_AREA_MAX_SIZE));
        }
        chanceOfNextAreaHavingStairs = Math.min(0.3f, chanceOfNextAreaHavingStairs + CHANCE_OF_STAIRS_AREA_INCREMENT);

        return mapArea;
    }

    public void setFocus(int x, int y) {
        focus.x = x;
        focus.y = y;
    }

    public Point getFocus() {
        return focus;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }
}
