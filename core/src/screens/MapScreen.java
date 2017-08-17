package screens;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.miv.EntityActions;
import com.miv.GestureListener;
import com.miv.Main;
import com.miv.Mappers;

import java.util.ArrayList;
import java.util.HashMap;

import map.Map;
import systems.RenderSystem;
import utils.Point;
import utils.Utils;

/**
 * Created by Miv on 8/5/2017.
 */
public class MapScreen implements Screen {
    public static class MapScreenArea {
        public boolean areaCleared;
        public int colorIndex;
        public Color borderColor;

        // Position on map
        public float x;
        public float y;

        // Drawn position on screen
        private float screenX;
        private float screenY;

        // Enemy indicators
        public ArrayList<Point> enemies;

        // If true, draw grid lines
        private boolean hasAreaRight;
        private boolean hasAreaUp;
    }

    public static final float MAP_AREA_BUTTON_RADIUS = 75f;
    private static final float MAP_AREA_BUTTON_PADDING = 100f;

    public static final Color NORMAL_MAP_AREA_COLOR = new Color(128/255f, 1f, 1f, 0.3f);
    public static final Color NORMAL_MAP_AREA_BORDER_COLOR = new Color(NORMAL_MAP_AREA_COLOR).lerp(0, 0, 0, 1f, 0.5f);
    public static final Color STAIRS_MAP_AREA_COLOR = new Color(1f, 227/255f, 71/255f, 0.3f);
    public static final Color STAIRS_MAP_AREA_BORDER_COLOR = new Color(STAIRS_MAP_AREA_COLOR).lerp(0, 0, 0, 1f, 0.5f);
    private static final Color GRID_LINE_COLOR = new Color(172/255f, 239/255f, 239/255f, 1f);

    private ImageButton.ImageButtonStyle mapAreaButtonStyle;
    private ImageButton.ImageButtonStyle teleportButtonStyle;
    private ImageButton.ImageButtonStyle backButtonStyle;

    private Drawable[] bubbleDrawables;
    private TextureRegion movementArrowTail;
    private TextureRegion movementArrowBody;
    private TextureRegion movementArrowHead;
    private float movementArrowAngle;
    private float movementArrowLength;

    private Main main;
    private AssetManager assetManager;
    // Stage with buttons to travel to new areas
    private Stage stage;
    private OrthographicCamera stageCamera;
    // Stage with constant hud
    private Stage noCameraStage;
    private PooledEngine engine;
    private Entity player;
    private Map map;
    private InputMultiplexer inputMultiplexer;
    private ImageButton teleportButton;

    private HashMap<Point, MapScreenArea> mapAreas;
    private ShapeRenderer shapeRenderer;

    private Point lastKnownTouchDraggedPoint;

    private MapScreenArea selectedMapArea;
    private float currentMapAreaScreenX;
    private float currentMapAreaScreenY;

    private boolean disableMapAreaButtons;

    public MapScreen(Main main, AssetManager assetManager, PooledEngine engine, Entity player, Map map, InputMultiplexer inputMultiplexer) {
        this.main = main;
        this.assetManager = assetManager;
        this.engine = engine;
        this.player = player;
        this.map = map;
        this.inputMultiplexer = inputMultiplexer;

        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);

        Texture temp = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.MOVEMENT_ARROW_TAIL_PATH).path());
        movementArrowTail = new TextureRegion(temp);
        temp = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.MOVEMENT_ARROW_BODY_PATH).path());
        movementArrowBody = new TextureRegion(temp);
        temp = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.MOVEMENT_ARROW_HEAD_PATH).path());
        movementArrowHead = new TextureRegion(temp);

        Texture mapAreaButtonDown = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.MAP_AREA_TRAVEL_BUTTON_DOWN_PATH).path());
        mapAreaButtonStyle = new ImageButton.ImageButtonStyle(null, new TextureRegionDrawable(new TextureRegion(mapAreaButtonDown)), null, null, null, null);

        Texture teleportButtonUp = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.CHECKMARK_BUTTON_UP_PATH).path());
        Texture teleportButtonDown = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.CHECKMARK_BUTTON_DOWN_PATH).path());
        Texture teleportButtonDisabled = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.CHECKMARK_BUTTON_DISABLED_PATH).path());
        teleportButtonStyle = new ImageButton.ImageButtonStyle(new TextureRegionDrawable(new TextureRegion(teleportButtonUp)), new TextureRegionDrawable(new TextureRegion(teleportButtonDown)), null, null, null, null);
        teleportButtonStyle.disabled = new TextureRegionDrawable(new TextureRegion(teleportButtonDisabled));

        Texture backButtonUp = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.BACK_BUTTON_UP_PATH).path());
        Texture backButtonDown = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.BACK_BUTTON_DOWN_PATH).path());
        backButtonStyle = new ImageButton.ImageButtonStyle(new TextureRegionDrawable(new TextureRegion(backButtonUp)), new TextureRegionDrawable(new TextureRegion(backButtonDown)), null, null, null, null);

        stage = new Stage();
        stageCamera = (OrthographicCamera)stage.getViewport().getCamera();
        noCameraStage = new Stage();

        lastKnownTouchDraggedPoint = new Point(-1, -1);

        loadBubbleTextures();
    }

    public void loadBubbleTextures() {
        // Load bubble textures
        Texture bubbleTexture = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.BUBBLE_DEFAULT_PATH).path());
        TextureRegionDrawable bubbleTextureDrawable = new TextureRegionDrawable(new TextureRegion(bubbleTexture));
        bubbleDrawables = new SpriteDrawable[2];
        // Index 0 = normal map area
        bubbleDrawables[0] = bubbleTextureDrawable.tint(NORMAL_MAP_AREA_COLOR);
        // Index 1 = boss map area
        bubbleDrawables[1] = bubbleTextureDrawable.tint(STAIRS_MAP_AREA_COLOR);
    }

    public void createMap() {

        // Set camera focus
        stageCamera.position.set(map.getFocus().x * (MAP_AREA_BUTTON_RADIUS*2f + MAP_AREA_BUTTON_PADDING), map.getFocus().y * (MAP_AREA_BUTTON_RADIUS*2f + MAP_AREA_BUTTON_PADDING), 0);

        // Create back button
        final ImageButton backButton = new ImageButton(backButtonStyle);
        backButton.setSize(70f, 70f);
        backButton.setPosition(25f, 25f);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                main.loadHUD();
                event.stop();
            }
        });
        noCameraStage.addActor(backButton);

        // Create teleport button
        teleportButton = new ImageButton(teleportButtonStyle);
        teleportButton.setSize(70f, 70f);
        teleportButton.setPosition(Gdx.graphics.getWidth() - teleportButton.getWidth() - 25f, 25f);
        teleportButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (selectedMapArea != null && !(map.getFocus().x == selectedMapArea.x && map.getFocus().y == selectedMapArea.y)) {
                    main.loadHUD();

                    float angle = MathUtils.atan2(selectedMapArea.y - map.getFocus().y, selectedMapArea.x - map.getFocus().x);
                    EntityActions.playerEnterNewMapArea(player, MathUtils.cos(angle), MathUtils.sin(angle), new Point(selectedMapArea.x, selectedMapArea.y));
                }
                event.stop();
            }
        });
        noCameraStage.addActor(teleportButton);

        // Create map area buttons
        mapAreas = map.getDiscoveredAreaPositions();
        currentMapAreaScreenX = map.getFocus().x * (MAP_AREA_BUTTON_RADIUS*2 + MAP_AREA_BUTTON_PADDING);
        currentMapAreaScreenY = map.getFocus().y * (MAP_AREA_BUTTON_RADIUS*2 + MAP_AREA_BUTTON_PADDING);
        for(final MapScreenArea area : mapAreas.values()) {
            final ImageButton button = new ImageButton(mapAreaButtonStyle);
            area.screenX = area.x * (MAP_AREA_BUTTON_RADIUS*2 + MAP_AREA_BUTTON_PADDING);
            area.screenY = area.y * (MAP_AREA_BUTTON_RADIUS*2 + MAP_AREA_BUTTON_PADDING);
            button.setPosition(area.screenX - MAP_AREA_BUTTON_RADIUS, area.screenY - MAP_AREA_BUTTON_RADIUS);
            button.setSize(MAP_AREA_BUTTON_RADIUS * 2, MAP_AREA_BUTTON_RADIUS * 2);
            button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    // Player cannot be dragging when clicking the button
                    if (!disableMapAreaButtons) {
                        if (selectedMapArea != null && selectedMapArea.equals(area)) {
                            selectedMapArea = null;
                        } else {
                            selectedMapArea = area;
                        }
                        updateActors();

                        event.stop();
                    }
                }
            });
            stage.addActor(button);

            if(mapAreas.containsKey(new Point(area.x + 1, area.y))) {
                area.hasAreaRight = true;
            }
            if(mapAreas.containsKey(new Point(area.x, area.y + 1))) {
                area.hasAreaUp = true;
            }
        }

        updateActors();
    }

    public void updateActors() {
        // Teleport button disabled until valid map area selected
        if(selectedMapArea == null || (selectedMapArea.x == map.getFocus().x && selectedMapArea.y == map.getFocus().y)) {
            teleportButton.setDisabled(true);
        } else {
            teleportButton.setDisabled(false);
        }

        if(selectedMapArea != null) {
            movementArrowAngle = MathUtils.atan2(map.getFocus().y - selectedMapArea.y, selectedMapArea.x - map.getFocus().x);
            movementArrowLength = Utils.getDistance(map.getFocus(), selectedMapArea.x, selectedMapArea.y) * (MAP_AREA_BUTTON_RADIUS*2f + MAP_AREA_BUTTON_PADDING) - movementArrowHead.getRegionWidth()/2f;
        }
    }

    // Called from GestureListener
    public void touchUp(float x, float y) {
        // Set delay so that the button click is registered first, if any
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                disableMapAreaButtons = false;
            }
        }, 0.1f);
    }

    public void pan(float x, float y, float deltaX, float deltaY) {
        stageCamera.position.add(-deltaX * stageCamera.zoom, deltaY * stageCamera.zoom, 0);
        disableMapAreaButtons = true;
    }

    public void zoom(float deltaDistance) {
        stageCamera.zoom -= deltaDistance * 0.01f;
        if(stageCamera.zoom > 3) {
            stageCamera.zoom = 3;
        } else if(stageCamera.zoom < 0.2) {
            stageCamera.zoom = 0.2f;
        }
    }

    @Override
    public void show() {
        createMap();
        inputMultiplexer.addProcessor(noCameraStage);
        inputMultiplexer.addProcessor(stage);
        selectedMapArea = null;
        stageCamera.zoom = 1;
    }

    @Override
    public void render(float delta) {
        // Background color
        Gdx.gl.glClearColor(240 / 255f, 1, 1, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        noCameraStage.act();

        stage.act();
        stage.draw();

        shapeRenderer.setProjectionMatrix(stage.getViewport().getCamera().combined);
        shapeRenderer.begin();
        Gdx.gl.glLineWidth(5f);
        for(MapScreenArea area : mapAreas.values()) {
            // Draw grid lines
            shapeRenderer.setColor(GRID_LINE_COLOR);
            shapeRenderer.set(ShapeRenderer.ShapeType.Line);
            if(area.hasAreaRight) {
                shapeRenderer.line(area.screenX + MAP_AREA_BUTTON_RADIUS, area.screenY, area.screenX + (MAP_AREA_BUTTON_RADIUS + MAP_AREA_BUTTON_PADDING), area.screenY);
            }
            if(area.hasAreaUp) {
                shapeRenderer.line(area.screenX, area.screenY + MAP_AREA_BUTTON_RADIUS, area.screenX, area.screenY + (MAP_AREA_BUTTON_RADIUS + MAP_AREA_BUTTON_PADDING));
            }
        }
        shapeRenderer.end();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        stage.getBatch().begin();
        for(MapScreenArea area : mapAreas.values()) {
            bubbleDrawables[area.colorIndex].draw(stage.getBatch(), area.screenX - MAP_AREA_BUTTON_RADIUS, area.screenY - MAP_AREA_BUTTON_RADIUS, MAP_AREA_BUTTON_RADIUS * 2f, MAP_AREA_BUTTON_RADIUS * 2f);
        }
        stage.getBatch().end();

        shapeRenderer.begin();
        for(MapScreenArea area : mapAreas.values()) {
            shapeRenderer.setColor(area.borderColor);
            shapeRenderer.set(ShapeRenderer.ShapeType.Line);
            shapeRenderer.circle(area.screenX, area.screenY, MAP_AREA_BUTTON_RADIUS);

            // Draw enemies
            if(area.enemies != null) {
                shapeRenderer.setColor(Color.RED);
                for(Point p : area.enemies) {
                    shapeRenderer.circle(area.screenX + p.x, area.screenY + p.y, 5f);
                }
            }
        }
        // Highlight selected map area with red ring
        if(selectedMapArea != null) {
            shapeRenderer.setColor(Color.RED);
            shapeRenderer.set(ShapeRenderer.ShapeType.Line);
            shapeRenderer.circle(selectedMapArea.screenX, selectedMapArea.screenY, MAP_AREA_BUTTON_RADIUS);
        }
        // Highlight current map area with green ring
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.set(ShapeRenderer.ShapeType.Line);
        shapeRenderer.circle(currentMapAreaScreenX, currentMapAreaScreenY, MAP_AREA_BUTTON_RADIUS);
        shapeRenderer.end();

        if(selectedMapArea != null && !(map.getFocus().x == selectedMapArea.x && map.getFocus().y == selectedMapArea.y)) {
            stage.getBatch().begin();

            // Draw arrow from current area to selected area
            // Draw arrow tail
            stage.getBatch().draw(movementArrowTail, currentMapAreaScreenX - movementArrowTail.getRegionWidth() / 2, currentMapAreaScreenY - movementArrowTail.getRegionHeight() / 2);

            // Draw stretched arrow body
            stage.getBatch().draw(movementArrowBody, currentMapAreaScreenX,
                    currentMapAreaScreenY - 5 + MathUtils.sin(movementArrowAngle),
                    0, 5, movementArrowLength - movementArrowHead.getRegionHeight()/2f, 10, 1, 1, -MathUtils.radiansToDegrees * movementArrowAngle);

            // Draw arrow head
            stage.getBatch().draw(movementArrowHead, currentMapAreaScreenX + MathUtils.cos(movementArrowAngle) * (movementArrowLength - movementArrowHead.getRegionHeight()/2f),
                    currentMapAreaScreenY - (movementArrowHead.getRegionHeight()/2f + MathUtils.sin(movementArrowAngle) * (movementArrowLength - movementArrowHead.getRegionHeight()/2f)),
                    0, movementArrowHead.getRegionHeight()/2f, movementArrowHead.getRegionWidth(), movementArrowHead.getRegionHeight(), 1, 1, -MathUtils.radiansToDegrees * movementArrowAngle);

            stage.getBatch().end();
        }

        Gdx.gl.glDisable(GL20.GL_BLEND);

        noCameraStage.draw();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        inputMultiplexer.removeProcessor(stage);
        inputMultiplexer.removeProcessor(noCameraStage);
        stage.clear();
        noCameraStage.clear();
    }

    @Override
    public void dispose() {

    }

    public void setMap(Map map) {
        this.map = map;
    }

    public void setPlayer(Entity player) {
        this.player = player;
    }
}
