package systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.miv.Mappers;

import components.HitboxComponent;
import map.Map;
import utils.CircleHitbox;
import utils.Point;

/**
 * Everything is rendered with ShapeRenderer for now (no images).
 * Created by Miv on 5/25/2017.
 */
public class RenderSystem extends EntitySystem {
    public static Color STAIRS_COLOR = Color.GOLD;
    public static Color PLAYER_COLOR = Color.BLUE;
    public static Color ENEMY_COLOR = Color.ORANGE;
    public static Color ENEMY_BULLET_COLOR = Color.RED;
    public static Color PLAYER_BULLET_COLOR = Color.GREEN;

    private Map map;

    private ImmutableArray<Entity> entities;
    private ShapeRenderer shapeRenderer;

    public RenderSystem(Map map) {
        this.map = map;

        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);
    }

    @Override
    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(HitboxComponent.class).get());
    }

    @Override
    public void removedFromEngine(Engine engine) {

    }

    @Override
    public void update(float deltaTime) {
        shapeRenderer.begin();

        // Draw map area boundaries
        if(map != null) {
            //TODO: change this color
            shapeRenderer.setColor(Color.BLACK);
            shapeRenderer.circle(0, 0, map.areas.get(map.getFocus()).getRadius());
        }

        for(Entity e : entities) {
            HitboxComponent hitbox = Mappers.hitbox.get(e);
            Point origin = hitbox.getOrigin();

            /**
            // Change color depending on if entity is a bullet, player, or enemy
            Color color = Color.BLACK;
            if(Mappers.player.has(e)) {
                color = PLAYER_COLOR;
            } else if(Mappers.enemy.has(e)) {
                color = ENEMY_COLOR;
            } else if(Mappers.playerBullet.has(e)) {
                color = PLAYER_BULLET_COLOR;
            } else if(Mappers.enemyBullet.has(e)) {
                color = ENEMY_BULLET_COLOR;
            }
            shapeRenderer.setColor(color);
            */

            // Draw hitboxes
            for(CircleHitbox c : hitbox.circles) {
                shapeRenderer.setColor(c.getColor());
                shapeRenderer.circle(c.x + origin.x, c.y + origin.y, c.radius);
            }
        }

        shapeRenderer.end();
    }

    public ShapeRenderer getShapeRenderer() {
        return shapeRenderer;
    }
}
