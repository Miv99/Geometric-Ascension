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
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import components.HitboxComponent;
import map.Map;
import map.MapArea;
import map.mods.MapAreaModifier;
import screens.DeathScreen;
import screens.HUD;
import screens.MainMenu;
import screens.MapScreen;
import screens.PlayerBuilder;
import systems.AISystem;
import systems.ExpirationSystem;
import systems.MovementSystem;
import systems.RenderSystem;
import systems.ShootingSystem;

public class Main extends Game {
	public static enum GameState {
		MAIN_MENU,
		MAIN_GAME,
		MAP,
		CUSTOMIZE,
		OPTIONS,
		DEATH_SCREEN
	}

	public static final int SCREEN_WIDTH = 1600;
	public static final int SCREEN_HEIGHT = 900;

	public static final String SKIN_PATH = "glassy\\skin\\glassy-ui.json";
	public static final String[] WORLD_MUSIC_PATHS = new String[] {
			"music\\world1.mp3",
			"music\\world2.ogg",
			"music\\world3.wav",
			"music\\world4.ogg"
	};
	public static final String[] POP_SOUND_PATHS = new String[] {
			"sound_effects\\pop_0.ogg",
			"sound_effects\\pop_1.ogg"
	};
	public static final String MAIN_MENU_MUSIC_1_PATH = "music\\main_menu1.ogg";
	public static final String GAME_OVER_SOUND_PATH = "sound_effects\\game_over.wav";
	public static final String UPGRADE_1_SOUND_PATH = "sound_effects\\upgrade_1.ogg";
	public static final String UPGRADE_2_SOUND_PATH = "sound_effects\\upgrade_2.ogg";
	public static final String UPGRADE_3_SOUND_PATH = "sound_effects\\upgrade_3.ogg";
	public static final String GAIN_PP_SOUND_PATH = "sound_effects\\gain_pp.ogg";
	public static final String WIND_WOOSH_SOUND_PATH = "sound_effects\\wind_woosh.wav";
	public static final String MOVEMENT_ARROW_TAIL_PATH = "movement_arrow_tail.png";
	public static final String MOVEMENT_ARROW_BODY_PATH = "movement_arrow_body.png";
	public static final String MOVEMENT_ARROW_HEAD_PATH = "movement_arrow_head.png";
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
	public static final String CUSTOMIZE_BUTTON_DISABLED_PATH = "customize_button_disabled.png";
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
	public static final String MAP_AREA_TRAVEL_BUTTON_DOWN_PATH = "map_area_travel_button_down.png";
	public static final String ATTACK_PATTERN_INFO_DISPLAY_TOGGLE_BUTTON_UP_PATH = "attack_pattern_info_display_toggle_button_up.png";
	public static final String ATTACK_PATTERN_INFO_DISPLAY_TOGGLE_BUTTON_DOWN_PATH = "attack_pattern_info_display_toggle_button_down.png";
	public static final String UNDO_BUTTON_UP_PATH = "undo_button_up.png";
	public static final String UNDO_BUTTON_DOWN_PATH = "undo_button_down.png";

	public static final String BUBBLE_DEFAULT_PATH = "bubble_default.png";
    public static final String BUBBLE_SHIELD_PATH = "bubble_shield.png";

	private PooledEngine engine;
	private ShootingSystem shootingSystem;
	private RenderSystem renderSystem;
	private MovementSystem movementSystem;
	private Camera camera;
	private InputMultiplexer inputMultiplexer;
	private GestureListener gestureListener;
	private Map map;
    private Entity player;
	private HitboxComponent playerHitboxComponent;

	private boolean playerDead;

	private AssetManager assetManager;
	private Preferences preferences;
	private Preferences highScores;

	private GameState state;
	private HUD hud;

	// Screens
	private MainMenu mainMenu;
	private PlayerBuilder playerBuilder;
	private screens.Options options;
	private MapScreen mapScreen;

	@Override
	public void create() {
		engine = new PooledEngine(20, 100, 20, 100);

		inputMultiplexer = new InputMultiplexer();
		gestureListener = new GestureListener(this, inputMultiplexer);
		inputMultiplexer.addProcessor(new GestureDetector(gestureListener));
		Gdx.input.setInputProcessor(inputMultiplexer);

		preferences = Gdx.app.getPreferences("Geometric Ascension");
		highScores = Gdx.app.getPreferences("Geometric Ascension High Scores");
		loadPreferences();
		savePreferences();
		loadAssets();

		renderSystem = new RenderSystem(this);
		assetManager.finishLoading();
		loadMainMenuMapPreview();
		renderSystem.setMap(map);

		// Add entity systems
		engine.addSystem(new AISystem());
		movementSystem = new MovementSystem(this, engine, map, player);
		engine.addSystem(movementSystem);
		shootingSystem = new ShootingSystem(map, engine);
		engine.addSystem(shootingSystem);
		engine.addSystem(renderSystem);
		engine.addSystem(new ExpirationSystem());

		camera = new Camera(renderSystem);
		camera.resetViewport();
		camera.position.set(0, 0, 0);
		camera.setFocus(player);

		movementSystem.loadAssets(assetManager);
		shootingSystem.loadAssets(assetManager);
		renderSystem.loadTextures(assetManager);
		loadMainMenu();
	}

	public void deleteSave() {
		Save.deleteSave();
		engine.removeAllEntities();
		loadMainMenuMapPreview();

		renderSystem.setMap(map);
		movementSystem.setMap(map);
		movementSystem.setPlayer(player);
		shootingSystem.setMap(map);

		loadMainMenu();
	}

	public void loadOptionsScreen(Music musicToBeContinuedPlaying, boolean showMainMenuOnBackButtonClick) {
		if(options == null) {
			options = new screens.Options(this, assetManager, inputMultiplexer, musicToBeContinuedPlaying);
		} else {
			options.setMusic(musicToBeContinuedPlaying);
		}
		options.setShowMainMenuOnBackButtonClick(showMainMenuOnBackButtonClick);
		setScreen(options);
		state = GameState.OPTIONS;
	}

	public void loadMainMenuMapPreview() {
		Save.load(this);
		engine.addEntity(player);
		Mappers.hitbox.get(player).setLastFacedAngle(MathUtils.PI / 2f);
		Mappers.hitbox.get(player).setTargetAngle(MathUtils.PI / 2f);
		playerDead = false;
		for(MapArea area : map.getAllSavedMapAreas()) {
			area.setEngine(engine);
		}
		map.enterNewArea(engine, player, (int) map.getFocus().x, (int) map.getFocus().y, true);
		if (camera != null) {
			camera.setLockedPosition(false);
			camera.setFocus(player);
			camera.teleportTo(player);
		}
	}

	public void loadMainMenu() {
		// Stop all other music
		if(hud != null) {
			hud.stopMusic();
		}
		if(mainMenu != null) {
			mainMenu.stopMusic();
		}
		if(options != null) {
			options.stopMusic();
		}

		if(mainMenu == null) {
			mainMenu = new MainMenu(this, assetManager, inputMultiplexer);
		}
		playerDead = false;
		setScreen(mainMenu);
		state = GameState.MAIN_MENU;
	}

	public void loadMapScreen() {
		if(mapScreen == null) {
			mapScreen = new MapScreen(this, assetManager, engine, player, map, inputMultiplexer);
			gestureListener.setMapScreen(mapScreen);
		}
		setScreen(mapScreen);
		state = GameState.MAP;
	}

	public void loadCustomizeScreen() {
		if(playerBuilder == null) {
			playerBuilder = new PlayerBuilder(this, inputMultiplexer, assetManager, player);
		}
		setScreen(playerBuilder);
		state = GameState.CUSTOMIZE;
	}

	public void loadHUD() {
		if(hud == null) {
			hud = new HUD(this, assetManager, inputMultiplexer, gestureListener, player, map);
		}
		setScreen(hud);
		state = GameState.MAIN_GAME;
	}

	public void loadDeathScreen(float score, boolean newHighScore) {
		setScreen(new DeathScreen(this, inputMultiplexer, assetManager, score, map, newHighScore));
		state = GameState.DEATH_SCREEN;
	}

	public void loadAssets() {
		InternalFileHandleResolver fileHandler = new InternalFileHandleResolver();
		assetManager = new AssetManager(fileHandler);
		assetManager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(fileHandler));
		assetManager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(fileHandler));

		assetManager.load(SKIN_PATH, Skin.class);
		for(String s : WORLD_MUSIC_PATHS) {
			assetManager.load(s, Music.class);
		}
		for(String s : POP_SOUND_PATHS) {
			assetManager.load(s, Sound.class);
		}
		assetManager.load(MAIN_MENU_MUSIC_1_PATH, Music.class);

		assetManager.load(GAME_OVER_SOUND_PATH, Sound.class);
		assetManager.load(UPGRADE_1_SOUND_PATH, Sound.class);
		assetManager.load(UPGRADE_2_SOUND_PATH, Sound.class);
		assetManager.load(UPGRADE_3_SOUND_PATH, Sound.class);
		assetManager.load(GAIN_PP_SOUND_PATH, Sound.class);
		assetManager.load(WIND_WOOSH_SOUND_PATH, Sound.class);

		assetManager.load(MOVEMENT_ARROW_TAIL_PATH, Texture.class);
		assetManager.load(MOVEMENT_ARROW_BODY_PATH, Texture.class);
		assetManager.load(MOVEMENT_ARROW_HEAD_PATH, Texture.class);

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
		assetManager.load(CUSTOMIZE_BUTTON_DISABLED_PATH, Texture.class);
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
		assetManager.load(MAP_AREA_TRAVEL_BUTTON_DOWN_PATH, Texture.class);
		assetManager.load(ATTACK_PATTERN_INFO_DISPLAY_TOGGLE_BUTTON_UP_PATH, Texture.class);
		assetManager.load(ATTACK_PATTERN_INFO_DISPLAY_TOGGLE_BUTTON_DOWN_PATH, Texture.class);
		assetManager.load(UNDO_BUTTON_UP_PATH, Texture.class);
		assetManager.load(UNDO_BUTTON_DOWN_PATH, Texture.class);

		assetManager.load(BUBBLE_SHIELD_PATH, Texture.class);
		assetManager.load(BUBBLE_DEFAULT_PATH, Texture.class);
	}

	public void savePreferences() {
		preferences.putFloat(Options.MUSIC_VOLUME_STRING, Options.MUSIC_VOLUME);
		preferences.putFloat(Options.SOUND_VOLUME_STRING, Options.SOUND_VOLUME);
		preferences.putFloat(Options.MASTER_VOLUME_STRING, Options.MASTER_VOLUME);
		preferences.putFloat(Options.MOVEMENT_DRAG_ARROW_MAX_DISTANCE_STRING, Options.MOVEMENT_DRAG_ARROW_MAX_DISTANCE);
		preferences.putBoolean(Options.SHOW_ENEMY_HEALTH_BARS_STRING, Options.SHOW_ENEMY_HEALTH_BARS);
		preferences.putBoolean(Options.SHOW_PLAYER_HEALTH_BARS_STRING, Options.SHOW_PLAYER_HEALTH_BARS);
		preferences.putBoolean(Options.SHOW_PP_GAIN_FLOATING_TEXT_STRING, Options.SHOW_PP_GAIN_FLOATING_TEXT);
		preferences.flush();
	}

	public void loadPreferences() {
		Options.MUSIC_VOLUME = preferences.getFloat(Options.MUSIC_VOLUME_STRING, Options.MUSIC_VOLUME);
		Options.SOUND_VOLUME = preferences.getFloat(Options.SOUND_VOLUME_STRING, Options.SOUND_VOLUME);
		Options.MASTER_VOLUME = preferences.getFloat(Options.MASTER_VOLUME_STRING, Options.MASTER_VOLUME);
		Options.MOVEMENT_DRAG_ARROW_MAX_DISTANCE = preferences.getFloat(Options.MOVEMENT_DRAG_ARROW_MAX_DISTANCE_STRING, Options.MOVEMENT_DRAG_ARROW_MAX_DISTANCE);
		Options.SHOW_ENEMY_HEALTH_BARS = preferences.getBoolean(Options.SHOW_ENEMY_HEALTH_BARS_STRING, Options.SHOW_ENEMY_HEALTH_BARS);
		Options.SHOW_PLAYER_HEALTH_BARS = preferences.getBoolean(Options.SHOW_PLAYER_HEALTH_BARS_STRING, Options.SHOW_PLAYER_HEALTH_BARS);
		Options.SHOW_PP_GAIN_FLOATING_TEXT = preferences.getBoolean(Options.SHOW_PP_GAIN_FLOATING_TEXT_STRING, Options.SHOW_PP_GAIN_FLOATING_TEXT);
	}

	public void updateScreenActors() {
		if(state == GameState.MAIN_GAME) {
			hud.updateActors();
		} else if(state == GameState.CUSTOMIZE) {
			playerBuilder.updateActors();
		} else if(state == GameState.MAP) {
			mapScreen.updateActors();
		}
	}

	/**
	 * Must be called while current screen is main menu
	 */
	public void startGame() {
		playerDead = false;
		try {
			if(!engine.getEntities().contains(player, false)) {
				engine.addEntity(player);
			}
		} catch(IllegalArgumentException e) {

		}
		Mappers.hitbox.get(player).setLastFacedAngle(MathUtils.PI / 2f);
		Mappers.hitbox.get(player).setTargetAngle(MathUtils.PI / 2f);
		gestureListener.setPlayer(player);
		hud = new HUD(Main.this, assetManager, inputMultiplexer, gestureListener, player, map);
		setScreen(hud);
		state = GameState.MAIN_GAME;
		camera.setFocus(player);
		camera.setLockedPosition(false);
		shootingSystem.setPlayer(player);
	}

	@Override
	public void pause() {
		// Save when game is paused instead of in dispose() because user may kill app incorrectly (not using the back button)

		savePreferences();

		// Save game if not in boss area
		if(!map.getCurrentArea().isBossArea()) {
			// Store enemies into ECDs
			map.getCurrentArea().storeExistingEnemies(engine, false);

			if(!state.equals(GameState.MAIN_MENU)) {
				Save.save(this);
			}
		}
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
				playerHitboxComponent.update(deltaTime);
				engine.update(deltaTime);
				map.getCurrentArea().update(deltaTime);
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

	public void onPlayerDeath(final float score) {
		playerDead = true;
		camera.setLockedPosition(true);

		hud.getAudioPlayer().fadeOut(2.5f);

		// Save high score
		if(score > highScores.getFloat(Options.HIGH_SCORE_STRING, 0f)) {
			highScores.putFloat(Options.HIGH_SCORE_STRING, Mappers.player.get(player).getScore());
			highScores.flush();

			hud.fadeToColor(new Color(1f, 1f, 1f, 1f), 3.5f, new Timer.Task() {
				@Override
				public void run() {
					loadDeathScreen(score, true);
					Save.deleteSave();
				}
			});
		} else {
			hud.fadeToColor(new Color(1f, 1f, 1f, 1f), 3.5f, new Timer.Task() {
				@Override
				public void run() {
					loadDeathScreen(score, false);
					Save.deleteSave();
				}
			});
		}
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
		playerHitboxComponent = Mappers.hitbox.get(player);
		if(movementSystem != null) {
			movementSystem.setPlayer(player);
		}
		if(hud != null) {
			hud.setPlayer(player);
		}
		if(playerBuilder != null) {
			playerBuilder.setPlayer(player);
		}
		if(mapScreen != null) {
			mapScreen.setPlayer(player);
		}
		if(map != null) {
			for(MapArea area : map.getAllSavedMapAreas()) {
				if(area.getMods() != null) {
					for (MapAreaModifier mod : area.getMods()) {
						mod.setPlayer(player);
					}
				}
			}
		}
	}

	public void setMap(Map map) {
		map.setMain(this);
		if(renderSystem != null) {
			renderSystem.setMap(map);
		}
		if(movementSystem != null) {
			movementSystem.setMap(map);
		}
		if(mapScreen != null) {
			mapScreen.setMap(map);
		}
		if(shootingSystem != null) {
			shootingSystem.setMap(map);
		}
		this.map = map;
		map.setCurrentArea(map.getFocus());
	}

	public void setMapAreaModFields() {
		for(MapArea area : map.getAllSavedMapAreas()) {
			for(MapAreaModifier m : area.getMods()) {
				m.setMapArea(area);
				m.setPlayer(player);
				m.setAssetManager(assetManager);
				m.setEngine(engine);
			}
		}
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

	public screens.Options getOptions() {
		return options;
	}

	public PlayerBuilder getPlayerBuilder() {
		return playerBuilder;
	}
}
