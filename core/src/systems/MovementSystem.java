package systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.utils.Array;
import com.miv.EntityActions;
import com.badlogic.gdx.math.Vector2;
import com.miv.Mappers;

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

/**
 * Created by Miv on 5/25/2017.
 */
public class MovementSystem extends EntitySystem {
    private ImmutableArray<Entity> entities;
    private ArrayList<Entity> entityRemovalQueue;
    private ArrayList<Entity> collisionEntitiesToHandle;
    private ArrayList<CircleHitbox> collisionCirclesToHandle;
    private ImmutableArray<Entity> players;
    private ImmutableArray<Entity> playerBullets;
    private ImmutableArray<Entity> enemies;
    private ImmutableArray<Entity> enemyBullets;
    private PooledEngine engine;
    private Map map;

    public MovementSystem(PooledEngine engine, Map map) {
        this.engine = engine;
        this.map = map;
        collisionCirclesToHandle = new ArrayList<CircleHitbox>();
        collisionEntitiesToHandle = new ArrayList<Entity>();
        entityRemovalQueue = new ArrayList<Entity>();
    }

    @Override
    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(HitboxComponent.class).get());
        players = engine.getEntitiesFor(Family.all(PlayerComponent.class).get());
        playerBullets = engine.getEntitiesFor(Family.all(PlayerBulletComponent.class).get());
        enemies = engine.getEntitiesFor(Family.all(EnemyComponent.class).get());
        enemyBullets = engine.getEntitiesFor(Family.all(EnemyBulletComponent.class).get());
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

    private void handleBulletCollision(Entity victim, CircleHitbox victimHitboxHit, Entity bullet) {
        float damage = 0;
        if(Mappers.enemyBullet.has(bullet)) {
            damage = Mappers.enemyBullet.get(bullet).getDamage();
        } else if(Mappers.playerBullet.has(bullet)) {
            damage = Mappers.playerBullet.get(bullet).getDamage();
        }

        // Victim takes damage
        HitboxComponent victimHitbox = Mappers.hitbox.get(victim);
        victimHitboxHit.setHealth(victimHitboxHit.getHealth() - damage);

        if(victimHitboxHit.getHealth() <= 0) {
            //TODO: circle death animation
            victimHitbox.queueCircleRemoval(victimHitboxHit);
            // All hit circles considered dead when number of circles is 1 because size() is not updated until
            // the circle removal queue is fired.
            if(victimHitbox.getCircles().size() == 1) {
                //TODO: kill entity
                // Queue entity removal from engine
            }
        }

        // Queue bullet entity removal
        entityRemovalQueue.add(bullet);
    }

    /**
     * For collisions that do not involve bullets
     * @param e1 - an entity in a collision; should *not* be a projectile
     * @param e2 - an entity hitbox in a collision
     */
    private void handleNonBulletCollision(Entity e1, Entity e2) {
        if (Mappers.customOnCollision.has(e1)) {
            OnCollisionEvent customOnCollisionEvent = Mappers.customOnCollision.get(e1).getOnCollisionEvent();
            customOnCollisionEvent.onCollision(e1, e2);
        }
        if (Mappers.customOnCollision.has(e2)) {
            OnCollisionEvent customOnCollisionEvent = Mappers.customOnCollision.get(e2).getOnCollisionEvent();
            customOnCollisionEvent.onCollision(e2, e1);
        }
    }

    private boolean checkIfOutsideCurrentMapArea(Entity e, Point origin, Vector2 velocity, CircleHitbox c, MapArea mapArea, float boundary) {
        if (c.x + origin.x + velocity.x >= boundary) {
            if (Mappers.playerBullet.has(e) || Mappers.enemyBullet.has(e)) {
                engine.removeEntity(e);
            } else if (velocity.x < 0) {
                EntityActions.playerEnterNewMapArea(engine, e, map, EntityActions.Direction.LEFT);
            } else if (velocity.x > 0) {
                EntityActions.playerEnterNewMapArea(engine, e, map, EntityActions.Direction.RIGHT);
            }
            return true;
        } else if (c.y + origin.y + velocity.y >= boundary) {
            if (Mappers.playerBullet.has(e) || Mappers.enemyBullet.has(e)) {
                engine.removeEntity(e);
            } else if (velocity.y < 0) {
                EntityActions.playerEnterNewMapArea(engine, e, map, EntityActions.Direction.DOWN);
            } else if (velocity.y > 0) {
                EntityActions.playerEnterNewMapArea(engine, e, map, EntityActions.Direction.UP);
            }
            return true;
        }
        return false;
    }

    @Override
    public void update(float deltaTime) {
        MapArea mapArea = map.areas.get(map.getFocus());

        if(mapArea != null) {
            float mapAreaRadiusSquared = mapArea.getRadius() * mapArea.getRadius();
            for (Entity e : entities) {
                HitboxComponent hitbox = Mappers.hitbox.get(e);
                Point hitboxOrigin = hitbox.getOrigin();
                Point origin = hitbox.getOrigin();
                Vector2 velocity = hitbox.getVelocity();

                boolean isValidMovement = true;

                if (!hitbox.isIntangible()) {
                    // If entity is a player, check for collisions against the edges of the MapArea, enemies, enemy bullets
                    if (Mappers.player.has(e)) {
                        for (CircleHitbox c : hitbox.getCircles()) {
                            // Check if circle is outside map area radius
                            checkIfOutsideCurrentMapArea(e, origin, velocity, c, mapArea, mapAreaRadiusSquared);

                            // Check against enemies
                            checkForCollision(hitboxOrigin, c, enemies);
                            for(int i = 0; i < collisionEntitiesToHandle.size(); i++) {
                                isValidMovement = false;
                                handleNonBulletCollision(e, collisionEntitiesToHandle.get(i));
                            }

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
                            // Check if circle is outside map area radius
                            if (checkIfOutsideCurrentMapArea(e, origin, velocity, c, mapArea, mapAreaRadiusSquared)) {
                                isValidMovement = false;
                            }

                            // Against players
                            checkForCollision(hitboxOrigin, c, players);
                            for(int i = 0; i < collisionEntitiesToHandle.size(); i++) {
                                isValidMovement = false;
                                handleNonBulletCollision(e, collisionEntitiesToHandle.get(i));
                            }

                            // Against player bullets
                            checkForCollision(hitboxOrigin, c, playerBullets);
                            for(int i = 0; i < collisionEntitiesToHandle.size(); i++) {
                                isValidMovement = false;
                                handleBulletCollision(e, c, collisionEntitiesToHandle.get(i));
                            }
                        }
                    }
                    // If entity is a player bullet, check for collisions against the square boundaries of the MapArea, enemies
                    else if (Mappers.playerBullet.has(e)) {
                        // Square boundaries have side length of 4x the radius
                        for (CircleHitbox c : hitbox.getCircles()) {
                            // Check if circle is outside map area radius
                            checkIfOutsideCurrentMapArea(e, origin, velocity, c, mapArea, mapArea.getRadius() * 2f);

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
                            checkIfOutsideCurrentMapArea(e, origin, velocity, c, mapArea, mapArea.getRadius() * 2f);

                            // Check for collision against the players
                            checkForCollision(hitboxOrigin, c, players);
                            for(int i = 0; i < collisionEntitiesToHandle.size(); i++) {
                                isValidMovement = false;
                                handleBulletCollision(collisionEntitiesToHandle.get(i), collisionCirclesToHandle.get(i), e);
                            }
                        }
                    }
                }

                if (isValidMovement) {
                    hitbox.setOrigin(origin.x + velocity.x, origin.y + velocity.y);
                    hitbox.setVelocity(velocity.x + hitbox.getAcceleration().x, velocity.y + hitbox.getAcceleration().y);
                }

                // Remove circles in hitbox circle removal queue from array list of circles in the hitbox component
                for(CircleHitbox c : hitbox.getCircleRemovalQueue()) {
                    hitbox.getCircles().remove(c);
                }
                hitbox.clearCircleRemovalQueue();
            }
        }
    }
}
