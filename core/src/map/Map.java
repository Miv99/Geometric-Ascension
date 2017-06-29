package map;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.miv.AttackPart;
import com.miv.AttackPattern;
import com.miv.Mappers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import components.EnemyComponent;
import components.HitboxComponent;
import components.IgnoreRespawnOnAreaResetComponent;
import factories.AttackPatternFactory;
import systems.RenderSystem;
import utils.CircleHitbox;
import utils.Point;
import utils.Utils;

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

    private static final float INITIAL_MAP_AREA_PIXEL_POINTS = 50f;
    /**
     * How much {@link map.Map#maxPixelPoints} increases by each time a new MapArea is discovered
     */
    private static final float MAP_AREA_PIXEL_POINTS_INCREMENT = 1f;

    private static final float MIN_BULLET_SPEED_MULTIPLIER = 0.8f;
    private static final float MAX_BULLET_SPEED_MULTIPLIER = 1.2f;
    private static final float AVERAGE_BULLET_SPEED_MULTIPLIER = (MAX_BULLET_SPEED_MULTIPLIER + MIN_BULLET_SPEED_MULTIPLIER)/2f;

    private static final float MIN_FIRE_RATE_MULTIPLIER = 0.8f;
    private static final float MAX_FIRE_RATE_MULTIPLIER = 1.35f;
    private static final float AVERAGE_FIRE_RATE_MULTIPLIER = (MIN_FIRE_RATE_MULTIPLIER + MAX_FIRE_RATE_MULTIPLIER)/2f;

    private static final float MIN_HEALTH_MULTIPLIER = 0.8f;
    private static final float MAX_HEALTH_MULTIPLIER = 1.5f;
    private static final float AVERAGE_HEALTH_MULTIPLIER = (MIN_HEALTH_MULTIPLIER + MAX_HEALTH_MULTIPLIER)/2f;

    private static final int MIN_ENEMIES_PER_MAP_AREA = 3;
    private  static final int MAX_ENEMIES_PER_MAP_AREA = 8;

    // Starts at 0
    private int floor;
    private Point focus;
    // Maps location on the world map to a specific MapArea
    public HashMap<Point, MapArea> areas;
    // Percent chance of the next undiscovered MapArea being having a stairway (0 to MAX_CHANCE_OF_STAIRS_AREA)*100 %
    private float chanceOfNextAreaHavingStairs;
    // Maximum pixel points, distributed evenly to all enemies, when generating MapAreas
    private float maxPixelPoints;

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

        chanceOfNextAreaHavingStairs = 0f;
        maxPixelPoints = INITIAL_MAP_AREA_PIXEL_POINTS + MAP_AREA_PIXEL_POINTS_INCREMENT*floor;

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

        /**
         * Store all enemies currently in the engine as {@link map.EntityCreationData} objects inside {@link MapArea#entityCreationDataArrayList}
         */
        if(oldMapArea != null) {
            for (Entity e : engine.getEntitiesFor(Family.all(EnemyComponent.class, HitboxComponent.class).exclude(IgnoreRespawnOnAreaResetComponent.class).get())) {
                EntityCreationData ecd = new EntityCreationData();
                ecd.setIsEnemy(true);
                if (Mappers.boss.has(e)) {
                    ecd.setIsBoss(true);
                }

                // Restore health
                for(CircleHitbox c : Mappers.hitbox.get(e).getCircles()) {
                    c.setHealth(c.getMaxHealth());
                }

                ecd.setCircleHitboxes(Mappers.hitbox.get(e).getCircles());
                oldMapArea.entityCreationDataArrayList.add(ecd);
            }
        }

        newMapArea.spawnEntities(engine, this);

        focus.x = x;
        focus.y = y;
    }

    private MapArea generateRandomMapArea() {
        MapArea mapArea;
        if(Math.random() < chanceOfNextAreaHavingStairs) {
            mapArea = new MapArea(MathUtils.random(MapArea.MAP_AREA_MIN_SIZE, MapArea.MAP_AREA_MAX_SIZE));
            mapArea.addStairs(floor + 1);
        } else {
            mapArea = new MapArea(MathUtils.random(MapArea.MAP_AREA_MIN_SIZE, MapArea.MAP_AREA_MAX_SIZE));
        }
        chanceOfNextAreaHavingStairs = Math.min(0.3f, chanceOfNextAreaHavingStairs + CHANCE_OF_STAIRS_AREA_INCREMENT);

        // Populate map area with entities
        randomlyPopulate(mapArea);
        // Generates [a] points worth of enemies
        //TODO: make an enemy factory class; enemies are worth base 1 point have a random number of circle hitboxes w/ minimum 1
        // each extra circle hitbox adds 0.75 points to current point value and each has its own attack part (all the same in the same entity)

        return mapArea;
    }

    private void randomlyPopulate(MapArea mapArea) {
        int enemies = MathUtils.random(MIN_ENEMIES_PER_MAP_AREA, MAX_ENEMIES_PER_MAP_AREA);
        float ppPerEnemy = maxPixelPoints/(float)enemies;

        // Array list of circles that surround each enemy's hitbox
        // Used to avoid spawning enemies too close to each other
        ArrayList<CircleHitbox> enemyBoundingCircles = new ArrayList<CircleHitbox>();

        for(int i = 0; i < enemies; i++) {
            EntityCreationData ecd = new EntityCreationData();
            ecd.setIsEnemy(true);

            // Max health is 5000% to 7500% of total pp
            ecd.setMaxHealth(ppPerEnemy * MathUtils.random(50f, 75f));

            ArrayList<CircleHitbox> circles = ecd.getCircleHitboxes();

            AttackPattern attackPattern = AttackPatternFactory.getRandomAttackPatternByFloor(floor);

            // Randomly distribute pixel points to various aspects of attack pattern bullets
            float pp = ppPerEnemy;
            // Put -15% to 15% of total pp into speed multiplier
            float speedMultiplier = MathUtils.random(MIN_BULLET_SPEED_MULTIPLIER, MAX_BULLET_SPEED_MULTIPLIER);
            pp += 0.15f * (AVERAGE_BULLET_SPEED_MULTIPLIER - speedMultiplier)/(MAX_BULLET_SPEED_MULTIPLIER - MIN_BULLET_SPEED_MULTIPLIER) * ppPerEnemy * 2f;
            // Put -15% to 15% of total pp into fire rate
            float fireRateMultiplier = MathUtils.random(MIN_FIRE_RATE_MULTIPLIER, MAX_FIRE_RATE_MULTIPLIER);
            pp += 0.15f * (AVERAGE_FIRE_RATE_MULTIPLIER - fireRateMultiplier)/(MAX_FIRE_RATE_MULTIPLIER - MIN_FIRE_RATE_MULTIPLIER) * ppPerEnemy * 2f;
            // Put -15% to 15% of total pp into hp multiplier
            float healthMultiplier = MathUtils.random(MIN_HEALTH_MULTIPLIER, MAX_HEALTH_MULTIPLIER);
            pp += 0.15f * (AVERAGE_HEALTH_MULTIPLIER - fireRateMultiplier)/(MIN_HEALTH_MULTIPLIER - MAX_HEALTH_MULTIPLIER) * ppPerEnemy * 2f;
            // Put 45% to 75% of remaining pp into damage
            float percentDamage = MathUtils.random(0.45f, 0.75f);
            float bulletTotalDamage = pp * percentDamage * attackPattern.getAttackParts().length;
            pp -= pp * percentDamage;
            // Put remaining pp into radius
            float bulletTotalRadius = pp * attackPattern.getAttackParts().length;

            // Modify the attack pattern according to pp distribution
            for(AttackPart a : attackPattern.getAttackParts()) {
                a.setSpeed(a.getSpeed() * speedMultiplier);
                a.setDelay(a.getDelay() * fireRateMultiplier);

                // New bullet damage is the old bullet damage's percent of the attack pattern's total damage, multiplied by the new total bullet damage
                a.setDamage(a.getDamage() / attackPattern.getTotalDamage() * bulletTotalDamage);
                // Same is done to radius
                a.setRadius(a.getRadius()/attackPattern.getTotalRadius() * bulletTotalRadius);
            }

            // If circle hitbox contains more than 1 circle, each circle except the first is placed
            // so that it is tangential to the first circle
            int circlesCount = getEnemyRandomCirclesCount();
            float c1Radius = getRandomCircleRadius(circlesCount);
            for(int a = 1; a < circlesCount; a++) {
                CircleHitbox c = new CircleHitbox();

                // Set color
                c.setColor(RenderSystem.ENEMY_COLOR);

                // Set attack pattern
                c.setAttackPattern(attackPattern);

                // Set radius
                float radius = getRandomCircleRadius(circlesCount);
                c.setRadius(radius);

                // Set health
                float health = ecd.getMaxHealth()/(float)circlesCount;
                c.setMaxHealth(health);
                c.setHealth(health);

                // Randomize angle from first circle until this circle does not overlap with any other
                float angle = MathUtils.random(0, MathUtils.PI2);
                c.setPosition((c1Radius + radius)*MathUtils.cos(angle), (c1Radius + radius)*MathUtils.cos(angle));
                while(Utils.overlaps(c, circles)) {
                    angle = MathUtils.random(0, MathUtils.PI2);
                    c.setPosition((c1Radius + radius)*MathUtils.cos(angle), (c1Radius + radius)*MathUtils.cos(angle));
                }
                circles.add(c);
            }

            // Calculate max size of entity hitbox
            float maxSize = c1Radius;
            float maxRadiusSoFar = 0;
            for(CircleHitbox c : circles) {
                if(c.radius > maxRadiusSoFar) {
                    maxRadiusSoFar = c.radius;
                }
            }
            // Extra padding of 5 pixels to avoid inaccuracies with MathUtils.cos/sin
            maxSize += maxRadiusSoFar + 5f;

            // Create bounding circle
            CircleHitbox boundingCircle = new CircleHitbox();
            boundingCircle.setRadius(maxSize);

            // Randomize hitbox origin with padding equal to the hitbox max size around the map area circumference
            float angle = MathUtils.random(0, MathUtils.PI2);
            float distance = MathUtils.random(100, mapArea.getRadius() - maxSize);
            boundingCircle.setPosition(distance*MathUtils.cos(angle), distance*MathUtils.sin(angle));
            while(Utils.overlaps(boundingCircle, enemyBoundingCircles)) {
                angle = MathUtils.random(0, MathUtils.PI2);
                distance = MathUtils.random(100, mapArea.getRadius() - maxSize);
                boundingCircle.setPosition(distance*MathUtils.cos(angle), distance*MathUtils.sin(angle));
            }

            ecd.setSpawnPosition(boundingCircle.x, boundingCircle.y);
            enemyBoundingCircles.add(boundingCircle);

            // Create first circle
            CircleHitbox c1 = new CircleHitbox();
            c1.setColor(RenderSystem.ENEMY_COLOR);
            c1.setPosition(0, 0);
            c1.setRadius(c1Radius);
            c1.setAttackPattern(attackPattern);
            circles.add(c1);

            mapArea.entityCreationDataArrayList.add(ecd);
        }
    }

    /**
     * Returns a random number for a randomly generated enemy's circle hitbox radius.
     * @param circlesCount - Must be less than 8
     */
    private static float getRandomCircleRadius(int circlesCount) {
        return MathUtils.random(35f - circlesCount*5f, 80f - circlesCount*10f);
    }

    /**
     * Returns a random number for the number of circle hitboxes a randomly generated enemy will have.
     * Weighted towards 1.
     */
    private int getEnemyRandomCirclesCount() {
        float random = MathUtils.random();
        // 70% of returning 1
        if(random < 0.70f) {
            return 1;
        }
        // 20% of 2
        else if(random < 0.9f) {
            return 2;
        }
        // 10% of 3
        else {
            return 3;
        }
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
