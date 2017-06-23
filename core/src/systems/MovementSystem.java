package systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.math.Circle;
import com.miv.EntityActions;
import com.badlogic.gdx.math.Vector2;
import com.miv.Mappers;

import components.CustomOnCollisionComponent;
import components.EnemyBulletComponent;
import components.EnemyComponent;
import components.HealthComponent;
import components.HitboxComponent;
import components.PlayerBulletComponent;
import components.PlayerComponent;
import map.Map;
import map.MapArea;
import utils.OnCollisionEvent;
import utils.Point;

/**
 * Created by Miv on 5/25/2017.
 */
public class MovementSystem extends EntitySystem {
    private ImmutableArray<Entity> entities;
    private Array<Entity> entitiesToHandle;
    private ImmutableArray<Entity> players;
    private ImmutableArray<Entity> playerBullets;
    private ImmutableArray<Entity> enemies;
    private ImmutableArray<Entity> enemyBullets;
    private PooledEngine engine;
    private Map map;

    public MovementSystem(PooledEngine engine, Map map) {
        this.engine = engine;
        this.map = map;
        entitiesToHandle = new Array<Entity>();
    }

    @Override
    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(HitboxComponent.class).get());
        players = engine.getEntitiesFor(Family.all(PlayerComponent.class).get());
        playerBullets = engine.getEntitiesFor(Family.all(PlayerBulletComponent.class).get());
        enemies = engine.getEntitiesFor(Family.all(EnemyComponent.class).get());
        enemyBullets = engine.getEntitiesFor(Family.all(EnemyBulletComponent.class).get());
    }

    private Array<Entity> checkForCollision(Circle c, ImmutableArray<Entity> arrayOfEntities) {
        entitiesToHandle.clear();
        for (Entity entity : arrayOfEntities) {
            HitboxComponent entityHitbox = Mappers.hitbox.get(entity);
            for (Circle entityHitboxCircle : entityHitbox.circles) {
                if (Math.sqrt(Math.pow((c.x - entityHitboxCircle.x), 2) + Math.pow((c.y - entityHitboxCircle.y), 2)) >= c.radius + entityHitboxCircle.radius) {
                    entitiesToHandle.add(entity);
                }
            }
        }
        return entitiesToHandle;
    }

    /**
     * @param e1 - an entity in a collision; should *not* be a projectile
     * @param e2 - an entity in a collision
     */
    private void handleCollision(Entity e1, Entity e2) {
        if (Mappers.customOnCollision.has(e1)) {
            OnCollisionEvent customOnCollisionEvent = Mappers.customOnCollision.get(e1).getOnCollisionEvent();
            customOnCollisionEvent.onCollision(e1, e2);
        }
        if (Mappers.customOnCollision.has(e2)) {
            OnCollisionEvent customOnCollisionEvent = Mappers.customOnCollision.get(e2).getOnCollisionEvent();
            customOnCollisionEvent.onCollision(e2, e1);
        }
        if (Mappers.player.has(e1)) {
            if (Mappers.enemyBullet.has(e2)) {
                int currentHealth = Mappers.health.get(e1).getHealth();
                Mappers.health.get(e1).setHealth(currentHealth - (int)Mappers.enemyBullet.get(e2).getDamage());
            }
        } else if (Mappers.enemy.has(e1)) {
            if (Mappers.playerBullet.has(e2)) {
                int currentHealth = Mappers.health.get(e1).getHealth();
                Mappers.health.get(e1).setHealth(currentHealth - (int)Mappers.playerBullet.get(e2).getDamage());
            }
        }
    }

    private boolean checkIfOutsideCurrentMapArea(Entity e, Point origin, Vector2 velocity, Circle c, MapArea mapArea, float boundary) {
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
                Point origin = hitbox.getOrigin();
                Vector2 velocity = hitbox.getVelocity();

                boolean isValidMovement = true;

                if (!hitbox.isIntangible()) {
                    // If entity is a player, check for collisions against the edges of the MapArea, enemies, enemy bullets
                    if (Mappers.player.has(e)) {
                        for (Circle c : hitbox.circles) {
                            // Check if circle is outside map area radius
                            checkIfOutsideCurrentMapArea(e, origin, velocity, c, mapArea, mapAreaRadiusSquared);

                            // Check against enemies
                            Array<Entity> enemiesToHandle = checkForCollision(c, enemies);
                            if (enemiesToHandle != null) {
                                for (Entity enemyToHandle : enemiesToHandle) {
                                    isValidMovement = false;
                                    handleCollision(e, enemyToHandle);
                                }
                            }

                            // Check against enemy bullets
                            Array<Entity> enemyBulletsToHandle = checkForCollision(c, enemyBullets);
                            if (enemyBulletsToHandle != null) {
                                for (Entity enemyBulletToHandle : enemyBulletsToHandle) {
                                    isValidMovement = false;
                                    handleCollision(e, enemyBulletToHandle);
                                }
                            }
                        }
                    }
                    // If entity is an enemy, check for collisions against the edges of the MapArea, players, player bullets
                    else if (Mappers.enemy.has(e)) {
                        for (Circle c : hitbox.circles) {
                            // Check if circle is outside map area radius
                            if (checkIfOutsideCurrentMapArea(e, origin, velocity, c, mapArea, mapAreaRadiusSquared)) {
                                isValidMovement = false;
                            }

                            // Against players
                            Array<Entity> playersToHandle = checkForCollision(c, players);
                            if (playersToHandle != null) {
                                for (Entity playerToHandle : playersToHandle) {
                                    isValidMovement = false;
                                    handleCollision(e, playerToHandle);
                                }
                            }

                            // Against player bullets
                            Array<Entity> playerBulletsToHandle = checkForCollision(c, playerBullets);
                            if (playerBulletsToHandle != null) {
                                for (Entity playerBulletToHandle : playerBulletsToHandle) {
                                    isValidMovement = false;
                                    handleCollision(e, playerBulletToHandle);
                                }
                            }
                        }
                    }
                    // If entity is a player bullet, check for collisions against the square boundaries of the MapArea, enemies
                    else if (Mappers.playerBullet.has(e)) {
                        // Square boundaries have side length of 4x the radius
                        for (Circle c : hitbox.circles) {
                            // Check if circle is outside map area radius
                            checkIfOutsideCurrentMapArea(e, origin, velocity, c, mapArea, mapArea.getRadius() * 2f);

                            // Against enemies
                            Array<Entity> enemiesToHandle = checkForCollision(c, enemies);
                            if (enemiesToHandle != null) {
                                for (Entity enemyToHandle : enemiesToHandle) {
                                    isValidMovement = false;
                                    handleCollision(enemyToHandle, e);
                                }
                            }
                        }
                    }
                    // If entity is an enemy bullet, check for collisions against the square boundaries of the MapArea, players
                    else if (Mappers.enemyBullet.has(e)) {
                        for (Circle c : hitbox.circles) {
                            // Against MapArea
                            checkIfOutsideCurrentMapArea(e, origin, velocity, c, mapArea, mapArea.getRadius() * 2f);

                            // Check for collision against the players
                            Array<Entity> playersToHandle = checkForCollision(c, players);
                            if (playersToHandle != null) {
                                for (Entity playerToHandle : playersToHandle) {
                                    isValidMovement = false;
                                    handleCollision(playerToHandle, e);
                                }
                            }
                        }
                    }
                }

                if (isValidMovement) {
                    hitbox.setOrigin(origin.x + velocity.x, origin.y + velocity.y);
                    hitbox.setVelocity(velocity.x + hitbox.getAcceleration().x, velocity.y + hitbox.getAcceleration().y);
                }
            }
        }
    }
}
