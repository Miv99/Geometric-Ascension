package screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Timer;
import com.miv.*;
import com.miv.Options;

/**
 * Created by Miv on 5/21/2017.
 */
public class MainMenu implements Screen {
	public static final Color COLOR_OVERLAY = new Color(1f, 1f, 1f, 0.7f);

	private Skin skin;
    private Music music;
	private InputMultiplexer inputMultiplexer;

    private Stage stage;

	private float screenWidth;
	private float screenHeight;

	private ShapeRenderer shapeRenderer;

    public MainMenu(final Main main, final AssetManager assetManager, final InputMultiplexer inputMultiplexer) {
		this.inputMultiplexer = inputMultiplexer;
		stage = new Stage();

		screenWidth = Gdx.graphics.getWidth();
		screenHeight = Gdx.graphics.getHeight();

		shapeRenderer = new ShapeRenderer();
		shapeRenderer.setAutoShapeType(true);

		// Load assets
		skin = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.SKIN_PATH).path());
        music = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.MAIN_MENU_MUSIC_1_PATH).path());

		Button.ButtonStyle invisibleButtonStyle = new Button.ButtonStyle();

		// Options button
		Texture optionsButtonUp = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.OPTIONS_BUTTON_UP_PATH).path());
		Texture optionsButtonDown = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.OPTIONS_BUTTON_DOWN_PATH).path());
		ImageButton.ImageButtonStyle optionsButtonStyle = new ImageButton.ImageButtonStyle(new TextureRegionDrawable(new TextureRegion(optionsButtonUp)), new TextureRegionDrawable(new TextureRegion(optionsButtonDown)), null, null, null, null);
		final ImageButton optionsButton = new ImageButton(optionsButtonStyle);
		optionsButton.setSize(70, 70);
		optionsButton.setPosition(25, 25);
		optionsButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				main.loadOptionsScreen(music);
				main.getOptions().setShowMainMenuOnBackButtonClick(true);
			}
		});
		// Disable input around options button in case of fat fingers
		final float optionsButtonDisabledInputPadding = 80f;

		// New game button that covers almost entire screen
		Button newGameButton = new Button(invisibleButtonStyle);
		newGameButton.setSize(Main.SCREEN_WIDTH, Main.SCREEN_HEIGHT);
		newGameButton.setPosition(0, 0);
		newGameButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if(x > optionsButton.getX() + optionsButton.getWidth() + optionsButtonDisabledInputPadding || y > optionsButton.getY() + optionsButton.getHeight() + optionsButtonDisabledInputPadding) {
					main.startGame();
					music.stop();
				}
			}
		});
		stage.addActor(newGameButton);
		stage.addActor(optionsButton);

		// Touch to start label
		Label touchToStart = new Label("Touch to start", skin);
		touchToStart.setAlignment(Align.center);
		touchToStart.setWidth(screenWidth);
		touchToStart.setFontScale(2.5f);
		touchToStart.setPosition(0, 200f);
		touchToStart.setColor(Color.GRAY);
		stage.addActor(touchToStart);
    }

    public void stopMusic() {
		music.stop();
	}

	@Override
	public void show() {
		inputMultiplexer.addProcessor(stage);

		music.play();
		music.setVolume(com.miv.Options.MASTER_VOLUME * Options.MUSIC_VOLUME);
		music.setLooping(true);
	}

	@Override
	public void render(float delta) {
		stage.getBatch().begin();
		stage.getBatch().end();

		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.setColor(COLOR_OVERLAY);
		shapeRenderer.rect(0, 0, screenWidth, screenHeight);
		shapeRenderer.end();
		Gdx.gl.glDisable(GL20.GL_BLEND);

		stage.act(delta);
		stage.draw();
	}

	@Override
	public void resize(int width, int height) {

	}

	@Override
	public void pause() {
		music.pause();
	}

	@Override
	public void resume() {
		music.play();
		music.setVolume(com.miv.Options.MASTER_VOLUME * Options.MUSIC_VOLUME);
	}

	@Override
	public void hide() {
		inputMultiplexer.removeProcessor(stage);
	}

	@Override
	public void dispose() {
		music.dispose();
		skin.dispose();
		stage.dispose();
	}
}
