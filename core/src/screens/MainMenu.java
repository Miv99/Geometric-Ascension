package screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.miv.Main;

/**
 * Created by Miv on 5/21/2017.
 */
public class MainMenu implements Screen {
	private Skin skin;
    private Texture background;
    private Music music;
	private InputMultiplexer inputMultiplexer;

    private Stage stage;

    public MainMenu(final Main main, final AssetManager assetManager, final InputMultiplexer inputMultiplexer) {
		this.inputMultiplexer = inputMultiplexer;
		stage = new Stage();

		// Load assets
		skin = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.SKIN_PATH).path());
        music = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.MAIN_MENU_MUSIC_1_PATH).path());
        background = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.MAIN_MENU_BACKGROUND_PATH).path());

		Button.ButtonStyle invisibleButtonStyle = new Button.ButtonStyle();

		// Create buttons
		Button newGameButton = new Button(invisibleButtonStyle);
		newGameButton.setSize(Main.SCREEN_WIDTH, Main.SCREEN_HEIGHT);
		newGameButton.setPosition(0, 0);
		newGameButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				main.startGame();
				music.stop();
			}
		});
		stage.addActor(newGameButton);

		// Options button
		Texture optionsButtonUp = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.OPTIONS_BUTTON_UP_PATH).path());
		Texture optionsButtonDown = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.OPTIONS_BUTTON_DOWN_PATH).path());
		ImageButton.ImageButtonStyle optionsButtonStyle = new ImageButton.ImageButtonStyle(new TextureRegionDrawable(new TextureRegion(optionsButtonUp)), new TextureRegionDrawable(new TextureRegion(optionsButtonDown)), null, null, null, null);
		ImageButton optionsButton = new ImageButton(optionsButtonStyle);
		optionsButton.setSize(70, 70);
		optionsButton.setPosition(25, Gdx.graphics.getHeight() - 25 - optionsButton.getHeight());
		optionsButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				main.setScreen(new Options(main, assetManager, inputMultiplexer, music));
			}
		});
		stage.addActor(optionsButton);
    }

	@Override
	public void show() {
		inputMultiplexer.addProcessor(stage);

		music.play();
		music.setLooping(true);

		for(Actor a : stage.getActors()) {
			a.setVisible(true);
		}
	}

	@Override
	public void render(float delta) {
		// Background color
		Gdx.gl.glClearColor(240/255f, 1, 1, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		stage.getBatch().begin();
		stage.getBatch().draw(background, 0, 0, Main.SCREEN_WIDTH, Main.SCREEN_HEIGHT);
		stage.getBatch().end();

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
	}

	@Override
	public void hide() {
		System.out.println("MAIN MENU HIDDEN");
		inputMultiplexer.removeProcessor(stage);
		music.pause();
		for(Actor a : stage.getActors()) {
			a.setVisible(false);
		}
	}

	@Override
	public void dispose() {
		music.dispose();
		skin.dispose();
		stage.dispose();
	}
}
