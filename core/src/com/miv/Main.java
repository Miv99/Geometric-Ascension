package com.miv;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import components.HitboxComponent;
import components.PlayerComponent;
import map.Map;
import map.MapArea;
import screens.HUD;
import screens.MainMenu;
import systems.AISystem;
import systems.MovementSystem;
import systems.RenderSystem;
import systems.ShootingSystem;
import utils.CircleHitbox;
import utils.Point;

public class Main extends Game {
	public static enum GameState {
		MAIN_MENU,
		MAIN_GAME
	}

	public static final int SCREEN_WIDTH = 1600;
	public static final int SCREEN_HEIGHT = 900;

	public static final String SKIN_PATH = "glassy\\skin\\glassy-ui.json";
	public static final String DEFAULT_FONT_PATH = "Roboto-Regular.ttf";
	public static final String WORLD_MUSIC_1_PATH = "music\\world1.mp3";
	public static final String WORLD_MUSIC_2_PATH = "music\\world2.ogg";
	public static final String WORLD_MUSIC_3_PATH = "music\\world3.wav";
	public static final String MAIN_MENU_MUSIC_1_PATH = "music\\main_menu1.ogg";
	public static final String MAIN_MENU_BACKGROUND_PATH = "main_menu_background.png";
	public static final String MOVEMENT_ARROW_TAIL_PATH = "movement_arrow_tail.png";
	public static final String MOVEMENT_ARROW_BODY_PATH = "movement_arrow_body.png";
	public static final String MOVEMENT_ARROW_HEAD_PATH = "movement_arrow_head.png";
	public static final String ATTACK_BUTTON_UP_PATH = "attack_button_up.png";
	public static final String ATTACK_BUTTON_DOWN_PATH = "attack_button_down.png";
	public static final String BUBBLE_DEFAULT_PATH = "bubble_default.png";
    public static final String BUBBLE_SHIELD_PATH = "bubble_shield.png";

	private PooledEngine engine;
	private ShootingSystem shootingSystem;
	private Camera camera;
	private Viewport viewport;
	private InputMultiplexer inputMultiplexer;
	private GestureListener gestureListener;
	private Map map;
    private Entity player;

	private boolean playerDead;

	private AssetManager assetManager;
	private Preferences preferences;

	private GameState state;
	private HUD hud;

	// Screens
	private MainMenu mainMenu;

	@Override
	public void create() {
		engine = new PooledEngine(20, 100, 20, 100);

		inputMultiplexer = new InputMultiplexer();
		gestureListener = new GestureListener(this, inputMultiplexer);
		inputMultiplexer.addProcessor(new GestureDetector(gestureListener));
		Gdx.input.setInputProcessor(inputMultiplexer);

		loadPreferences();
		loadAssets();

		Save.load(this);

		// Add entity systems
		engine.addSystem(new AISystem());
		engine.addSystem(new MovementSystem(engine, map));
		shootingSystem = new ShootingSystem(engine);
		engine.addSystem(shootingSystem);
		RenderSystem renderSystem = new RenderSystem(map);
		engine.addSystem(renderSystem);

		camera = new Camera(renderSystem);
		viewport = new FillViewport(SCREEN_WIDTH, SCREEN_HEIGHT, camera);
		viewport.apply();
		camera.update();

		assetManager.finishLoading();
		renderSystem.loadTextures(assetManager);
		loadMainMenu();
	}

	public void loadMainMenu() {
		if(mainMenu == null) {
			mainMenu = new MainMenu(this, assetManager, inputMultiplexer);
		}
		setScreen(mainMenu);
		state = GameState.MAIN_MENU;
	}

	public void loadAssets() {
		InternalFileHandleResolver fileHandler = new InternalFileHandleResolver();
		assetManager = new AssetManager(fileHandler);
		assetManager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(fileHandler));
		assetManager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(fileHandler));

		assetManager.load(SKIN_PATH, Skin.class);
		assetManager.load(DEFAULT_FONT_PATH, FreeTypeFontGenerator.class);
		assetManager.load(WORLD_MUSIC_1_PATH, Music.class);
		assetManager.load(WORLD_MUSIC_2_PATH, Music.class);
		assetManager.load(WORLD_MUSIC_3_PATH, Music.class);
		assetManager.load(MAIN_MENU_MUSIC_1_PATH, Music.class);
		assetManager.load(MAIN_MENU_BACKGROUND_PATH, Texture.class);
		assetManager.load(MOVEMENT_ARROW_TAIL_PATH, Texture.class);
		assetManager.load(MOVEMENT_ARROW_BODY_PATH, Texture.class);
		assetManager.load(MOVEMENT_ARROW_HEAD_PATH, Texture.class);
		assetManager.load(ATTACK_BUTTON_UP_PATH, Texture.class);
		assetManager.load(ATTACK_BUTTON_DOWN_PATH, Texture.class);

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
	}

	public void startGame() {
		playerDead = false;
		engine.addEntity(player);
		map.enterNewArea(engine, player, 0, 0);

		gestureListener.setPlayer(player);
		hud = new HUD(assetManager, inputMultiplexer, gestureListener, player, map);
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
		if(state == GameState.MAIN_GAME) {
			if(!playerDead) {
				engine.update(deltaTime);
			} else {
				// Update all systems except for ShootingSystem
				engine.getSystem(AISystem.class).update(deltaTime);
				engine.getSystem(MovementSystem.class).update(deltaTime);
				engine.getSystem(RenderSystem.class).update(deltaTime);
			}
			camera.update();
		}
		super.render();
	}
	
	@Override
	public void dispose() {

	}

	public void onPlayerDeath() {
		playerDead = true;
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
}
