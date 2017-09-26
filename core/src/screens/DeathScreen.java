package screens;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.EventAction;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.miv.*;
import com.miv.Options;

import map.Map;

/**
 * Created by Miv on 9/24/2017.
 */
public class DeathScreen implements Screen {
    private static final float TEXT_FADE_IN_TIME = 5f;
    private static final float HIGH_SCORE_POPUP_DELAY = TEXT_FADE_IN_TIME + 3f;
    private static final float TOUCH_TO_START_POPUP_DELAY = HIGH_SCORE_POPUP_DELAY + 3f;

    private Stage stage;
    private InputMultiplexer inputMultiplexer;
    private Skin skin;
    private float screenWidth;
    private float screenHeight;

    /**
     * Application screen should be set to this screen immediately after constructor is called.
     * A new instance of this screen should be used each time it needs to be shown since it
     * probably will not show up often in gameplay, so there's no point in wasting resources on
     * keeping an instance of it.
     */
    public DeathScreen(final Main main, InputMultiplexer inputMultiplexer, final AssetManager assetManager, float score, Map map, boolean showNewHighScore) {
        stage = new Stage();
        this.inputMultiplexer = inputMultiplexer;
        skin = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.SKIN_PATH).path());

        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();

        Label text = new Label("You made it to\nfloor " + (map.getFloor() + 1) + "\nand earned a " +
                "total of\n" + PlayerBuilder.formatNumber(score) + "pp", skin, "big");
        text.setColor(0, 0, 0, 0);
        text.setSize(screenWidth, screenHeight);
        text.setAlignment(Align.center);
        stage.addActor(text);

        Label newHighScore = null;
        if(showNewHighScore) {
            newHighScore = new Label("\n\n\n\nNew high score!", skin, "big");
            newHighScore.setColor(Color.BLACK);
            newHighScore.setSize(screenWidth, screenHeight);
            newHighScore.setAlignment(Align.center);
            newHighScore.setVisible(false);
            stage.addActor(newHighScore);
        }

        Button.ButtonStyle invisibleButtonStyle = new Button.ButtonStyle();
        Button newGameButton = new Button(invisibleButtonStyle);
        newGameButton.setSize(screenWidth, screenHeight);
        newGameButton.setPosition(0, 0);
        newGameButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                main.loadMainMenuMapPreview();
                main.loadMainMenu();
            }
        });
        newGameButton.setVisible(false);
        stage.addActor(newGameButton);

        Label touchToStart = new Label("Touch to continue", skin);
        touchToStart.setAlignment(Align.center);
        touchToStart.setWidth(screenWidth);
        touchToStart.setFontScale(2.5f);
        touchToStart.setPosition(0, 60f);
        touchToStart.setColor(Color.BLACK);
        touchToStart.setVisible(false);
        stage.addActor(touchToStart);

        text.addAction(Actions.fadeIn(TEXT_FADE_IN_TIME));
        if(newHighScore != null) {
            touchToStart.addAction(Actions.delay(TOUCH_TO_START_POPUP_DELAY, Actions.visible(true)));
            newGameButton.addAction(Actions.delay(TOUCH_TO_START_POPUP_DELAY, Actions.visible(true)));

            newHighScore.addAction(Actions.delay(HIGH_SCORE_POPUP_DELAY, Actions.visible(true)));
            newHighScore.addAction(Actions.delay(HIGH_SCORE_POPUP_DELAY, new Action() {
                @Override
                public boolean act(float delta) {
                    Sound sound = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.UPGRADE_1_SOUND_PATH).path());
                    sound.play(com.miv.Options.MASTER_VOLUME * Options.SOUND_VOLUME);
                    return false;
                }
            }));
        } else {
            touchToStart.addAction(Actions.delay(HIGH_SCORE_POPUP_DELAY, Actions.visible(true)));
            newGameButton.addAction(Actions.delay(HIGH_SCORE_POPUP_DELAY, Actions.visible(true)));
        }

        Sound gameOverSound = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.GAME_OVER_SOUND_PATH).path());
        // Game over played at music volume
        gameOverSound.play(Options.MASTER_VOLUME * Options.MUSIC_VOLUME);
    }

    @Override
    public void render(float delta) {
        // Background color
        Gdx.gl.glClearColor(1f, 1, 1, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void show() {
        inputMultiplexer.addProcessor(stage);
    }

    @Override
    public void hide() {
        inputMultiplexer.removeProcessor(stage);
    }

    @Override
    public void dispose() {

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
}
