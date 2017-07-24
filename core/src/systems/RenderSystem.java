package systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.miv.Main;
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
    public static enum HitboxTextureType {
        // ID must be in ascending order starting from 0
        STAIRS(0, new Color(Color.YELLOW.r, Color.YELLOW.g, Color.YELLOW.b, 0.3f)),
        PLAYER(1, new Color(Color.CYAN.r, Color.CYAN.g, Color.CYAN.b, 0.3f)),
        ENEMY(2, new Color(Color.ORANGE.r, Color.ORANGE.g, Color.ORANGE.b, 0.3f)),
        ENEMY_BULLET(3, new Color(Color.RED.r, Color.RED.g, Color.RED.b, 0.3f)),
        PLAYER_BULLET(4, new Color(Color.GREEN.r, Color.GREEN.g, Color.GREEN.b, 0.3f));

        private int id;
        private Color color;
        private Color outlineColor;

        HitboxTextureType(int id, Color color) {
            this.id = id;
            this.color = color;
            outlineColor = new Color(color).set(color.r, color.g, color.b, 1f);
        }
    }

    private Map map;

    private ImmutableArray<Entity> entities;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;

    // Array of bubble textures for each color in HitboxTextureType
    private Drawable[] bubbleDrawables;
    private Drawable bubbleShieldDrawable;

    public RenderSystem(Map map) {
        this.map = map;

        // Set shape renderer thickness
        Gdx.gl.glLineWidth(4f);

        batch = new SpriteBatch();

        // Initiate shape renderer
        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);
    }

    public void loadTextures(AssetManager assetManager) {
        Texture bubbleTexture = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.BUBBLE_DEFAULT_PATH).path());
        TextureRegionDrawable bubbleTextureDrawable = new TextureRegionDrawable(new TextureRegion(bubbleTexture));

        // Load bubble textures
        bubbleDrawables = new SpriteDrawable[HitboxTextureType.values().length];
        for(HitboxTextureType h : HitboxTextureType.values()) {
            bubbleDrawables[h.id] = bubbleTextureDrawable.tint(h.color);
        }

        Texture bubbleShieldTexture = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.BUBBLE_SHIELD_PATH).path());
        bubbleShieldDrawable = new TextureRegionDrawable(new TextureRegion(bubbleShieldTexture));
    }

    @Override
    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(HitboxComponent.class).get());
    }

    @Override
    public void removedFromEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(HitboxComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        // Background color
        Gdx.gl.glClearColor(214 / 255f, 238 / 255f, 1, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        for(Entity e : entities) {
            HitboxComponent hitbox = Mappers.hitbox.get(e);
            Point origin = hitbox.getOrigin();

            // Draw hitboxes
            for(CircleHitbox c : hitbox.getCircles()) {
                bubbleDrawables[c.getHitboxTextureType().id].draw(batch, c.x + origin.x - c.radius, c.y + origin.y - c.radius, c.radius * 2, c.radius * 2);
            }

            // Draw shield around entity if travelling
            if(hitbox.isTravelling()) {
                bubbleShieldDrawable.draw(batch, origin.x - (hitbox.getGravitationalRadius() + 15f), origin.y - (hitbox.getGravitationalRadius() + 15f), (hitbox.getGravitationalRadius() + 15f)*2, (hitbox.getGravitationalRadius() + 15f)*2);
            }
        }
        batch.end();

        shapeRenderer.begin();
        for(Entity e : entities) {
            HitboxComponent hitbox = Mappers.hitbox.get(e);
            Point origin = hitbox.getOrigin();

            // Draw hitbox outlines
            for(CircleHitbox c : hitbox.getCircles()) {
                shapeRenderer.setColor(c.getHitboxTextureType().outlineColor);
                shapeRenderer.circle(c.x + origin.x, c.y + origin.y, c.radius);
            }

            //TODO: remove this
            //shapeRenderer.setColor(Color.BLACK);
            //shapeRenderer.circle(origin.x, origin.y, 3f);
            //shapeRenderer.circle(origin.x, origin.y, hitbox.getGravitationalRadius());
        }
        // Draw map area boundaries
        if(map != null) {
            //TODO: change this color
            shapeRenderer.setColor(Color.BLACK);
            shapeRenderer.circle(0, 0, map.getCurrentArea().getRadius());
        }
        shapeRenderer.end();
    }

    public ShapeRenderer getShapeRenderer() {
        return shapeRenderer;
    }

    public SpriteBatch getBatch() {
        return batch;
    }
}
