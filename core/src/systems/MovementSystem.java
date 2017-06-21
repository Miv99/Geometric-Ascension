package systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import com.miv.Mappers;

import components.HitboxComponent;
import map.Map;
import map.MapArea;
import utils.Point;

/**
 * Created by Miv on 5/25/2017.
 */
public class MovementSystem extends EntitySystem {
    private ImmutableArray<Entity> entities;
    private Map map;

    public MovementSystem(Map map) {
        this.map = map;
    }

    @Override
    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(HitboxComponent.class).get());
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
                            if ((c.x + origin.x + velocity.x) * (c.x + origin.x + velocity.x) + (c.y + origin.y + velocity.y) * (c.y + origin.y + velocity.y) >= mapAreaRadiusSquared) {
                                // Enter new MapArea
                                //TODO
                            }

                            // Check against enemies

                            // Check against enemy bullets
                        }
                    }
                    // If entity is an enemy, check for collisions against the edges of the MapArea, players, player bullets
                    else if (Mappers.enemy.has(e)) {
                        // Against edges of MapArea
                        // Block movement
                        isValidMovement = false;

                        // Against players

                        // Against player bullets
                    }
                    // If entity is a player bullet, check for collisions against the square boundaries of the MapArea, enemies
                    else if (Mappers.playerBullet.has(e)) {
                        // Square boundaries have side length of 4x the radius
                        for (Circle c : hitbox.circles) {
                            // Check if circle is outside map area radius
                            if (c.x + origin.x + velocity.x >= mapArea.getRadius() * 2f || c.y + origin.y + velocity.y >= mapArea.getRadius() * 2f) {
                                // Delete entity

                            }
                        }

                        // Against enemies
                    }
                    // If entity is an enemy bullet, check for collisions against the square boundaries of the MapArea, players
                    else if (Mappers.enemyBullet.has(e)) {
                        // Against MapArea
                        // Delete entity

                        // Check for collision against the player
                    }
                }

                if (isValidMovement) {
                    hitbox.setOrigin(origin.x + velocity.x, origin.y + velocity.y);
                }
            }
        }
    }

    private void moveEntity(Entity e) {

    }
}
