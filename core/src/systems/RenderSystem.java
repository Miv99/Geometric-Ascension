package systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.miv.Main;
import com.miv.Mappers;
import com.miv.Options;

import java.util.ArrayList;

import components.HitboxComponent;
import map.Map;
import utils.CircleHitbox;
import utils.Point;

/**
 * Everything is rendered with ShapeRenderer for now (no images).
 * Created by Miv on 5/25/2017.
 */
public class RenderSystem extends EntitySystem {
    private static class FloatingText {
        private String text;
        private Color textColor;
        private float x;
        private float y;

        private float timeLeft;
        private float deltaAlpha;

        private float width;
        private float height;

        private FloatingText(float x, float y, String text, Color textColor, float time) {
            this.x = x;
            this.y = y;
            this.text = text;
            this.textColor = new Color(textColor);
            timeLeft = time;
            deltaAlpha = textColor.a/time;
        }
    }

    public static enum HitboxTextureType {
        // ID must be in ascending order starting from 0
        PLAYER(0, new Color(Color.CYAN.r, Color.CYAN.g, Color.CYAN.b, 0.3f), new Color(0f, 163/255f, 33/255f, 1f)),
        ENEMY(1, new Color(Color.ORANGE.r, Color.ORANGE.g, Color.ORANGE.b, 0.3f), new Color(196/255f, 0f, 0f, 1f)),
        ENEMY_BULLET(2, new Color(Color.RED.r, Color.RED.g, Color.RED.b, 0.3f), new Color(Color.RED)),
        PLAYER_BULLET(3, new Color(Color.GREEN.r, Color.GREEN.g, Color.GREEN.b, 0.3f), new Color(Color.RED));

        private int id;
        private Color color;
        private Color outlineColor;
        private Color healthBarColor;

        HitboxTextureType(int id, Color color, Color healthBarColor) {
            this.id = id;
            this.color = color;
            outlineColor = new Color(color).set(color.r, color.g, color.b, 1f);

            this.healthBarColor = healthBarColor;
        }
    }

    private static final float FLOATING_TEXT_BOUNDARY_PADDING = 20f;

    // Health bar y-axis offset from center of circle
    private static final float HEALTH_BAR_Y = -20f;
    public static final Color NORMAL_MAP_AREA_BACKGROUND_COLOR = new Color(224/255f, 1f, 1f, 1f);
    public static final Color STAIRS_MAP_AREA_BACKGROUND_COLOR = new Color(1f, 237/255f, 147/255f, 1f);
    public static final Color NORMAL_MAP_AREA_BORDER_COLOR = new Color(NORMAL_MAP_AREA_BACKGROUND_COLOR).lerp(0f, 0f, 0f, 1f, 0.5f);
    public static final Color STAIRS_MAP_AREA_BORDER_COLOR = new Color(STAIRS_MAP_AREA_BACKGROUND_COLOR).lerp(0f, 0f, 0f, 1f, 0.5f);
    private static final Color NORMAL_MAP_AREA_GRID_LINE_COLOR = new Color(NORMAL_MAP_AREA_BACKGROUND_COLOR).lerp(0f, 0f, 0f, 1f, 0.2f);
    private static final Color STAIRS_MAP_AREA_GRID_LINE_COLOR = new Color(STAIRS_MAP_AREA_BACKGROUND_COLOR).lerp(0f, 0f, 0f, 1f, 0.2f);

    private Main main;
    private Map map;

    private ImmutableArray<Entity> entities;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;

    // Array of bubble textures for each color in HitboxTextureType
    private Drawable[] bubbleDrawables;
    private Drawable bubbleShieldDrawable;

    private GlyphLayout layout;
    private BitmapFont floatingTextFont;
    private ArrayList<FloatingText> floatingTexts;

    public RenderSystem(Main main) {
        this.main = main;
        layout = new GlyphLayout();
        floatingTexts = new ArrayList<FloatingText>();

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

        // Load font
        Skin skin = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.SKIN_PATH).path());
        floatingTextFont = skin.getFont("font-big");
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
        Gdx.gl.glClearColor(240/255f, 1, 1, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shapeRenderer.begin();
        // Draw map area boundaries
        if(map != null) {
            // Color inside of map area
            Color gridLineColor;
            if(map.getCurrentArea().getStairsDestination() == -1) {
                shapeRenderer.setColor(NORMAL_MAP_AREA_BACKGROUND_COLOR);
                gridLineColor = NORMAL_MAP_AREA_GRID_LINE_COLOR;
            } else {
                shapeRenderer.setColor(STAIRS_MAP_AREA_BACKGROUND_COLOR);
                gridLineColor = STAIRS_MAP_AREA_GRID_LINE_COLOR;
            }
            shapeRenderer.set(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.circle(0, 0, map.getCurrentArea().getRadius());

            // Draw grid lines
            if(main.getState() != Main.GameState.MAIN_MENU) {
                shapeRenderer.setColor(gridLineColor);
                for (Map.GridLine gl : map.getGridLines()) {
                    shapeRenderer.rectLine(gl.getStartX(), gl.getStartY(), gl.getEndX(), gl.getEndY(), 2.5f);
                }
            }

            // Draw border
            Gdx.gl.glLineWidth(20);
            shapeRenderer.set(ShapeRenderer.ShapeType.Line);
            if(map.getCurrentArea().getStairsDestination() == -1) {
                shapeRenderer.setColor(NORMAL_MAP_AREA_BORDER_COLOR);
            } else {
                shapeRenderer.setColor(STAIRS_MAP_AREA_BORDER_COLOR);
            }
            shapeRenderer.circle(0, 0, map.getCurrentArea().getRadius());
        }
        shapeRenderer.end();

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
        Gdx.gl.glLineWidth(4f);
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

        // Draw health bars
        shapeRenderer.set(ShapeRenderer.ShapeType.Filled);
        for(Entity e : entities) {
            if((Options.SHOW_ENEMY_HEALTH_BARS && Mappers.enemy.has(e)) || (Options.SHOW_PLAYER_HEALTH_BARS && Mappers.player.has(e))) {
                HitboxComponent hitbox = Mappers.hitbox.get(e);
                Point origin = hitbox.getOrigin();

                for (CircleHitbox c : hitbox.getCircles()) {
                    float healthBarWidth = c.getHealth() / c.getMaxHealth() * c.radius * 2.5f;
                    float healthBarRadius = Math.min(18f, c.radius / 12f);
                    float x = -healthBarWidth / 2f;

                    // Color in health bar
                    shapeRenderer.setColor(c.getHitboxTextureType().healthBarColor);
                    // Draw start arc
                    shapeRenderer.arc(origin.x + c.x + x, origin.y + c.y + HEALTH_BAR_Y + healthBarRadius, healthBarRadius, 90f, 180f);
                    // Draw rectangle
                    shapeRenderer.rect(origin.x + c.x + x, origin.y + c.y + HEALTH_BAR_Y, healthBarWidth, healthBarRadius * 2f);
                    // Draw end arc
                    shapeRenderer.arc(origin.x + c.x + x + healthBarWidth, origin.y + c.y + HEALTH_BAR_Y + healthBarRadius, healthBarRadius, 270f, 450f);
                }
            }
        }
        shapeRenderer.end();

        // Draw floating text
        batch.begin();
        for(int i = 0; i < floatingTexts.size(); i++) {
            FloatingText f = floatingTexts.get(i);

            floatingTextFont.setColor(f.textColor);
            floatingTextFont.draw(batch, f.text, f.x, f.y);

            f.timeLeft -= deltaTime;
            f.textColor.a -= f.deltaAlpha * deltaTime;
            f.y += Options.GLOBAL_MOVEMENT_SPEED_MULTIPLIER * 3f * deltaTime;
            if(f.timeLeft <= 0 || f.textColor.a <= 0) {
                floatingTexts.remove(f);
                i--;
            }
        }
        batch.end();
    }

    /**
     * Adds text to originate from a point and fly up while losing alpha.
     * @param x x-coordinate of text relative to the map area
     * @param y y-coordinate of text relative to the map area
     * @param text Text to be shown
     * @param color Color of text
     */
    public void addFloatingText(float x, float y, String text, Color color) {
        FloatingText ft = new FloatingText(x, y, text, color, 1.5f);
        layout.setText(floatingTextFont, text);
        ft.width = layout.width;
        ft.height = layout.height;

        // Center text on position given
        ft.x -= ft.width/2f;

        floatingTexts.add(ft);

        checkOverlappingTexts(ft);
    }

    private void checkOverlappingTexts(FloatingText f1) {
        // Check if text will overlap with any other texts
        for(FloatingText f : floatingTexts) {
            if(!f1.equals(f) && overlaps(f1, f)) {
                // If it does, bump up the text that will expire first
                if(f.timeLeft < f1.timeLeft) {
                    f.y += f.height + FLOATING_TEXT_BOUNDARY_PADDING;

                    checkOverlappingTexts(f);
                } else {
                    f1.y += f1.height + FLOATING_TEXT_BOUNDARY_PADDING;

                    checkOverlappingTexts(f1);
                }
                break;
            }
        }
    }

    private boolean overlaps(FloatingText f1, FloatingText f2) {
        return f1.x < f2.x + f2.width && f1.x + f1.width > f2.x && f1.y < f2.y + f2.height && f1.y + f1.height > f2.y;
    }

    public void addFloatingText(Entity origin, String text, Color color) {
        Point p = Mappers.hitbox.get(origin).getOrigin();
        addFloatingText(p.x, p.y, text, color);
    }

    public void clearFloatingTexts() {
        floatingTexts.clear();
    }

    public ShapeRenderer getShapeRenderer() {
        return shapeRenderer;
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public void setMap(Map map) {
        this.map = map;
    }
}
