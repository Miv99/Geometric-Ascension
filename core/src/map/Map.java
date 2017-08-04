package map;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.math.MathUtils;
import com.miv.AttackPart;
import com.miv.AttackPattern;
import com.miv.Main;
import com.miv.Mappers;
import com.miv.Options;

import java.util.ArrayList;
import java.util.HashMap;

import ai.AI;
import ai.SimpleFollowTarget;
import ai.SimpleStalkTarget;
import ai.SimpleWander;
import components.AIComponent;
import components.EnemyBulletComponent;
import components.EnemyComponent;
import components.HitboxComponent;
import components.IgnoreRespawnOnAreaResetComponent;
import components.PlayerBulletComponent;
import factories.AttackPatternFactory;
import factories.BossFactory;
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
    public static class GridLine {
        private float startX;
        private float startY;
        private float endX;
        private float endY;

        public float getStartX() {
            return startX;
        }

        public float getStartY() {
            return startY;
        }

        public float getEndX() {
            return endX;
        }

        public float getEndY() {
            return endY;
        }
    }

    //------------------------------------------------------------- MAP AREA GENERATION --------------------------------------------------
    private static final float MAX_CHANCE_OF_STAIRS_AREA = 1;
    /**
     * How much {@link map.Map#chanceOfNextAreaHavingStairs} increases by each time a new MapArea is discovered
     */
    private static final float CHANCE_OF_STAIRS_AREA_INCREMENT = 0.01f;

    public static final float INITIAL_MAP_AREA_PIXEL_POINTS = 20f;
    /**
     * How much {@link map.Map#maxPixelPoints} increases by each time the player enters a new floor
     */
    private static final float MAP_AREA_PIXEL_POINTS_INCREMENT = 2.5f;

    private static final int MIN_ENEMIES_PER_MAP_AREA = 3;
    private static final int MAX_ENEMIES_PER_MAP_AREA = 8;
    //-----------------------------------------------------------------------------------------------------

    private static final float GRID_LINE_SEPARATION_DISTANCE = 150f;

    private transient Main main;

    // Starts at 0
    private int floor;
    private Point focus;
    // Maps location on the world map to a specific MapArea
    private HashMap<String, MapArea> areas;
    // Percent chance of the next undiscovered MapArea being having a stairway (0 to MAX_CHANCE_OF_STAIRS_AREA)*100 %
    private float chanceOfNextAreaHavingStairs;
    // Maximum pixel points, distributed evenly to all enemies, when generating MapAreas
    private float maxPixelPoints;

    // min/max enemies per map area, affected by floor
    private float minEnemiesPerMapArea;
    private float maxEnemiesPerMapArea;

    private MapArea currentArea;

    // Positions of grid lines; purely visual effects
    private ArrayList<GridLine> gridLines;

    /**
     * For Json files
     */
    public Map() {}

    public Map(Main main) {
        this.main = main;

        gridLines = new ArrayList<GridLine>();
        areas = new HashMap<String, MapArea>();
        focus = new Point(0, 0);
        chanceOfNextAreaHavingStairs = 0f;
        maxPixelPoints = INITIAL_MAP_AREA_PIXEL_POINTS;

        minEnemiesPerMapArea = MIN_ENEMIES_PER_MAP_AREA;
        maxEnemiesPerMapArea = MAX_ENEMIES_PER_MAP_AREA;
    }

    public void enterNewFloor(int floor) {
        this.floor = floor;
        setFocus(0, 0);
        areas.clear();

        chanceOfNextAreaHavingStairs = 0f;
        maxPixelPoints = INITIAL_MAP_AREA_PIXEL_POINTS + MAP_AREA_PIXEL_POINTS_INCREMENT*(float)floor;

        float enemyCountIncrease = floor/10f;
        minEnemiesPerMapArea = MIN_ENEMIES_PER_MAP_AREA + enemyCountIncrease/2f;
        maxEnemiesPerMapArea = MAX_ENEMIES_PER_MAP_AREA + enemyCountIncrease;

        // First MapArea always has stairs leading to the previous floor
        MapArea mapArea = new MapArea(MapArea.MAP_AREA_MIN_SIZE);
        areas.put(new Point(0, 0).toString(), mapArea);
        Mappers.hitbox.get(main.getPlayer()).setOrigin(0, 0);
        enterNewArea(main.getEngine(), main.getPlayer(), 0, 0, true);

        main.save();
    }

    public void enterNewArea(PooledEngine engine, Entity player, int x, int y, boolean clearNewMapAreaEntityCreationDataAfterSpawningEnemies) {
        MapArea oldMapArea = areas.get(focus.toString());

        boolean increaseChanceOfNextAreaHavingStairs = false;

        MapArea newMapArea;
        Point newPos = new Point(x, y);
        if(!areas.containsKey(newPos.toString())) {
            increaseChanceOfNextAreaHavingStairs = true;
            newMapArea = generateRandomMapArea(newPos);
            areas.put(newPos.toString(), newMapArea);
        } else {
            newMapArea = areas.get(new Point(x, y).toString());
        }
        currentArea = newMapArea;

        // Calculate grid line locations
        gridLines.clear();
        float gridPadding = (newMapArea.getRadius() % GRID_LINE_SEPARATION_DISTANCE)/2f;
        // Lines from quadrants 1 and 2, extending down
        for(float gridX = -newMapArea.getRadius() + gridPadding; gridX < newMapArea.getRadius() - gridPadding; gridX += GRID_LINE_SEPARATION_DISTANCE) {
            GridLine gl = new GridLine();
            gl.startX = gridX;
            gl.startY = (float)Math.sqrt(newMapArea.getRadius()*newMapArea.getRadius() - gridX*gridX);
            gl.endX = gridX;
            gl.endY = -gl.startY;
            gridLines.add(gl);
        }
        // Lines from quadrants 1 and 4, extending left
        for(float gridY = -newMapArea.getRadius() + gridPadding; gridY < newMapArea.getRadius() - gridPadding; gridY += GRID_LINE_SEPARATION_DISTANCE) {
            GridLine gl = new GridLine();
            gl.startX = (float)Math.sqrt(newMapArea.getRadius()*newMapArea.getRadius() - gridY*gridY);
            gl.startY = gridY;
            gl.endX = -gl.startX;
            gl.endY = gridY;
            gridLines.add(gl);
        }

        /**
         * Store all enemies currently in the engine as {@link map.EntityCreationData} objects inside {@link MapArea#entityCreationDataArrayList}
         */
        if(oldMapArea != null && !(focus.x == x && focus.y == y)) {
            oldMapArea.storeExistingEnemies(engine, true);
        }

        newMapArea.spawnEntities(engine, player, clearNewMapAreaEntityCreationDataAfterSpawningEnemies);

        focus.x = x;
        focus.y = y;

        // Increase chance of next area having stairs after autosaving to avoid the user entering new areas and
        // reloading the game to avoid all enemies and quickly enter new floors
        if(increaseChanceOfNextAreaHavingStairs && (x != 0 && y != 0)) {
            chanceOfNextAreaHavingStairs = Math.min(MAX_CHANCE_OF_STAIRS_AREA, chanceOfNextAreaHavingStairs + CHANCE_OF_STAIRS_AREA_INCREMENT);
        }
    }

    private MapArea generateRandomMapArea(Point pos) {
        MapArea mapArea;
        if(pos.x == 0 && pos.y == 0) {
            mapArea = new MapArea(MapArea.MAP_AREA_MIN_SIZE);
        } else {
            if(Math.random() < chanceOfNextAreaHavingStairs) {
                mapArea = new MapArea(BOSS_MAP_AREA_SIZE);
                mapArea.addStairs(floor + 1);
                populateWithBoss(mapArea);
            } else {
                mapArea = new MapArea(MathUtils.random(MapArea.MAP_AREA_MIN_SIZE, MapArea.MAP_AREA_MAX_SIZE));
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

        // Array list of circles that surround each enemy's hitbox
        // Used to avoid spawning enemies too close to each other
        ArrayList<CircleHitbox> enemyBoundingCircles = new ArrayList<CircleHitbox>();

        for(int i = 0; i < enemies; i++) {
            EntityCreationData ecd = new EntityCreationData();
            ecd.setIsEnemy(true);

            randomizeEnemyMovementAI(ecd, mapArea.getRadius());

            // Max speed is a random number between 1f and 5f
            ecd.setMaxSpeed(MathUtils.random(1f, 3f));

            // Max health is 500% to 2000% of total pp
            ecd.setMaxHealth(ppPerEnemy * MathUtils.random(5f, 20f));

            ArrayList<CircleHitbox> circles = ecd.getCircleHitboxes();

            AttackPattern attackPattern = AttackPatternFactory.getRandomAttackPatternByFloor(floor);
            float[] attackPatternStats = attackPattern.getAttackPatternStatModifiers(ppPerEnemy);
            attackPattern.modify(attackPatternStats[0], attackPatternStats[1], attackPatternStats[2], attackPatternStats[3]);

            CircleHitbox c1 = new CircleHitbox();

            // If circle hitbox contains more than 1 circle, each circle except the first is placed
            // so that it is tangential to the first circle
            int circlesCount = getEnemyRandomCirclesCount();
            float c1Radius = getRandomCircleRadius(circlesCount);
            for(int a = 1; a < circlesCount; a++) {
                CircleHitbox c = new CircleHitbox();

                c.setPpGain(ppPerEnemy / circlesCount * Options.PP_GAIN_MULTIPLIER);

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

            c1.setPpGain(ppPerEnemy / circlesCount * Options.PP_GAIN_MULTIPLIER);

            // Scale circle health to radius
            float c1Health = c1Radius/totalCircleRadius * ecd.getMaxHealth();
            c1.setMaxHealth(c1Health);
            c1.setHealth(c1Health);
            for(int a = 0; a < circlesCount - 1; a++) {
                CircleHitbox c = circles.get(a);

                // Set health
                float health = c.radius/totalCircleRadius * ecd.getMaxHealth();
                c.setMaxHealth(health);
                c.setHealth(health);
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
        ecd.setAiType(AI.AIType.SIMPLE_STALK_TARGET);
        ecd.setSimpleStalkMinSpeedDistance(MathUtils.random(100f, 250f));
        ecd.setSimpleStalkMaxSpeedDistance(MathUtils.random(330f, 450f));
    }

    public static void randomizeSimpleWanderAI(EntityCreationData ecd, float mapAreaRadius) {
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
           return engine.createComponent(AIComponent.class).setAi(new SimpleFollowTarget(e, player));
        } else if(ecd.getAiType() == AI.AIType.SIMPLE_STALK_TARGET) {
            return engine.createComponent(AIComponent.class).setAi(new SimpleStalkTarget(e, player, ecd.getSimpleStalkMinSpeedDistance(), ecd.getSimpleStalkMaxSpeedDistance(), 0));
        } else if(ecd.getAiType() == AI.AIType.SIMPLE_WANDER) {
            return engine.createComponent(AIComponent.class).setAi(new SimpleWander(e, ecd.getSimpleWanderRadius(), ecd.getSimpleWanderMinInterval(), ecd.getSimpleWanderMaxInterval(), ecd.getSimpleWanderMinAcceleration(), ecd.getSimpleWanderMaxAcceleration()));
        } else {
            System.out.println("347SJDFIODS CREATING AI COMPONENT FROM NULL AITYPE ???");
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

    public ArrayList<GridLine> getGridLines() {
        return gridLines;
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
}
