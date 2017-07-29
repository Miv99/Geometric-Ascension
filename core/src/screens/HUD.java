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
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Timer;
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
    private Stage stage;
    private InputMultiplexer inputMultiplexer;

    private GestureListener gestureListener;
    private Point movementDragTouchDownPoint;
    private Point movementDragCurrentPoint;

    private Entity player;

    private float screenWidth;
    private float screenHeight;

    // Textures
    private TextureRegion movementArrowTail;
    private TextureRegion movementArrowBody;
    private TextureRegion movementArrowHead;

    /**
     * Lower x bound and upper y bound of rectangle in which {@link com.miv.GestureListener} will not work for movement
     */
    private float disableGesturesLowerXBound;
    private float disableGesturesLowerYBound;

    private ShapeRenderer shapeRenderer;
    private Color screenOverlayColor;
    private float screenOverlayDeltaAlpha;
    private Timer.Task taskToBeRunAfterScreenFade;

    public HUD(AssetManager assetManager, InputMultiplexer inputMultiplexer, GestureListener gestureListener, final Entity player, Map map) {
        this.inputMultiplexer = inputMultiplexer;
        this.gestureListener = gestureListener;
        this.player = player;

        stage = new Stage();
        stage.addListener(new ClickListener() {});

        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);

        // Load textures
        Texture temp = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.MOVEMENT_ARROW_TAIL_PATH).path());
        movementArrowTail = new TextureRegion(temp);
        temp = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.MOVEMENT_ARROW_BODY_PATH).path());
        movementArrowBody = new TextureRegion(temp);
        temp = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.MOVEMENT_ARROW_HEAD_PATH).path());
        movementArrowHead = new TextureRegion(temp);

        // Attack buttons
        Texture attackButtonUp = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.ATTACK_BUTTON_UP_PATH).path());
        Texture attackButtonDown = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.ATTACK_BUTTON_DOWN_PATH).path());
        ImageButton.ImageButtonStyle imageButtonStyle = new ImageButton.ImageButtonStyle(new TextureRegionDrawable(new TextureRegion(attackButtonUp)), new TextureRegionDrawable(new TextureRegion(attackButtonDown)), null, null, null, null);
        // Primary fire button
        float padding = 20f;
        ImageButton primaryFireButton = new ImageButton(imageButtonStyle);
        primaryFireButton.setPosition(Gdx.graphics.getWidth() - primaryFireButton.getWidth() - padding, padding);
        primaryFireButton.addListener(new InputListener() {
            HitboxComponent playerHitbox = Mappers.hitbox.get(player);

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                playerHitbox.setIsShooting(true);
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                playerHitbox.setIsShooting(false);
            }
        });
        stage.addActor(primaryFireButton);

        //TODO: change calculation for this as more attack buttons are added
        disableGesturesLowerXBound = Gdx.graphics.getWidth() - primaryFireButton.getWidth() - padding;
        disableGesturesLowerYBound = Gdx.graphics.getHeight() - primaryFireButton.getHeight() - padding;

        movementDragTouchDownPoint = gestureListener.getMovementDragTouchDownPoint();
        movementDragCurrentPoint = gestureListener.getMovementDragCurrentPoint();
        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();

    }

    @Override
    public void show() {
        inputMultiplexer.addProcessor(stage);
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
        stage.getBatch().end();

        if(screenOverlayDeltaAlpha != 0) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            System.out.println(screenOverlayColor.r + ", " + screenOverlayColor.g + ", " + screenOverlayColor.b + ", " + screenOverlayColor.a);
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

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        inputMultiplexer.removeProcessor(stage);
    }

    @Override
    public void dispose() {

    }

    /**
     * Fades screen to white and then runs the task
     * @param time - time it takes to completely fade
     * @param task - task to be run after screen is done fading to white
     */
    public void fadeToColor(Color color, float time, Timer.Task task) {
        taskToBeRunAfterScreenFade = task;
        color.a = 0;
        screenOverlayColor = color;
        screenOverlayDeltaAlpha = 1f/time;
    }

    public float getDisableGesturesLowerXBound() {
        return disableGesturesLowerXBound;
    }

    public float getDisableGesturesLowerYBound() {
        return disableGesturesLowerYBound;
    }
}
