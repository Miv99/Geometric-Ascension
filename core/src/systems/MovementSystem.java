package systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.miv.Camera;
import com.miv.EntityActions;
import com.badlogic.gdx.math.Vector2;
import com.miv.Main;
import com.miv.Mappers;
import com.miv.Options;

import java.util.ArrayList;

import components.EnemyBulletComponent;
import components.EnemyComponent;
import components.HitboxComponent;
import components.PlayerBulletComponent;
import components.PlayerComponent;
import map.Map;
import map.MapArea;
import utils.CircleHitbox;
import utils.OnCollisionEvent;
import utils.Point;
import utils.Utils;

/**
 * Created by Miv on 5/25/2017.
 */
public class MovementSystem extends EntitySystem {
    // Time after leaving map area that new map area begins loading
    private static final float NEW_MAP_AREA_LEAVE_TRAVEL_TIME = 1.25f;
    // Time after new map area loads until player enters bounds of new area
    private static final float NEW_MAP_AREA_ENTER_TRAVEL_TIME = 0.75f;

    private ImmutableArray<Entity> entities;
    private ArrayList<Entity> entityRemovalQueue;
    private ArrayList<Entity> collisionEntitiesToHandle;
    private ArrayList<CircleHitbox> collisionCirclesToHandle;
    private ImmutableArray<Entity> players;
    private ImmutableArray<Entity> playerBullets;
    private ImmutableArray<Entity> enemies;
    private ImmutableArray<Entity> enemyBullets;
    private ImmutableArray<Entity> enemiesAndPlayers;
    private PooledEngine engine;
    private Map map;
    private Main main;

    private Entity player;

    private Array<Sound> popSounds;

    public MovementSystem(Main main, PooledEngine engine, Map map, Entity player) {
        this.main = main;
        this.engine = engine;
        this.map = map;
        this.player = player;
        collisionCirclesToHandle = new ArrayList<CircleHitbox>();
        collisionEntitiesToHandle = new ArrayList<Entity>();
        entityRemovalQueue = new ArrayList<Entity>();
        popSounds = new Array<Sound>();
    }

    public void loadAssets(AssetManager assetManager) {
        popSounds.clear();
        for(String s : Main.POP_SOUND_PATHS) {
            Sound sound = assetManager.get(assetManager.getFileHandleResolver().resolve(s).path());
            popSounds.add(sound);
        }
    }

    @Override
    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(HitboxComponent.class).get());
        players = engine.getEntitiesFor(Family.all(PlayerComponent.class).get());
        playerBullets = engine.getEntitiesFor(Family.all(PlayerBulletComponent.class).get());
        enemies = engine.getEntitiesFor(Family.all(EnemyComponent.class).get());
        enemyBullets = engine.getEntitiesFor(Family.all(EnemyBulletComponent.class).get());
        enemiesAndPlayers = engine.getEntitiesFor(Family.one(EnemyComponent.class, PlayerComponent.class).get());
    }

    @Override
    public void removedFromEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(HitboxComponent.class).get());
        players = engine.getEntitiesFor(Family.all(PlayerComponent.class).get());
        playerBullets = engine.getEntitiesFor(Family.all(PlayerBulletComponent.class).get());
        enemies = engine.getEntitiesFor(Family.all(EnemyComponent.class).get());
        enemyBullets = engine.getEntitiesFor(Family.all(EnemyBulletComponent.class).get());
        enemiesAndPlayers = engine.getEntitiesFor(Family.one(EnemyComponent.class, PlayerComponent.class).get());
    }

    /**
     * Stores entities/circle hitboxes affected in collisionEntitiesToHandle and circleCollisionsToHandle to save memory
     */
    private void checkForCollision(Point circleOrigin, CircleHitbox c, ImmutableArray<Entity> arrayOfEntities) {
        collisionEntitiesToHandle.clear();
        collisionCirclesToHandle.clear();
        for (Entity entity : arrayOfEntities) {
            HitboxComponent entityHitbox = Mappers.hitbox.get(entity);
            Point entityHitboxCircleOrigin = entityHitbox.getOrigin();
            for (CircleHitbox entityHitboxCircle : entityHitbox.getCircles()) {
                if (((c.x + circleOrigin.x) - (entityHitboxCircle.x + entityHitboxCircleOrigin.x))*(((c.x + circleOrigin.x) - (entityHitboxCircle.x + entityHitboxCircleOrigin.x)))
                        + ((c.y + circleOrigin.y) - (entityHitboxCircle.y + entityHitboxCircleOrigin.y))*((c.y + circleOrigin.y) - (entityHitboxCircle.y + entityHitboxCircleOrigin.y)) <= (c.radius + entityHitboxCircle.radius)*(c.radius + entityHitboxCircle.radius)) {
                    collisionEntitiesToHandle.add(entity);
                    collisionCirclesToHandle.add(entityHitboxCircle);
                }
            }
        }
    }

    private void handleBulletCollision(Entity victim, CircleHitbox victimCircleHit, Entity bullet) {
        if(!entityRemovalQueue.contains(bullet)) {
            float damage = 0;
            if (Mappers.enemyBullet.has(bullet)) {
                damage = Mappers.enemyBullet.get(bullet).getDamage();
            } else if (Mappers.playerBullet.has(bullet)) {
                damage = Mappers.playerBullet.get(bullet).getDamage();
            }

            // Victim takes damage
            HitboxComponent victimHitbox = Mappers.hitbox.get(victim);
            victimCircleHit.setHealth(victimCircleHit.getHealth() - damage);

            if (victimCircleHit.getHealth() <= 0) {
                //TODO: circle death animation
                victimHitbox.queueCircleRemoval(victimCircleHit);

                if (Mappers.enemy.has(victim)) {
                    Mappers.player.get(player).addPixelPoints(main, victimCircleHit.getPpGain());

                    // TODO: remove this
                    if (victimCircleHit.getPpGain() <= 0) {
                        System.out.println("you messed up; a circle has no pp gain for some reason 1374dskjfsd9");
                    }
                }

                // All hit circles considered dead when number of circles is 1 because size() is not updated until
                // the circle removal queue is fired.
                if (victimHitbox.getCircles().size() == 1) {
                    // Queue entity removal from engine
                    entityRemovalQueue.add(victim);
                }
            }

            // Queue bullet entity removal
            entityRemovalQueue.add(bullet);

            // Play pop sound
            popSounds.random().play(Options.BULLET_BUBBLE_POP_VOLUME);
        }
    }

    private boolean bulletIsOutsideBoundary(Entity e, Point origin, float boundary) {
        return Math.abs(origin.x) > Math.abs(boundary) || Math.abs(origin.y) > Math.abs(boundary);
    }

    private boolean checkIfOutsideCurrentMapArea(Entity e, Point origin, CircleHitbox c, float boundary) {
        if(origin.x*origin.x + origin.y*origin.y > boundary*boundary) {
            float angle = MathUtils.atan2(origin.y, origin.x);
            angle = Utils.normalizeAngle(angle);
            if(angle >= Math.PI/4f && angle <= 3f * Math.PI/4f) {
                EntityActions.playerEnterNewMapArea(e, EntityActions.Direction.UP);
            } else if(angle >= 3f * Math.PI/4f && angle <= 5f * Math.PI/4f) {
                EntityActions.playerEnterNewMapArea(e, EntityActions.Direction.LEFT);
            } else if(angle >= 5f * Math.PI/4f && angle <= 7f * Math.PI/4f) {
                EntityActions.playerEnterNewMapArea(e, EntityActions.Direction.DOWN);
            } else {
                EntityActions.playerEnterNewMapArea(e, EntityActions.Direction.RIGHT);
            }
        }
        return false;
    }

    /**
     * Returns the change in velocity due to an entity's proximity to other nearby entities and/or the map area border
     * @param entities - all entities affected by gravity
     * @param entity - the entity whose velocity's change is being calculated for
     * @param entityOrigin - origin of e
     */
    private Point calculateVelocityAdditionDueToGravity(ImmutableArray<Entity> entities, Entity entity, Point entityOrigin) {
        Point vel = new Point(0, 0);

        if(!Mappers.hitbox.get(entity).isIgnoreGravity()) {
            for (Entity e : entities) {
                if (!e.equals(entity)) {
                    HitboxComponent hitbox = Mappers.hitbox.get(e);
                    if (!hitbox.isIgnoreGravity()) {
                        Point origin = hitbox.getOrigin();
                        float distance = Utils.getDistance(origin, entityOrigin);
                        if (distance < Options.GRAVITY_DROP_OFF_DISTANCE + hitbox.getGravitationalRadius()) {
                            float angle = MathUtils.atan2(entityOrigin.y - origin.y, entityOrigin.x - origin.x);

                            // Prevent division by 0
                            if (distance == 0) {
                                distance = 1f;
                            }

                            float magnitude = Options.GRAVITATIONAL_CONSTANT / (float) Math.pow(distance, 1.2);

                            vel.x += magnitude * MathUtils.cos(angle);
                            vel.y += magnitude * MathUtils.sin(angle);
                        }
                    }
                }
            }

            float distanceFromMapAreaCenter = Utils.getDistance(entityOrigin, 0, 0);
            if (distanceFromMapAreaCenter > map.getCurrentArea().getRadius() - Options.GRAVITY_DROP_OFF_DISTANCE) {
                float angle = MathUtils.atan2(entityOrigin.y, entityOrigin.x);
                float magnitude;

                if (distanceFromMapAreaCenter < map.getCurrentArea().getRadius() && distanceFromMapAreaCenter != map.getCurrentArea().getRadius()) {
                    magnitude = Options.GRAVITATIONAL_CONSTANT / (float) Math.pow(map.getCurrentArea().getRadius() - distanceFromMapAreaCenter, 1.2);
                } else {
                    // Treat being outside the map area border as being repelled with the same force as being 1m away from the border
                    magnitude = Options.GRAVITATIONAL_CONSTANT / (float) Math.pow(1f, 1.2);
                }

                vel.x -= magnitude * MathUtils.cos(angle);
                vel.y -= magnitude * MathUtils.sin(angle);
            }

            if (vel.x > 0) {
                vel.x = Math.min(Options.GRAVITY_SPEED_CAP, vel.x);
            } else {
                vel.x = Math.max(-Options.GRAVITY_SPEED_CAP, vel.x);
            }
            if (vel.y > 0) {
                vel.y = Math.min(Options.GRAVITY_SPEED_CAP, vel.y);
            } else {
                vel.y = Math.max(-Options.GRAVITY_SPEED_CAP, vel.y);
            }
        }

        return vel;
    }

    @Override
    public void update(float deltaTime) {
        MapArea mapArea = map.getCurrentArea();

        for (Entity e : entities) {
            HitboxComponent hitbox = Mappers.hitbox.get(e);
            Point hitboxOrigin = hitbox.getOrigin();
            Point origin = hitbox.getOrigin();
            Vector2 velocity = hitbox.getVelocity();
            Point velocityAdditionDueToGravity = null;

            boolean isValidMovement = true;

            if (!hitbox.isIntangible() && !hitbox.isTravelling()) {
                // If entity is a player, check for collisions against the edges of the MapArea, enemies, enemy bullets
                if (Mappers.player.has(e)) {
                    for (CircleHitbox c : hitbox.getCircles()) {
                        // Check if circle is outside map area radius
                        if(mapArea != null) {
                            checkIfOutsideCurrentMapArea(e, origin, c, mapArea.getRadius());
                        }

                        // Check against enemies
                        /**
                        checkForCollision(hitboxOrigin, c, enemies);
                        for(int i = 0; i < collisionEntitiesToHandle.size(); i++) {
                            isValidMovement = false;
                            handleNonBulletCollision(e, collisionEntitiesToHandle.get(i));
                        }
                         */

                        // Check against enemy bullets
                        checkForCollision(hitboxOrigin, c, enemyBullets);
                        for(int i = 0; i < collisionEntitiesToHandle.size(); i++) {
                            isValidMovement = false;
                            handleBulletCollision(e, c, collisionEntitiesToHandle.get(i));
                        }
                    }
                }
                // If entity is an enemy, check for collisions against the edges of the MapArea, players, player bullets
                else if (Mappers.enemy.has(e)) {
                    for (CircleHitbox c : hitbox.getCircles()) {
                        /**
                        // Against players
                        checkForCollision(hitboxOrigin, c, players);
                        for(int i = 0; i < collisionEntitiesToHandle.size(); i++) {
                            isValidMovement = false;
                            handleNonBulletCollision(e, collisionEntitiesToHandle.get(i));
                        }
                         */

                        // Against player bullets
                        checkForCollision(hitboxOrigin, c, playerBullets);
                        for(int i = 0; i < collisionEntitiesToHandle.size(); i++) {
                            isValidMovement = false;
                            handleBulletCollision(e, c, collisionEntitiesToHandle.get(i));
                        }
                    }

                    // Calculate effect of gravity
                    velocityAdditionDueToGravity = calculateVelocityAdditionDueToGravity(enemiesAndPlayers, e, origin);
                }
                // If entity is a player bullet, check for collisions against the square boundaries of the MapArea, enemies
                else if (Mappers.playerBullet.has(e)) {
                    // Square boundaries have side length of 4x the radius
                    for (CircleHitbox c : hitbox.getCircles()) {
                        // Check if circle is outside map area radius
                        if(mapArea != null) {
                            if(bulletIsOutsideBoundary(e, origin, mapArea.getRadius() * 1.5f)) {
                                entityRemovalQueue.add(e);
                            }
                        }

                        // Against enemies
                        checkForCollision(hitboxOrigin, c, enemies);
                        for(int i = 0; i < collisionEntitiesToHandle.size(); i++) {
                            isValidMovement = false;
                            handleBulletCollision(collisionEntitiesToHandle.get(i), collisionCirclesToHandle.get(i), e);
                        }
                    }
                }
                // If entity is an enemy bullet, check for collisions against the square boundaries of the MapArea, players
                else if (Mappers.enemyBullet.has(e)) {
                    for (CircleHitbox c : hitbox.getCircles()) {
                        // Against MapArea
                        if(mapArea != null) {
                            if(bulletIsOutsideBoundary(e, origin, mapArea.getRadius() * 1.5f)) {
                                entityRemovalQueue.add(e);
                            }
                        }

                        // Check for collision against the players
                        checkForCollision(hitboxOrigin, c, players);
                        for(int i = 0; i < collisionEntitiesToHandle.size(); i++) {
                            isValidMovement = false;
                            handleBulletCollision(collisionEntitiesToHandle.get(i), collisionCirclesToHandle.get(i), e);
                        }
                    }
                }
            } else if(hitbox.isTravelling()) {
                // I already know this is bad code; it's used only for player travelling
                if(!hitbox.isTravellingFlag() && (hitbox.getTravellingTime() > NEW_MAP_AREA_LEAVE_TRAVEL_TIME || mapAreaIsOutOfCameraRange())) {
                    EntityActions.Direction directionOfTravel = hitbox.getTravellingDirection();

                    map.enterNewArea(engine, e, (int)map.getFocus().x + directionOfTravel.getDeltaX(), (int)map.getFocus().y + directionOfTravel.getDeltaY(), false);

                    // Set position of player so that the player will enter the new map area in a certain amount of time
                    final float newMapAreaRadius = map.getCurrentArea().getRadius();

                    Camera camera = map.getMain().getCamera();
                    float cameraDistanceXFromPlayer = hitbox.getOrigin().x - camera.position.x;
                    float cameraDistanceYFromPlayer = hitbox.getOrigin().y - camera.position.y;

                    hitbox.setOrigin(-directionOfTravel.getDeltaX() * newMapAreaRadius - (directionOfTravel.getDeltaX() * NEW_MAP_AREA_ENTER_TRAVEL_TIME * hitbox.getTravellingSpeed() * Options.GLOBAL_MOVEMENT_SPEED_MULTIPLIER),
                            -directionOfTravel.getDeltaY() * newMapAreaRadius - (directionOfTravel.getDeltaY() * NEW_MAP_AREA_ENTER_TRAVEL_TIME * hitbox.getTravellingSpeed() * Options.GLOBAL_MOVEMENT_SPEED_MULTIPLIER));
                    // Instantly teleport camera to the same distance behind player from before to have illusion of smooth travel
                    camera.position.x = hitboxOrigin.x - cameraDistanceXFromPlayer;
                    camera.position.y = hitboxOrigin.y - cameraDistanceYFromPlayer;

                    hitbox.setVelocity(directionOfTravel.getDeltaX() * hitbox.getTravellingSpeed(), directionOfTravel.getDeltaY() * hitbox.getTravellingSpeed());
                    hitbox.setTravellingDestination(new Point(-directionOfTravel.getDeltaX() * newMapAreaRadius + (directionOfTravel.getDeltaX() * hitbox.getGravitationalRadius() * 2.5f),
                            -directionOfTravel.getDeltaY() * newMapAreaRadius + (directionOfTravel.getDeltaY() * hitbox.getGravitationalRadius() * 2.5f)));

                    hitbox.setTravellingFlag(true);
                    hitbox.setTravellingTime(0);
                } else if(hitbox.isTravellingFlag()
                        && (hitbox.getTravellingTime() > NEW_MAP_AREA_ENTER_TRAVEL_TIME || hitbox.isPastTravellingDestination())) {
                    EntityActions.Direction directionOfTravel = hitbox.getTravellingDirection();

                    // Set position of player in new map area
                    float newMapAreaRadius = map.getCurrentArea().getRadius();
                    if(!hitbox.isPastTravellingDestination()) {
                        hitbox.setOrigin(-directionOfTravel.getDeltaX() * newMapAreaRadius + (directionOfTravel.getDeltaX() * hitbox.getGravitationalRadius() * 2.5f),
                                -directionOfTravel.getDeltaY() * newMapAreaRadius + (directionOfTravel.getDeltaY() * hitbox.getGravitationalRadius() * 2.5f));
                    }
                    hitbox.setVelocity(0, 0);

                    // Save game
                    main.save();
                    // Manually clear new map area entityCreationData to avoid saving an empty one to the current map area
                    map.getCurrentArea().entityCreationDataArrayList.clear();

                    // Make player no longer invincible
                    hitbox.setIntangible(false);

                    hitbox.setTravelling(false);
                    hitbox.setIgnoreSpeedLimit(false);

                    hitbox.setTravellingFlag(false);
                    hitbox.setTravellingTime(0);
                } else {
                    hitbox.setTravellingTime(hitbox.getTravellingTime() + deltaTime);
                }
            }

            if (isValidMovement) {
                float deltaX = velocity.x * deltaTime * Options.GLOBAL_MOVEMENT_SPEED_MULTIPLIER;
                float deltaY = velocity.y * deltaTime * Options.GLOBAL_MOVEMENT_SPEED_MULTIPLIER;

                if(velocityAdditionDueToGravity == null) {
                    hitbox.setOrigin(origin.x + deltaX, origin.y + deltaY);
                } else {
                    hitbox.setOrigin(origin.x + velocity.x + velocityAdditionDueToGravity.x, origin.y + velocity.y + velocityAdditionDueToGravity.y);
                }
                hitbox.setVelocity(velocity.x + hitbox.getAcceleration().x, velocity.y + hitbox.getAcceleration().y);
            }

            // Remove circles in hitbox circle removal queue from array list of circles in the hitbox component
            for(CircleHitbox c : hitbox.getCircleRemovalQueue()) {
                if(Mappers.player.has(e)) {
                    popSounds.random().play(Options.PLAYER_BUBBLE_POP_VOLUME);
                } else if(Mappers.enemy.has(e)) {
                    popSounds.random().play(Options.ENEMY_BUBBLE_POP_VOLUME);
                }

                ArrayList<Entity> subEntities = hitbox.removeCircle(engine, e, c);
                if(subEntities != null) {
                    map.getCurrentArea().setEnemyCount(map.getCurrentArea().getEnemyCount() + subEntities.size());
                    for (Entity sub : subEntities) {
                        engine.addEntity(sub);
                    }
                }
            }
            hitbox.clearCircleRemovalQueue();
        }

        // Remove entities in entity removal queue from engine
        for(Entity e : entityRemovalQueue) {
            engine.removeEntity(e);

            if(Mappers.player.has(e)) {
                map.getMain().onPlayerDeath();
            } else if(Mappers.enemy.has(e)) {
                map.getCurrentArea().setEnemyCount(main, engine, players.first(), map, map.getCurrentArea().getEnemyCount() - 1);

                if(map.getCurrentArea().getEnemyCount() == 0) {
                    // Bonus pp for killing all enemies
                    float bonusPp = map.getCurrentArea().getOriginalEnemyCount()/((map.getMinEnemiesPerMapArea() + map.getMaxEnemiesPerMapArea())/2f) * map.getMaxPixelPoints() * Options.BONUS_PP_MULTIPLIER;
                    Mappers.player.get(player).addPixelPoints(main, bonusPp);

                    // Save
                    main.save();
                }
            }
        }
        entityRemovalQueue.clear();
    }

    private boolean mapAreaIsOutOfCameraRange() {
        return Math.abs(map.getMain().getCamera().position.x) - map.getMain().getCamera().viewportWidth/1.7f > map.getCurrentArea().getRadius()
                || Math.abs(map.getMain().getCamera().position.y) - map.getMain().getCamera().viewportHeight/1.7f > map.getCurrentArea().getRadius();
    }

    public void setMap(Map map) {
        this.map = map;
    }

    public void setPlayer(Entity player) {
        this.player = player;
    }
}
