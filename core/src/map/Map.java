package map;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.miv.AttackPattern;
import com.miv.Main;
import com.miv.Mappers;
import com.miv.Options;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import ai.AI;
import ai.SimpleFollowTarget;
import ai.SimpleStalkTarget;
import ai.SimpleWander;
import components.AIComponent;
import components.EnemyBulletComponent;
import components.EnemyComponent;
import components.HitboxComponent;
import components.ObstacleComponent;
import components.PlayerBulletComponent;
import components.PpOrbComponent;
import factories.AttackPatternFactory;
import factories.BossFactory;
import screens.MapScreen;
import systems.RenderSystem;
import utils.CircleHitbox;
import utils.Point;
import utils.Utils;

import static map.MapArea.BOSS_MAP_AREA_SIZE;

/**
 * Only the MapArea that is being focused on {@link map.Map#focus} has its entities in the engine.
 * Every time a new area is entered, all non-player entities are removed from the engine and stored into {@link EntityCreationData} objects.
 * This results in all non-player entities regaining maximum health after the player leaves the MapArea.
 * Every time the player enters a new point on the world map that isn't in {@link map.Map#areas}, a new MapArea
 * is generated on the spot and populated with entities depending on {@link map.Map#floor}.
 * Created by Miv on 5/23/2017.
 */
public class Map {
    //------------------------------------------------------------- MAP AREA GENERATION --------------------------------------------------
    private static final float NEW_MAP_AREAS_UNTIL_BOSS = 20;

    public static final float INITIAL_MAP_AREA_PIXEL_POINTS = 20f;
    /**
     * How much {@link map.Map#maxPixelPoints} increases by each time the player enters a new floor
     */
    private static final float MAP_AREA_PIXEL_POINTS_INCREMENT = 7.5f;

    private static final int MIN_ENEMIES_PER_MAP_AREA = 3;
    private static final int MAX_ENEMIES_PER_MAP_AREA = 8;

    public static final float MIN_OBSTACLE_RADIUS = 25f;
    public static final float MAX_OBSTACLE_RADIUS = 125f;

    public static final float MIN_OBSTACLE_HEALTH_MULTIPLIER = 0.8f;
    public static final float MAX_OBSTACLE_HEALTH_MULTIPLIER = 1.6f;

    public static final float OBSTACLE_HEALTH_PP_SCALE = 0.5f;
    //-----------------------------------------------------------------------------------------------------

    private transient Main main;

    // Starts at 0
    private int floor;
    private Point focus;
    // Maps location on the world map to a specific MapArea
    // String representation of utils.Point is used as key to be able to save HashMap in a json
    private HashMap<String, MapArea> areas;
    // Number of new map areas to be discovered until the next one is the floor's boss
    private float newMapAreasUntilBoss;
    // Maximum pixel points, distributed evenly to all enemies, when generating MapAreas
    private float maxPixelPoints;

    // min/max enemies per map area, affected by floor
    private float minEnemiesPerMapArea;
    private float maxEnemiesPerMapArea;

    private transient MapArea currentArea;

    /**
     * For Json files
     */
    public Map() {}

    public Map(Main main) {
        this.main = main;

        areas = new HashMap<String, MapArea>();
        focus = new Point(0, 0);
        newMapAreasUntilBoss = NEW_MAP_AREAS_UNTIL_BOSS;
        maxPixelPoints = INITIAL_MAP_AREA_PIXEL_POINTS;

        minEnemiesPerMapArea = MIN_ENEMIES_PER_MAP_AREA;
        maxEnemiesPerMapArea = MAX_ENEMIES_PER_MAP_AREA;
    }

    public void enterNewFloor(int floor) {
        this.floor = floor;
        setFocus(0, 0);
        areas.clear();

        newMapAreasUntilBoss = NEW_MAP_AREAS_UNTIL_BOSS;
        maxPixelPoints = INITIAL_MAP_AREA_PIXEL_POINTS + MAP_AREA_PIXEL_POINTS_INCREMENT*(float)floor;

        float enemyCountIncrease = floor/10f;
        minEnemiesPerMapArea = MIN_ENEMIES_PER_MAP_AREA + enemyCountIncrease/2f;
        maxEnemiesPerMapArea = MAX_ENEMIES_PER_MAP_AREA + enemyCountIncrease;

        MapArea mapArea = new MapArea(main.getEngine(), MapArea.MAP_AREA_MIN_SIZE, maxPixelPoints);
        areas.put(new Point(0, 0).toString(), mapArea);
        Mappers.hitbox.get(main.getPlayer()).setOrigin(0, 0);
        enterNewArea(main.getEngine(), main.getPlayer(), 0, 0, true);

        main.save();
    }

    public void enterNewArea(PooledEngine engine, Entity player, int x, int y, boolean clearNewMapAreaEntityCreationDataAfterSpawningEnemies) {
        main.getRenderSystem().clearFloatingTexts();

        MapArea oldMapArea = areas.get(focus.toString());
        if(oldMapArea != null) {
            oldMapArea.unloadMods();
        }

        boolean increaseChanceOfNextAreaHavingStairs = false;

        MapArea newMapArea;
        Point newPos = new Point(x, y);
        if(!areas.containsKey(newPos.toString())) {
            increaseChanceOfNextAreaHavingStairs = true;
            newMapArea = generateRandomMapArea(newPos);
            areas.put(newPos.toString(), newMapArea);
        } else {
            newMapArea = areas.get(new Point(x, y).toString());
            newMapArea.loadMods(main.getEngine(), main.getAssetManager(), main.getPlayer());
        }
        currentArea = newMapArea;

        /**
         * Store all enemies currently in the engine as {@link map.EntityCreationData} objects inside {@link MapArea#entityCreationDataArrayList}
         */
        if(oldMapArea != null && !(focus.x == x && focus.y == y)) {
            oldMapArea.storeExistingEnemies(engine, true);
        }

        ArrayList<Entity> entitiesToBeRemoved = new ArrayList<Entity>();
        for (Entity e : engine.getEntitiesFor(Family.one(EnemyBulletComponent.class, PlayerBulletComponent.class, PpOrbComponent.class, ObstacleComponent.class).get())) {
            entitiesToBeRemoved.add(e);
        }

        if(!(x == 0 && y == 0) && !newMapArea.isBossArea()) {
            populateWithObstacles(newMapArea);
        }
        newMapArea.spawnEntities(player, clearNewMapAreaEntityCreationDataAfterSpawningEnemies);

        focus.x = x;
        focus.y = y;

        // Increase chance of next area having stairs after autosaving to avoid the user entering new areas and
        // reloading the game to avoid all enemies and quickly enter new floors
        if(increaseChanceOfNextAreaHavingStairs && !(x == 0 && y == 0)) {
            newMapAreasUntilBoss--;
        }

        newMapArea.onEntityEnter(player);
        if(oldMapArea != null && !(focus.x == x && focus.y == y)) {
            oldMapArea.onPlayerLeave();
        }

        main.updateScreenActors();

        // Entities being removed later fixes bug where they weren't actually being removed somehow
        for(Entity e : entitiesToBeRemoved) {
            engine.removeEntity(e);
        }
    }

    private MapArea generateRandomMapArea(Point pos) {
        MapArea mapArea;
        if(pos.x == 0 && pos.y == 0) {
            mapArea = new MapArea(main.getEngine(), MapArea.MAP_AREA_MIN_SIZE, maxPixelPoints);
        } else {
            if(newMapAreasUntilBoss <= 0) {
                mapArea = new MapArea(main.getEngine(), BOSS_MAP_AREA_SIZE, maxPixelPoints);
                mapArea.addStairs(floor + 1);
                populateWithBoss(mapArea);
            } else {
                mapArea = new MapArea(main.getEngine(), MathUtils.random(MapArea.MAP_AREA_MIN_SIZE, MapArea.MAP_AREA_MAX_SIZE), maxPixelPoints);
                mapArea.randomizeRarity();
                mapArea.loadMods(main.getEngine(), main.getAssetManager(), main.getPlayer());
                // Populate map area with enemies
                randomlyPopulate(mapArea);
            }
        }

        return mapArea;
    }

    public void populateWithBoss(MapArea mapArea) {
        mapArea.entityCreationDataArrayList.addAll(BossFactory.getBossByFloor(floor, mapArea.getRadius(), maxPixelPoints));
    }

    private void randomlyPopulate(MapArea mapArea) {
        int enemies = Math.round(MathUtils.random(minEnemiesPerMapArea, maxEnemiesPerMapArea));
        mapArea.setOriginalEnemyCount(enemies);
        mapArea.setEnemyCount(enemies);
        float ppPerEnemy = maxPixelPoints/(float)enemies * (minEnemiesPerMapArea + maxEnemiesPerMapArea)/2f;

        // Set map area attack pattern bias
        AttackPattern bias = AttackPatternFactory.getRandomAttackPatternByFloor(floor);

        // Array list of circles that surround each enemy's hitbox
        // Used to avoid spawning enemies too close to each other
        ArrayList<CircleHitbox> enemyBoundingCircles = new ArrayList<CircleHitbox>();

        for(int i = 0; i < enemies; i++) {
            float adjustedPpPerEnemy = ppPerEnemy;

            EntityCreationData ecd = new EntityCreationData();
            ecd.setIsEnemy(true);

            randomizeEnemyMovementAI(ecd, mapArea.getRadius());

            // Max speed is a random number between 1f and 5f
            ecd.setMaxSpeed(MathUtils.random(1f, 3f));

            // Max health is 25% to 100% of total pp
            float hpMultiplier = MathUtils.random(0.25f, 1f);
            ecd.setMaxHealth(ppPerEnemy * hpMultiplier);
            adjustedPpPerEnemy *= (hpMultiplier/((0.25f + 1f)/2f));

            ArrayList<CircleHitbox> circles = ecd.getCircleHitboxes();

            AttackPattern attackPattern;
            // 75% of enemy having the map area's attack pattern bias
            if(Math.random() < 0.75f) {
                attackPattern = bias.clone();
            } else {
                attackPattern = AttackPatternFactory.getRandomAttackPatternByFloor(floor);
            }
            attackPattern.addRandomAttackPatternStatModifiers(ppPerEnemy);
            adjustedPpPerEnemy *= Math.pow((attackPattern.getBulletDamagePpMultiplier() * attackPattern.getBulletRadiusPpMultiplier() * attackPattern.getFireIntervalPpMultiplier() * attackPattern.getSpeedPpMultiplier())/3f, 1.5f);

            CircleHitbox c1 = new CircleHitbox();

            // If circle hitbox contains more than 1 circle, each circle except the first is placed
            // so that it is tangential to the first circle
            int circlesCount = getEnemyRandomCirclesCount();
            float c1Radius = getRandomCircleRadius(circlesCount);
            for(int a = 1; a < circlesCount; a++) {
                CircleHitbox c = new CircleHitbox();

                c.setBasePpGain(adjustedPpPerEnemy / circlesCount * Options.PP_GAIN_MULTIPLIER);

                // Set color
                c.setHitboxTextureType(RenderSystem.HitboxTextureType.ENEMY);

                // Set attack pattern
                c.setAttackPattern(attackPattern);

                // Set radius
                float radius = getRandomCircleRadius(circlesCount);
                c.setRadius(radius);

                // Randomize angle from first circle until this circle does not overlap with any other
                float angle = MathUtils.random(0, MathUtils.PI2);
                c.setPosition((c1Radius + radius) * MathUtils.cos(angle), (c1Radius + radius) * MathUtils.sin(angle));
                while(Utils.overlaps(c, circles)) {
                    angle = MathUtils.random(0, MathUtils.PI2);
                    c.setPosition((c1Radius + radius)*MathUtils.cos(angle), (c1Radius + radius)*MathUtils.sin(angle));
                }
                circles.add(c);
            }

            float totalCircleRadius = c1Radius;
            for(int a = 0; a < circlesCount - 1; a++) {
                totalCircleRadius += circles.get(a).radius;
            }

            c1.setBasePpGain(adjustedPpPerEnemy / circlesCount * Options.PP_GAIN_MULTIPLIER);

            // Scale circle health to radius
            float c1Health = c1Radius/totalCircleRadius * ecd.getMaxHealth();
            c1.setBaseMaxHealth(c1Health);
            c1.setHealth(c1.getMaxHealth());
            for(int a = 0; a < circlesCount - 1; a++) {
                CircleHitbox c = circles.get(a);

                // Set health
                float health = c.radius/totalCircleRadius * ecd.getMaxHealth();
                c.setBaseMaxHealth(health);
                c.setHealth(c.getMaxHealth());
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
            c1.setHitboxTextureType(RenderSystem.HitboxTextureType.ENEMY);
            c1.setPosition(0, 0);
            c1.setRadius(c1Radius);
            c1.setAttackPattern(attackPattern);
            circles.add(c1);

            mapArea.entityCreationDataArrayList.add(ecd);
            mapArea.onEnemyDataCreation(ecd);
        }
    }

    private void populateWithObstacles(MapArea mapArea) {
        for(int i = 0; i < getRandomObstaclesCount(); i++) {
            float radius = MathUtils.random(MIN_OBSTACLE_RADIUS, MAX_OBSTACLE_RADIUS);

            EntityCreationData ecd = new EntityCreationData();
            ecd.setIsEnemy(false);
            ecd.setObstacle(true);

            CircleHitbox c = new CircleHitbox();
            // Set health to be > 0 to prevent death instantly
            float hp = maxPixelPoints * OBSTACLE_HEALTH_PP_SCALE * MathUtils.random(MIN_OBSTACLE_HEALTH_MULTIPLIER, MAX_OBSTACLE_HEALTH_MULTIPLIER);
            c.setBaseMaxHealth(hp);
            c.setHealth(c.getMaxHealth());
            c.setRadius(radius);
            c.setHitboxTextureType(RenderSystem.HitboxTextureType.OBSTACLE);
            ecd.getCircleHitboxes().add(c);

            float angle = MathUtils.random(MathUtils.PI2);
            float distance = MathUtils.random(radius, mapArea.getRadius() - radius);
            ecd.setSpawnPosition(distance * MathUtils.cos(angle), distance * MathUtils.sin(angle));

            mapArea.entityCreationDataArrayList.add(ecd);
            mapArea.onEnemyDataCreation(ecd);
        }
    }

    private int getRandomObstaclesCount() {
        float rand = MathUtils.random();
        if(rand < 0.1f) {
            return 5;
        } else if(rand < 0.2f) {
            return 4;
        } else if(rand < 0.35f) {
            return 3;
        } else if(rand < 0.7f) {
            return 2;
        } else if(rand < 0.9f) {
            return 1;
        } else {
            return 0;
        }
    }

    public static void randomizeEnemyMovementAI(EntityCreationData ecd, float mapAreaRadius) {
        //TODO: add on to this as more AI types are added
        float rand = MathUtils.random();
        // 75% for SimpleStalk
        if(rand < 0.75f) {
            randomizeSimpleStalkTargetAI(ecd);
        }
        // 25% for SimpleWander
        else {
            randomizeSimpleWanderAI(ecd, mapAreaRadius);
        }
    }

    public static void randomizeSimpleStalkTargetAI(EntityCreationData ecd) {
        ecd.setRotationBehaviorParams(new AI.RotationBehaviorParams(true, 0.6f));
        ecd.setAiType(AI.AIType.SIMPLE_STALK_TARGET);
        ecd.setSimpleStalkMinSpeedDistance(MathUtils.random(100f, 250f));
        ecd.setSimpleStalkMaxSpeedDistance(MathUtils.random(330f, 450f));
    }

    public static void randomizeSimpleWanderAI(EntityCreationData ecd, float mapAreaRadius) {
        ecd.setRotationBehaviorParams(new AI.RotationBehaviorParams(true, 0.6f));
        ecd.setAiType(AI.AIType.SIMPLE_WANDER);
        ecd.setSimpleWanderRadius(MathUtils.random(0.2f * mapAreaRadius, 0.4f * mapAreaRadius));
        ecd.setSimpleWanderMinInterval(0.5f);
        ecd.setSimpleWanderMaxInterval(1.5f);
        ecd.setSimpleWanderMinAcceleration(1 / 60f);
        ecd.setSimpleWanderMaxAcceleration(1.5f / 60f);
    }

    //TODO: add to this as more AI types are added
    public static AIComponent createAIComponent(PooledEngine engine, Entity e, EntityCreationData ecd, Entity player) {
        if(ecd.getAiType() == AI.AIType.SIMPLE_FOLLOW_TARGET) {
           return engine.createComponent(AIComponent.class).setAi(new SimpleFollowTarget(e, player, ecd.getRotationBehaviorParams()));
        } else if(ecd.getAiType() == AI.AIType.SIMPLE_STALK_TARGET) {
            return engine.createComponent(AIComponent.class).setAi(new SimpleStalkTarget(e, player, ecd.getRotationBehaviorParams(), ecd.getSimpleStalkMinSpeedDistance(), ecd.getSimpleStalkMaxSpeedDistance(), 0));
        } else if(ecd.getAiType() == AI.AIType.SIMPLE_WANDER) {
            return engine.createComponent(AIComponent.class).setAi(new SimpleWander(e, ecd.getRotationBehaviorParams(), ecd.getSimpleWanderRadius(), ecd.getSimpleWanderMinInterval(), ecd.getSimpleWanderMaxInterval(), ecd.getSimpleWanderMinAcceleration(), ecd.getSimpleWanderMaxAcceleration()));
        }
        return null;
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

    public float getMaxPixelPoints() {
        return maxPixelPoints;
    }

    public float getMinEnemiesPerMapArea() {
        return minEnemiesPerMapArea;
    }

    public float getMaxEnemiesPerMapArea() {
        return maxEnemiesPerMapArea;
    }

    public Collection<MapArea> getAllSavedMapAreas() {
        return areas.values();
    }

    public HashMap<Point, MapScreen.MapScreenArea> getDiscoveredAreaPositions() {
        HashMap<Point, MapScreen.MapScreenArea> mapScreenAreas = new HashMap<Point, MapScreen.MapScreenArea>();

        for(java.util.Map.Entry<String, MapArea> entry : areas.entrySet()) {
            MapArea mapArea = entry.getValue();

            MapScreen.MapScreenArea area = new MapScreen.MapScreenArea();
            area.areaCleared = (mapArea.getEnemyCount() == 0);

            // Create red circles on map area display equal to number of enemies in the area
            // Position of red circle depends on position of enemy spawn
            if(!area.areaCleared) {
                area.objectIndicators = new ArrayList<MapScreen.MapScreenObjectIndicator>();
                for(EntityCreationData ecd : mapArea.entityCreationDataArrayList) {
                    area.objectIndicators.add(new MapScreen.MapScreenObjectIndicator(
                            ecd.getSpawnX()/mapArea.getRadius() * MapScreen.MAP_AREA_BUTTON_RADIUS, ecd.getSpawnY()/mapArea.getRadius() * MapScreen.MAP_AREA_BUTTON_RADIUS,
                            ecd.getGravitationalRadius()/mapArea.getRadius() * MapScreen.MAP_AREA_BUTTON_RADIUS,
                            Color.RED
                    ));
                }

                if(mapArea.equals(currentArea)) {
                    // Create red circles from existing enemies instead
                    for(Entity e : main.getEngine().getEntitiesFor(Family.all(EnemyComponent.class, HitboxComponent.class).get())) {
                        Point pos = Mappers.hitbox.get(e).getOrigin();
                        area.objectIndicators.add(new MapScreen.MapScreenObjectIndicator(
                                pos.x/mapArea.getRadius() * MapScreen.MAP_AREA_BUTTON_RADIUS, pos.y/mapArea.getRadius() * MapScreen.MAP_AREA_BUTTON_RADIUS,
                                Mappers.hitbox.get(e).getGravitationalRadius()/mapArea.getRadius() * MapScreen.MAP_AREA_BUTTON_RADIUS * 2,
                                Color.RED
                        ));
                    }

                    // Create blue circle from player
                    HitboxComponent playerHitbox = Mappers.hitbox.get(main.getPlayer());
                    area.objectIndicators.add(new MapScreen.MapScreenObjectIndicator(
                            playerHitbox.getOrigin().x/mapArea.getRadius() * MapScreen.MAP_AREA_BUTTON_RADIUS, playerHitbox.getOrigin().y/mapArea.getRadius() * MapScreen.MAP_AREA_BUTTON_RADIUS,
                            playerHitbox.getGravitationalRadius()/mapArea.getRadius() * MapScreen.MAP_AREA_BUTTON_RADIUS * 2,
                            Color.BLUE
                    ));
                }
            }

            String[] strArr = entry.getKey().replaceAll("\\]|=|x|y|\\[|,", "").split("\\s+");
            area.x = Float.valueOf(strArr[0]);
            area.y = Float.valueOf(strArr[1]);

            //TODO: different color for uncommon/rare map areas
            /**
             * See {@link MapScreen#loadBubbleTextures()} for color indexes
             */
            if(mapArea.getStairsDestination() == -1) {
                if(mapArea.isUncommon()) {
                    area.colorIndex = 3;
                    area.borderColor = MapScreen.UNCOMMON_MAP_AREA_BORDER_COLOR;
                } else if(mapArea.isRare()) {
                    area.colorIndex = 2;
                    area.borderColor = MapScreen.RARE_MAP_AREA_BORDER_COLOR;
                } else {
                    area.colorIndex = 0;
                    area.borderColor = MapScreen.NORMAL_MAP_AREA_BORDER_COLOR;
                }
            } else {
                area.colorIndex = 1;
                area.borderColor = MapScreen.STAIRS_MAP_AREA_BORDER_COLOR;
            }

            mapScreenAreas.put(new Point(area.x, area.y), area);
        }

        return mapScreenAreas;
    }

    public void setNewMapAreasUntilBoss(float newMapAreasUntilBoss) {
        this.newMapAreasUntilBoss = newMapAreasUntilBoss;
    }
}
