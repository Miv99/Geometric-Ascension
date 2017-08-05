package com.miv;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import map.Map;
import screens.HUD;
import screens.MainMenu;
import screens.PlayerBuilder;
import systems.AISystem;
import systems.MovementSystem;
import systems.RenderSystem;
import systems.ShootingSystem;

public class Main extends Game {
	public static enum GameState {
		MAIN_MENU,
		MAIN_GAME,
		MAP,
		CUSTOMIZE
	}

	public static final int SCREEN_WIDTH = 1600;
	public static final int SCREEN_HEIGHT = 900;

	public static final String SKIN_PATH = "glassy\\skin\\glassy-ui.json";
	public static final String DEFAULT_FONT_PATH = "Roboto-Regular.ttf";
	public static final String[] WORLD_MUSIC_PATHS = new String[] {
			"music\\world1.mp3",
			"music\\world2.ogg",
			"music\\world3.wav",
			"music\\world4.ogg"
	};
	public static final String MAIN_MENU_MUSIC_1_PATH = "music\\main_menu1.ogg";
	public static final String MOVEMENT_ARROW_TAIL_PATH = "movement_arrow_tail.png";
	public static final String MOVEMENT_ARROW_BODY_PATH = "movement_arrow_body.png";
	public static final String MOVEMENT_ARROW_HEAD_PATH = "movement_arrow_head.png";
	public static final String ATTACK_BUTTON_UP_PATH = "attack_button_up.png";
	public static final String ATTACK_BUTTON_DOWN_PATH = "attack_button_down.png";
	public static final String BACK_BUTTON_UP_PATH = "back_button_up.png";
	public static final String BACK_BUTTON_DOWN_PATH = "back_button_down.png";
	public static final String CANCEL_BUTTON_UP_PATH = "cancel_button_up.png";
	public static final String CANCEL_BUTTON_DOWN_PATH = "cancel_button_down.png";
	public static final String CANCEL_BUTTON_DISABLED_PATH = "cancel_button_disabled.png";
	public static final String CHECKMARK_BUTTON_UP_PATH = "checkmark_button_up.png";
	public static final String CHECKMARK_BUTTON_DOWN_PATH = "checkmark_button_down.png";
	public static final String CHECKMARK_BUTTON_DISABLED_PATH = "checkmark_button_disabled.png";
	public static final String CUSTOMIZE_BUTTON_UP_PATH = "customize_button_up.png";
	public static final String CUSTOMIZE_BUTTON_DOWN_PATH = "customize_button_down.png";
	public static final String DELETE_BUTTON_UP_PATH = "delete_button_up.png";
	public static final String DELETE_BUTTON_DOWN_PATH = "delete_button_down.png";
	public static final String HOME_BUTTON_UP_PATH = "home_button_up.png";
	public static final String HOME_BUTTON_DOWN_PATH = "home_button_down.png";
	public static final String NEXT_BUTTON_UP_PATH = "next_button_up.png";
	public static final String NEXT_BUTTON_DOWN_PATH = "next_button_down.png";
	public static final String OPTIONS_BUTTON_UP_PATH = "options_button_up.png";
	public static final String OPTIONS_BUTTON_DOWN_PATH = "options_button_down.png";
	public static final String MAP_BUTTON_UP_PATH = "map_button_up.png";
	public static final String MAP_BUTTON_DOWN_PATH = "map_button_down.png";
	public static final String BUBBLE_DEFAULT_PATH = "bubble_default.png";
    public static final String BUBBLE_SHIELD_PATH = "bubble_shield.png";

	private PooledEngine engine;
	private ShootingSystem shootingSystem;
	private RenderSystem renderSystem;
	private Camera camera;
	private InputMultiplexer inputMultiplexer;
	private GestureListener gestureListener;
	private Map map;
    private Entity player;

	private boolean playerDead;

	private AssetManager assetManager;
	private Preferences preferences;
	private Preferences highScores;

	private GameState state;
	private HUD hud;

	// Screens
	private MainMenu mainMenu;
	private PlayerBuilder playerBuilder;

	@Override
	public void create() {
		engine = new PooledEngine(20, 100, 20, 100);

		inputMultiplexer = new InputMultiplexer();
		gestureListener = new GestureListener(this, inputMultiplexer);
		inputMultiplexer.addProcessor(new GestureDetector(gestureListener));
		Gdx.input.setInputProcessor(inputMultiplexer);

		loadPreferences();
		loadAssets();

		renderSystem = new RenderSystem(this);
		loadMainMenuMapPreview();
		renderSystem.setMap(map);

		// Add entity systems
		engine.addSystem(new AISystem());
		engine.addSystem(new MovementSystem(this, engine, map, player));
		shootingSystem = new ShootingSystem(engine);
		engine.addSystem(shootingSystem);
		engine.addSystem(renderSystem);

		camera = new Camera(renderSystem);
		camera.resetViewport();
		camera.position.set(0, 0, 0);
		camera.setFocus(player);

		assetManager.finishLoading();
		renderSystem.loadTextures(assetManager);
		loadMainMenu();
	}

	public void loadMainMenuMapPreview() {
		Save.load(this);
		engine.addEntity(player);
		map.enterNewArea(engine, player, (int) map.getFocus().x, (int) map.getFocus().y, true);
		if(camera != null) {
			camera.setFocus(player);
		}
	}

	public void loadMainMenu() {
		if(mainMenu == null) {
			mainMenu = new MainMenu(this, assetManager, inputMultiplexer);
		}
		setScreen(mainMenu);
		state = GameState.MAIN_MENU;
	}

	public void loadMapScreen() {
		//TODO
		state = GameState.MAP;

		//TODO: on exit, set state to main game
	}

	public void loadCustomizeScreen() {
		if(playerBuilder == null) {
			playerBuilder = new PlayerBuilder(this, inputMultiplexer, assetManager, player);
		}
		setScreen(playerBuilder);
		state = GameState.CUSTOMIZE;

		//TODO: on exit, set state to main game
	}

	public void loadHUD() {
		if(hud == null) {
			hud = new HUD(this, assetManager, inputMultiplexer, gestureListener, player, map);
		}
		setScreen(hud);
		state = GameState.MAIN_GAME;
	}

	public void loadAssets() {
		InternalFileHandleResolver fileHandler = new InternalFileHandleResolver();
		assetManager = new AssetManager(fileHandler);
		assetManager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(fileHandler));
		assetManager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(fileHandler));

		assetManager.load(SKIN_PATH, Skin.class);
		assetManager.load(DEFAULT_FONT_PATH, FreeTypeFontGenerator.class);
		for(String s : WORLD_MUSIC_PATHS) {
			assetManager.load(s, Music.class);
		}
		assetManager.load(MAIN_MENU_MUSIC_1_PATH, Music.class);
		assetManager.load(MOVEMENT_ARROW_TAIL_PATH, Texture.class);
		assetManager.load(MOVEMENT_ARROW_BODY_PATH, Texture.class);
		assetManager.load(MOVEMENT_ARROW_HEAD_PATH, Texture.class);

		assetManager.load(ATTACK_BUTTON_UP_PATH, Texture.class);
		assetManager.load(ATTACK_BUTTON_DOWN_PATH, Texture.class);
		assetManager.load(BACK_BUTTON_UP_PATH, Texture.class);
		assetManager.load(BACK_BUTTON_DOWN_PATH, Texture.class);
		assetManager.load(CANCEL_BUTTON_UP_PATH, Texture.class);
		assetManager.load(CANCEL_BUTTON_DOWN_PATH, Texture.class);
		assetManager.load(CANCEL_BUTTON_DISABLED_PATH, Texture.class);
		assetManager.load(CHECKMARK_BUTTON_UP_PATH, Texture.class);
		assetManager.load(CHECKMARK_BUTTON_DOWN_PATH, Texture.class);
		assetManager.load(CHECKMARK_BUTTON_DISABLED_PATH, Texture.class);
		assetManager.load(CUSTOMIZE_BUTTON_UP_PATH, Texture.class);
		assetManager.load(CUSTOMIZE_BUTTON_DOWN_PATH, Texture.class);
		assetManager.load(DELETE_BUTTON_UP_PATH, Texture.class);
		assetManager.load(DELETE_BUTTON_DOWN_PATH, Texture.class);
		assetManager.load(HOME_BUTTON_UP_PATH, Texture.class);
		assetManager.load(HOME_BUTTON_DOWN_PATH, Texture.class);
		assetManager.load(NEXT_BUTTON_UP_PATH, Texture.class);
		assetManager.load(NEXT_BUTTON_DOWN_PATH, Texture.class);
		assetManager.load(OPTIONS_BUTTON_UP_PATH, Texture.class);
		assetManager.load(OPTIONS_BUTTON_DOWN_PATH, Texture.class);
		assetManager.load(MAP_BUTTON_UP_PATH, Texture.class);
		assetManager.load(MAP_BUTTON_DOWN_PATH, Texture.class);

		assetManager.load(BUBBLE_SHIELD_PATH, Texture.class);
		assetManager.load(BUBBLE_DEFAULT_PATH, Texture.class);
	}

	public void loadPreferences() {
		preferences = Gdx.app.getPreferences("Geometric Ascension");
		preferences.putFloat(Options.MUSIC_VOLUME_STRING, preferences.getFloat(Options.MUSIC_VOLUME_STRING, Options.MUSIC_VOLUME));
		preferences.putFloat(Options.SOUND_VOLUME_STRING, preferences.getFloat(Options.SOUND_VOLUME_STRING, Options.SOUND_VOLUME));
		preferences.putFloat(Options.MASTER_VOLUME_STRING, preferences.getFloat(Options.MASTER_VOLUME_STRING, Options.MASTER_VOLUME));
		preferences.putFloat(Options.MOVEMENT_DRAG_ARROW_MAX_DISTANCE_STRING, preferences.getFloat(Options.MOVEMENT_DRAG_ARROW_MAX_DISTANCE_STRING, Options.MOVEMENT_DRAG_ARROW_MAX_DISTANCE));
		preferences.putBoolean(Options.SHOW_ENEMY_HEALTH_BARS_STRING, preferences.getBoolean(Options.SHOW_ENEMY_HEALTH_BARS_STRING, Options.SHOW_ENEMY_HEALTH_BARS));
		preferences.putBoolean(Options.SHOW_PLAYER_HEALTH_BARS_STRING, preferences.getBoolean(Options.SHOW_PLAYER_HEALTH_BARS_STRING, Options.SHOW_PLAYER_HEALTH_BARS));
		preferences.putBoolean(Options.SHOW_PP_GAIN_FLOATING_TEXT_STRING , preferences.getBoolean(Options.SHOW_PP_GAIN_FLOATING_TEXT_STRING, Options.SHOW_PP_GAIN_FLOATING_TEXT));
	}

	public void updateScreenText() {
		if(state == GameState.MAIN_GAME) {
			hud.updateText();
		} else if(state == GameState.CUSTOMIZE) {
			playerBuilder.updateText();
		}
	}

	/**
	 * Must be called while current screen is main menu
	 */
	public void startGame() {
		playerDead = false;
		try {
			engine.addEntity(player);
		} catch(IllegalArgumentException e) {

		}
		// map.enterNewArea(engine, player, (int)map.getFocus().x, (int)map.getFocus().y);

		gestureListener.setPlayer(player);
		hud = new HUD(Main.this, assetManager, inputMultiplexer, gestureListener, player, map);
		setScreen(hud);
		state = GameState.MAIN_GAME;
		camera.setFocus(player);
		shootingSystem.setPlayer(player);
	}

	@Override
	public void pause() {

	}

	@Override
	public void resume() {

	}

	@Override
	public void render() {
		float deltaTime = Gdx.graphics.getDeltaTime();

		assetManager.update();
		if (state == GameState.MAIN_GAME) {
			if (!playerDead) {
				engine.update(deltaTime);
			} else {
				// Update all systems except for ShootingSystem
				engine.getSystem(AISystem.class).update(deltaTime);
				engine.getSystem(MovementSystem.class).update(deltaTime);
				engine.getSystem(RenderSystem.class).update(deltaTime);
			}
			camera.update();
		} else if (state == GameState.MAIN_MENU) {
			engine.getSystem(RenderSystem.class).update(deltaTime);
			camera.update();
		}
		super.render();
	}
	
	@Override
	public void dispose() {

	}

	public void onPlayerDeath() {
		playerDead = true;

		//TODO: death screen --> wait a bit

		//TODO: save high score
		Save.deleteSave();

		Save.load(this);
		loadMainMenu();
	}

	public void save() {
        Save.save(this);
    }

	public AssetManager getAssetManager() {
		return assetManager;
	}

	public Preferences getPreferences() {
		return preferences;
	}

	public GameState getState() {
		return state;
	}

	public HUD getHud() {
		return hud;
	}

	public void setHud(HUD hud) {
		this.hud = hud;
	}

    public PooledEngine getEngine() {
        return engine;
    }

    public Map getMap() {
        return map;
    }

    public Entity getPlayer() {
        return player;
    }

	public void setPlayer(Entity player) {
		this.player = player;
	}

	public void setMap(Map map) {
		map.setMain(this);
		this.map = map;
	}

	public Camera getCamera() {
		return camera;
	}

	public boolean isPlayerDead() {
		return playerDead;
	}

	public void setPlayerDead(boolean playerDead) {
		this.playerDead = playerDead;
	}

	public RenderSystem getRenderSystem() {
		return renderSystem;
	}
}
