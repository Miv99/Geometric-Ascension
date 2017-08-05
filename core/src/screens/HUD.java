package screens;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Timer;
import com.miv.AudioPlayer;
import com.miv.GestureListener;
import com.miv.Main;
import com.miv.Mappers;

import components.HitboxComponent;
import map.Map;
import utils.Point;

/**
 * Created by Miv on 6/8/2017.
 */
public class HUD implements Screen {
    public static float SMALL_BUTTON_PADDING = 25f;
    public static float SMALL_BUTTON_SIZE = 70f;

    private Stage stage;
    private InputMultiplexer inputMultiplexer;

    private GestureListener gestureListener;
    private Point movementDragTouchDownPoint;
    private Point shootingDragTouchDownPoint;

    private Entity player;

    private Main main;

    private float screenWidth;
    private float screenHeight;

    // Textures
    private TextureRegion movementArrowTail;
    private TextureRegion movementArrowBody;
    private TextureRegion movementArrowHead;

    private ShapeRenderer shapeRenderer;
    private Color screenOverlayColor;
    private float screenOverlayDeltaAlpha;
    private Timer.Task taskToBeRunAfterScreenFade;

    private AudioPlayer audioPlayer;

    private Label pp;
    private float ppY;

    public HUD(final Main main, AssetManager assetManager, InputMultiplexer inputMultiplexer, GestureListener gestureListener, final Entity player, Map map) {
        this.main = main;
        this.inputMultiplexer = inputMultiplexer;
        this.gestureListener = gestureListener;
        this.player = player;

        Skin skin = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.SKIN_PATH).path());

        movementDragTouchDownPoint = gestureListener.getMovementDragTouchDownPoint();
        shootingDragTouchDownPoint = gestureListener.getShootingDragTouchDownPoint();
        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();

        stage = new Stage();
        stage.addListener(new ClickListener() {});

        audioPlayer = new AudioPlayer(assetManager, Main.WORLD_MUSIC_PATHS);

        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);

        // Load textures
        Texture temp = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.MOVEMENT_ARROW_TAIL_PATH).path());
        movementArrowTail = new TextureRegion(temp);
        temp = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.MOVEMENT_ARROW_BODY_PATH).path());
        movementArrowBody = new TextureRegion(temp);
        temp = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.MOVEMENT_ARROW_HEAD_PATH).path());
        movementArrowHead = new TextureRegion(temp);

        // Map button top left
        Texture mapButtonUp = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.MAP_BUTTON_UP_PATH).path());
        Texture mapButtonDown = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.MAP_BUTTON_DOWN_PATH).path());
        ImageButton.ImageButtonStyle mapButtonStyle = new ImageButton.ImageButtonStyle(new TextureRegionDrawable(new TextureRegion(mapButtonUp)), new TextureRegionDrawable(new TextureRegion(mapButtonDown)), null, null, null, null);
        ImageButton mapButton = new ImageButton(mapButtonStyle);
        mapButton.setSize(SMALL_BUTTON_SIZE, SMALL_BUTTON_SIZE);
        mapButton.setPosition(SMALL_BUTTON_PADDING, Gdx.graphics.getHeight() - mapButton.getHeight() - SMALL_BUTTON_PADDING);
        mapButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                main.loadMapScreen();
            }
        });
        stage.addActor(mapButton);

        // Player customization button
        Texture customizeButtonUp = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.CUSTOMIZE_BUTTON_UP_PATH).path());
        Texture customizeButtonDown = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.CUSTOMIZE_BUTTON_DOWN_PATH).path());
        ImageButton.ImageButtonStyle customizeButtonStyle = new ImageButton.ImageButtonStyle(new TextureRegionDrawable(new TextureRegion(customizeButtonUp)), new TextureRegionDrawable(new TextureRegion(customizeButtonDown)), null, null, null, null);
        ImageButton customizeButton = new ImageButton(customizeButtonStyle);
        customizeButton.setSize(SMALL_BUTTON_SIZE, SMALL_BUTTON_SIZE);
        customizeButton.setPosition(Gdx.graphics.getWidth() - customizeButton.getWidth() - SMALL_BUTTON_PADDING, Gdx.graphics.getHeight() - customizeButton.getHeight() - SMALL_BUTTON_PADDING);
        customizeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                main.loadCustomizeScreen();
            }
        });
        stage.addActor(customizeButton);

        ppY = screenHeight - 80f;

        pp = new Label(Math.round(Mappers.player.get(player).getPixelPoints()) + "pp", skin);
        pp.setFontScale(2.7f);
        pp.setAlignment(Align.center);
        pp.setColor(Color.BLACK);
        pp.pack();
        pp.setPosition(screenWidth/2f - pp.getWidth()/2f, ppY);
        stage.addActor(pp);

        updateText();
    }

    public void updateText() {
        // Update pp label
        float pixelPoints = Mappers.player.get(player).getPixelPoints();
        if(pixelPoints < 10) {
            pp.setText(String.format("%.2f", pixelPoints) + "pp");
        } else if(pixelPoints < 100) {
            pp.setText(String.format("%.1f", pixelPoints) + "pp");
        } else {
            pp.setText(Math.round(pixelPoints) + "pp");
        }
        pp.setPosition((screenWidth - pp.getWidth())/2f, ppY);
    }

    @Override
    public void show() {
        inputMultiplexer.addProcessor(stage);
        audioPlayer.playRandomMusic();
        updateText();
    }

    @Override
    public void render(float delta) {
        stage.getBatch().begin();

        if(movementDragTouchDownPoint.x != -1) {
            // Draw arrow tail
            stage.getBatch().draw(movementArrowTail, movementDragTouchDownPoint.x - movementArrowTail.getRegionWidth() / 2, screenHeight - (movementDragTouchDownPoint.y + movementArrowTail.getRegionHeight() / 2));

            // Draw stretched arrow body
            float bodyThickness = 10f;
            stage.getBatch().draw(movementArrowBody, movementDragTouchDownPoint.x,
                    screenHeight - (movementDragTouchDownPoint.y + (bodyThickness / 2) + MathUtils.sin(gestureListener.getMovementArrowAngle())),
                    0, bodyThickness/2f, gestureListener.getMovementArrowLength(), bodyThickness, 1, 1, -MathUtils.radiansToDegrees * gestureListener.getMovementArrowAngle());

            // Draw arrow head
            stage.getBatch().draw(movementArrowHead, movementDragTouchDownPoint.x + MathUtils.cos(gestureListener.getMovementArrowAngle()) * gestureListener.getMovementArrowLength(),
                    screenHeight - (movementDragTouchDownPoint.y + (movementArrowHead.getRegionHeight()/2) + MathUtils.sin(gestureListener.getMovementArrowAngle()) * gestureListener.getMovementArrowLength()),
                    0, movementArrowHead.getRegionHeight()/2f, movementArrowHead.getRegionWidth(), movementArrowHead.getRegionHeight(), 1, 1, -MathUtils.radiansToDegrees * gestureListener.getMovementArrowAngle());
        }
        if(shootingDragTouchDownPoint.x != -1) {
            // Draw arrow tail
            stage.getBatch().draw(movementArrowTail, shootingDragTouchDownPoint.x - movementArrowTail.getRegionWidth() / 2, screenHeight - (shootingDragTouchDownPoint.y + movementArrowTail.getRegionHeight() / 2));

            // Draw stretched arrow body
            float bodyThickness = 10f;
            stage.getBatch().draw(movementArrowBody, shootingDragTouchDownPoint.x,
                    screenHeight - (shootingDragTouchDownPoint.y + (bodyThickness / 2) + MathUtils.sin(gestureListener.getShootingArrowAngle())),
                    0, bodyThickness/2f, gestureListener.getShootingArrowLength(), bodyThickness, 1, 1, -MathUtils.radiansToDegrees * gestureListener.getShootingArrowAngle());

            // Draw arrow head
            stage.getBatch().draw(movementArrowHead, shootingDragTouchDownPoint.x + MathUtils.cos(gestureListener.getShootingArrowAngle()) * gestureListener.getShootingArrowLength(),
                    screenHeight - (shootingDragTouchDownPoint.y + (movementArrowHead.getRegionHeight()/2) + MathUtils.sin(gestureListener.getShootingArrowAngle()) * gestureListener.getShootingArrowLength()),
                    0, movementArrowHead.getRegionHeight()/2f, movementArrowHead.getRegionWidth(), movementArrowHead.getRegionHeight(), 1, 1, -MathUtils.radiansToDegrees * gestureListener.getShootingArrowAngle());
        }
        stage.getBatch().end();

        if(screenOverlayDeltaAlpha != 0) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(screenOverlayColor);

            screenOverlayColor.a += screenOverlayDeltaAlpha * delta;
            if(screenOverlayColor.a > 1) {
                screenOverlayColor.a = 1;
                screenOverlayDeltaAlpha = 0;
                taskToBeRunAfterScreenFade.run();
            }

            shapeRenderer.rect(0, 0, screenWidth, screenHeight);

            shapeRenderer.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {
        audioPlayer.pause();
    }

    @Override
    public void resume() {
        audioPlayer.resume();
    }

    @Override
    public void hide() {
        inputMultiplexer.removeProcessor(stage);
        audioPlayer.stop();
        audioPlayer.reset();
    }

    @Override
    public void dispose() {

    }

    /**
     * Fades screen to a color and then runs the task
     * @param time - time it takes to completely fade
     * @param task - task to be run after screen is done fading to white
     */
    public void fadeToColor(Color color, float time, Timer.Task task) {
        fadeToColor(color, time, task, 0);
    }

    /**
     * Fades screen to a color and then runs the task
     * @param time - time it takes to completely fade
     * @param task - task to be run after screen is done fading to white
     */
    public void fadeToColor(Color color, float time, Timer.Task task, float startingAlpha) {
        taskToBeRunAfterScreenFade = task;
        screenOverlayColor = new Color(color);
        screenOverlayColor.a = startingAlpha;
        screenOverlayDeltaAlpha = (1f - startingAlpha)/time;
    }
}
