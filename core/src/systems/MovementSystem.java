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
        if(!entityRemovalQueue.contains(bullet) && !entityRemovalQueue.contains(victim)) {
            HitboxComponent victimHitbox = Mappers.hitbox.get(victim);
            if(!victimHitbox.isIntangible()) {
                float damage = 0;
                if (Mappers.enemyBullet.has(bullet)) {
                    damage = Mappers.enemyBullet.get(bullet).getDamage();
                    // Parent gets healed
                    Mappers.hitbox.get(Mappers.enemyBullet.get(bullet).getEntityToBeHealed()).healWeakestCircle(damage * Mappers.enemyBullet.get(bullet).getLifestealMultiplier());
                } else if (Mappers.playerBullet.has(bullet)) {
                    damage = Mappers.playerBullet.get(bullet).getDamage();
                    Mappers.hitbox.get(Mappers.playerBullet.get(bullet).getEntityToBeHealed()).healWeakestCircle(damage * Mappers.enemyBullet.get(bullet).getLifestealMultiplier());
                }

                // Victim takes damage
                victimCircleHit.setHealth(victimCircleHit.getHealth() - damage);


                if (victimCircleHit.getHealth() <= 0) {
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

    private boolean checkIfOutsideCurrentMapArea(Entity e, Point origin, Vector2 velocity, float boundary) {
        if(origin.x*origin.x + origin.y*origin.y > boundary*boundary) {
            // Angle depends on direction the entity is currently travelling in
            float angle = Utils.normalizeAngle(MathUtils.atan2(velocity.y, velocity.x));
            if (angle >= Math.PI / 4f && angle <= 3f * Math.PI / 4f) {
                EntityActions.playerEnterNewMapArea(e, MathUtils.cos(angle), MathUtils.sin(angle), new Point(map.getFocus().x, map.getFocus().y + 1));
            } else if (angle >= 3f * Math.PI / 4f && angle <= 5f * Math.PI / 4f) {
                EntityActions.playerEnterNewMapArea(e, MathUtils.cos(angle), MathUtils.sin(angle), new Point(map.getFocus().x - 1, map.getFocus().y));
            } else if (angle >= 5f * Math.PI / 4f && angle <= 7f * Math.PI / 4f) {
                EntityActions.playerEnterNewMapArea(e, MathUtils.cos(angle), MathUtils.sin(angle), new Point(map.getFocus().x, map.getFocus().y - 1));
            } else {
                EntityActions.playerEnterNewMapArea(e, MathUtils.cos(angle), MathUtils.sin(angle), new Point(map.getFocus().x + 1, map.getFocus().y));
            }
            /**
             * // 4 cardinal directions
             if (angle >= 3f * Math.PI / 8f && angle <= 5f * Math.PI / 8f) {
             EntityActions.playerEnterNewMapArea(e, MathUtils.cos(angle), MathUtils.sin(angle), new Point(map.getFocus().x, map.getFocus().y + 1));
             } else if (angle >= 7f * Math.PI / 8f && angle <= 9f * Math.PI / 8f) {
             EntityActions.playerEnterNewMapArea(e, MathUtils.cos(angle), MathUtils.sin(angle), new Point(map.getFocus().x - 1, map.getFocus().y));
             } else if (angle >= 11f * Math.PI / 8f && angle <= 13f * Math.PI / 8f) {
             EntityActions.playerEnterNewMapArea(e, MathUtils.cos(angle), MathUtils.sin(angle), new Point(map.getFocus().x, map.getFocus().y - 1));
             } else if (angle >= 15f * Math.PI / 8f || angle <= Math.PI / 8f) {
             EntityActions.playerEnterNewMapArea(e, MathUtils.cos(angle), MathUtils.sin(angle), new Point(map.getFocus().x + 1, map.getFocus().y));
             }
             // Primary inter-cardinal directions
             else if (angle >= Math.PI / 8f && angle <= 3f * Math.PI / 8f) {
             EntityActions.playerEnterNewMapArea(e, MathUtils.cos(angle), MathUtils.sin(angle), new Point(map.getFocus().x + 1, map.getFocus().y + 1));
             } else if (angle >= 5f * Math.PI / 8f && angle <= 7f * Math.PI / 8f) {
             EntityActions.playerEnterNewMapArea(e, MathUtils.cos(angle), MathUtils.sin(angle), new Point(map.getFocus().x - 1, map.getFocus().y + 1));
             } else if (angle >= 9f * Math.PI / 8f && angle <= 11f * Math.PI / 8f) {
             EntityActions.playerEnterNewMapArea(e, MathUtils.cos(angle), MathUtils.sin(angle), new Point(map.getFocus().x - 1, map.getFocus().y - 1));
             } else if (angle >= 13f * Math.PI / 8f && angle <= 15f * Math.PI / 8f) {
             EntityActions.playerEnterNewMapArea(e, MathUtils.cos(angle), MathUtils.sin(angle), new Point(map.getFocus().x + 1, map.getFocus().y - 1));
             }
             */
            return true;
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
            Point origin = hitbox.getOrigin();
            Vector2 velocity = hitbox.getVelocity();
            Point velocityAdditionDueToGravity = null;

            boolean isValidMovement = true;

            if (!hitbox.isIntangible() && !hitbox.isTravelling() && !hitbox.isDisabledMovement()) {
                // If entity is a player, check for collisions against the edges of the MapArea, enemies, enemy bullets
                if (Mappers.player.has(e)) {
                    // Check if circle is outside map area radius
                    if(mapArea != null) {
                        // Player cannot leave boss area
                        checkIfOutsideCurrentMapArea(e, origin, hitbox.getVelocity(), mapArea.getRadius());
                    }

                    for (CircleHitbox c : hitbox.getCircles()) {
                        // Check against enemy bullets
                        checkForCollision(origin, c, enemyBullets);
                        for(int i = 0; i < collisionEntitiesToHandle.size(); i++) {
                            isValidMovement = false;
                            handleBulletCollision(e, c, collisionEntitiesToHandle.get(i));
                        }
                    }
                }
                // If entity is an enemy, check for collisions against the edges of the MapArea, players, player bullets
                else if (Mappers.enemy.has(e)) {
                    for (CircleHitbox c : hitbox.getCircles()) {
                        // Against player bullets
                        checkForCollision(origin, c, playerBullets);
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
                        checkForCollision(origin, c, enemies);
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
                        checkForCollision(origin, c, players);
                        for(int i = 0; i < collisionEntitiesToHandle.size(); i++) {
                            isValidMovement = false;
                            handleBulletCollision(collisionEntitiesToHandle.get(i), collisionCirclesToHandle.get(i), e);
                        }
                    }
                }
            } else if(hitbox.isTravelling()) {
                // I already know this is bad code; it's used only for player travelling
                if(!hitbox.isTravellingFlag() && mapAreaIsOutOfCameraRange()) {
                    if(!map.getCurrentArea().isBossArea()) {
                        map.enterNewArea(engine, e, (int)hitbox.getTravellingMapAreaDestination().x, (int)hitbox.getTravellingMapAreaDestination().y, false);
                        hitbox.setTravellingFromSameMapArea(false);
                    } else {
                        hitbox.setTravellingFromSameMapArea(true);
                    }

                    // Set position of player so that the player will enter the new map area in a certain amount of time
                    final float newMapAreaRadius = map.getCurrentArea().getRadius();

                    Camera camera = map.getMain().getCamera();
                    float cameraDistanceXFromPlayer = hitbox.getOrigin().x - camera.position.x;
                    float cameraDistanceYFromPlayer = hitbox.getOrigin().y - camera.position.y;

                    // No idea why these if statements are necessary
                    float newOriginX = 0;
                    float newOriginY = 0;
                    if(hitbox.getTravellingDirectionX() > 0) {
                        newOriginX = -hitbox.getTravellingDirectionX() * newMapAreaRadius - (hitbox.getTravellingDirectionX() * NEW_MAP_AREA_ENTER_TRAVEL_TIME * hitbox.getTravellingVelocityX() * Options.GLOBAL_MOVEMENT_SPEED_MULTIPLIER);
                    } else {
                        newOriginX = -hitbox.getTravellingDirectionX() * newMapAreaRadius - (-hitbox.getTravellingDirectionX() * NEW_MAP_AREA_ENTER_TRAVEL_TIME * hitbox.getTravellingVelocityX() * Options.GLOBAL_MOVEMENT_SPEED_MULTIPLIER);
                    }
                    if(hitbox.getTravellingDirectionY() > 0) {
                        newOriginY = -hitbox.getTravellingDirectionY() * newMapAreaRadius - (hitbox.getTravellingDirectionY() * NEW_MAP_AREA_ENTER_TRAVEL_TIME * hitbox.getTravellingVelocityY() * Options.GLOBAL_MOVEMENT_SPEED_MULTIPLIER);
                    } else {
                        newOriginY = -hitbox.getTravellingDirectionY() * newMapAreaRadius - (-hitbox.getTravellingDirectionY() * NEW_MAP_AREA_ENTER_TRAVEL_TIME * hitbox.getTravellingVelocityY() * Options.GLOBAL_MOVEMENT_SPEED_MULTIPLIER);
                    }
                    hitbox.setOrigin(newOriginX, newOriginY);

                    // Instantly teleport camera to the same distance behind player from before to have illusion of smooth travel
                    camera.position.x = origin.x - cameraDistanceXFromPlayer;
                    camera.position.y = origin.y - cameraDistanceYFromPlayer;

                    hitbox.setVelocity(hitbox.getTravellingVelocityX(), hitbox.getTravellingVelocityY());
                    hitbox.setTravellingDestination(new Point(-hitbox.getTravellingDirectionX() * newMapAreaRadius + (hitbox.getTravellingDirectionX() * hitbox.getGravitationalRadius() * 2.5f),
                            -hitbox.getTravellingDirectionY() * newMapAreaRadius + (hitbox.getTravellingDirectionY() * hitbox.getGravitationalRadius() * 2.5f)));

                    hitbox.setTravellingFlag(true);
                    hitbox.setTravellingTime(0);
                } else if(hitbox.isTravellingFlag() && Utils.getDistance(origin, 0, 0) <= mapArea.getRadius() - hitbox.getGravitationalRadius() - 25f) {
                    hitbox.setVelocity(0, 0);

                    // Save game
                    if(!hitbox.isTravellingFromSameMapArea()) {
                        main.save();
                        // Manually clear new map area entityCreationData to avoid saving an empty one to the current map area
                        map.getCurrentArea().entityCreationDataArrayList.clear();
                    }

                    // Make player no longer invincible
                    hitbox.setIntangible(false);

                    hitbox.setTravelling(false);
                    hitbox.setIgnoreSpeedLimit(false);

                    hitbox.setTravellingFlag(false);
                    hitbox.setTravellingTime(0);
                    hitbox.setTravellingFromSameMapArea(false);
                } else {
                    hitbox.setTravellingTime(hitbox.getTravellingTime() + deltaTime);
                }
            } else if(hitbox.isDisabledMovement()) {
                isValidMovement = false;
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

                ArrayList<Entity> subEntities = hitbox.removeCircle(engine, e, c, (map.getCurrentArea().isBossArea() && Mappers.boss.has(e)));
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
                    float bonusPp = map.getCurrentArea().getOriginalEnemyCount()/((map.getMinEnemiesPerMapArea() + map.getMaxEnemiesPerMapArea())/2f) * map.getMaxPixelPoints() * Options.BONUS_PP_MULTIPLIER * Options.PP_GAIN_MULTIPLIER;
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
