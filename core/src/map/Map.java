package map;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.miv.AttackPart;
import com.miv.AttackPattern;
import com.miv.Main;
import com.miv.Mappers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import ai.AI;
import components.EnemyBulletComponent;
import components.EnemyComponent;
import components.HitboxComponent;
import components.IgnoreRespawnOnAreaResetComponent;
import components.PlayerBulletComponent;
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

    private static final float INITIAL_MAP_AREA_PIXEL_POINTS = 20f;
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

    private transient Main main;

    // Starts at 0
    private int floor;
    private Point focus;
    // Maps location on the world map to a specific MapArea
    private HashMap<Point, MapArea> areas;
    // Percent chance of the next undiscovered MapArea being having a stairway (0 to MAX_CHANCE_OF_STAIRS_AREA)*100 %
    private float chanceOfNextAreaHavingStairs;
    // Maximum pixel points, distributed evenly to all enemies, when generating MapAreas
    private float maxPixelPoints;

    private MapArea currentArea;

    /**
     * For Json files
     */
    public Map() {}

    public Map(Main main) {
        this.main = main;

        areas = new HashMap<Point, MapArea>();
        focus = new Point(0, 0);
        chanceOfNextAreaHavingStairs = 0f;
        maxPixelPoints = INITIAL_MAP_AREA_PIXEL_POINTS;
    }

    public void enterNewFloor(int floor) {
        this.floor = floor;
        setFocus(0, 0);
        areas.clear();

        chanceOfNextAreaHavingStairs = 0f;
        maxPixelPoints = INITIAL_MAP_AREA_PIXEL_POINTS + MAP_AREA_PIXEL_POINTS_INCREMENT*(float)floor;

        // First MapArea always has stairs leading to the previous floor
        MapArea mapArea = new MapArea(MapArea.MAP_AREA_MIN_SIZE);
        areas.put(new Point(0, 0), mapArea);
        enterNewArea(main.getEngine(), main.getPlayer(), 0, 0);

        main.save();
    }

    public void enterNewArea(PooledEngine engine, Entity player, int x, int y) {
        MapArea oldMapArea = areas.get(focus);

        boolean increaseChanceOfNextAreaHavingStairs = false;

        MapArea newMapArea;
        Point newPos = new Point(x, y);
        if(!areas.containsKey(newPos)) {
            increaseChanceOfNextAreaHavingStairs = true;
            newMapArea = generateRandomMapArea();
            areas.put(newPos, newMapArea);
        } else {
            newMapArea = areas.get(new Point(x, y));
        }
        currentArea = newMapArea;

        /**
         * Store all enemies currently in the engine as {@link map.EntityCreationData} objects inside {@link MapArea#entityCreationDataArrayList}
         */
        if(oldMapArea != null) {
            oldMapArea.entityCreationDataArrayList.clear();

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

                ArrayList<CircleHitbox> circles = new ArrayList<CircleHitbox>();
                circles.addAll(Mappers.hitbox.get(e).getCircles());
                ecd.setCircleHitboxes(circles);
                oldMapArea.entityCreationDataArrayList.add(ecd);

                engine.removeEntity(e);
            }

            // Remove all bullets
            for(Entity e : engine.getEntitiesFor(Family.one(EnemyBulletComponent.class, PlayerBulletComponent.class).get())) {
                engine.removeEntity(e);
            }
        }

        newMapArea.spawnEntities(engine, this, player);

        focus.x = x;
        focus.y = y;

        main.save();

        // Increase chance of next area having stairs after autosaving to avoid the user entering new areas and
        // reloading the game to avoid all enemies and quickly enter new floors
        if(increaseChanceOfNextAreaHavingStairs && (x != 0 && y != 0)) {
            chanceOfNextAreaHavingStairs = Math.min(MAX_CHANCE_OF_STAIRS_AREA, chanceOfNextAreaHavingStairs + CHANCE_OF_STAIRS_AREA_INCREMENT);
        }
    }

    private MapArea generateRandomMapArea() {
        MapArea mapArea;
        if(Math.random() < chanceOfNextAreaHavingStairs) {
            mapArea = new MapArea(MathUtils.random(MapArea.MAP_AREA_MIN_SIZE, MapArea.MAP_AREA_MAX_SIZE));
            mapArea.addStairs(floor + 1);
        } else {
            mapArea = new MapArea(MathUtils.random(MapArea.MAP_AREA_MIN_SIZE, MapArea.MAP_AREA_MAX_SIZE));
        }

        // Populate map area with entities
        randomlyPopulate(mapArea);

        return mapArea;
    }

    private void randomlyPopulate(MapArea mapArea) {
        int enemies = MathUtils.random(MIN_ENEMIES_PER_MAP_AREA, MAX_ENEMIES_PER_MAP_AREA);
        mapArea.setEnemyCount(enemies);
        float ppPerEnemy = maxPixelPoints/(float)enemies;

        // Array list of circles that surround each enemy's hitbox
        // Used to avoid spawning enemies too close to each other
        ArrayList<CircleHitbox> enemyBoundingCircles = new ArrayList<CircleHitbox>();

        for(int i = 0; i < enemies; i++) {
            EntityCreationData ecd = new EntityCreationData();
            ecd.setIsEnemy(true);

            //TODO: add on to this as more AI types are added
            // Randomize AI type
            float rand = MathUtils.random();
            // 75% for SimpleStalk
            if(rand < 0.75f) {
                ecd.setAiType(AI.AIType.SIMPLE_STALK_TARGET);
                ecd.setSimpleStalkMinSpeedDistance(MathUtils.random(100f, 250f));
                ecd.setSimpleStalkMaxSpeedDistance(MathUtils.random(330f, 450f));
            }
            /**
            // 25% for SimpleFollow
            else if(rand < 0.75f) {
                ecd.setAiType(AI.AIType.SIMPLE_FOLLOW_TARGET);

            }
             */
            // 25% for SimpleWander
            else {
                float mapRadius = mapArea.getRadius();

                ecd.setAiType(AI.AIType.SIMPLE_WANDER);
                ecd.setSimpleWanderRadius(MathUtils.random(0.1f * mapRadius, 0.2f * mapRadius));
                ecd.setSimpleWanderMinInterval(0.5f);
                ecd.setSimpleWanderMaxInterval(1.5f);
                ecd.setSimpleWanderMinAcceleration(1 / 60f);
                ecd.setSimpleWanderMaxAcceleration(1.5f / 60f);
            }

            // Max speed is a random number between 1f and 5f
            ecd.setMaxSpeed(MathUtils.random(1f, 3f));

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
                c.setHitboxTextureType(RenderSystem.HitboxTextureType.ENEMY);

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
                c.setPosition((c1Radius + radius) * MathUtils.cos(angle), (c1Radius + radius) * MathUtils.sin(angle));
                while(Utils.overlaps(c, circles)) {
                    angle = MathUtils.random(0, MathUtils.PI2);
                    c.setPosition((c1Radius + radius)*MathUtils.cos(angle), (c1Radius + radius)*MathUtils.sin(angle));
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
            c1.setHitboxTextureType(RenderSystem.HitboxTextureType.ENEMY);
            c1.setPosition(0, 0);
            c1.setRadius(c1Radius);
            c1.setAttackPattern(attackPattern);
            float health = ecd.getMaxHealth()/(float)circlesCount;
            c1.setMaxHealth(health);
            c1.setHealth(health);
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

    public void setMain(Main main) {
        this.main = main;
    }

    public Main getMain() {
        return main;
    }

    public MapArea getCurrentArea() {
        return currentArea;
    }
}
