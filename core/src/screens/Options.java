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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.miv.AudioPlayer;
import com.miv.Main;
import com.miv.Mappers;
import com.miv.Save;

import components.HitboxComponent;

/**
 * Created by Miv on 6/5/2017.
 */
public class Options implements Screen {
    private Stage stage;
    private Music music;
    private AudioPlayer audioPlayer;
    private InputMultiplexer inputMultiplexer;
    private boolean showMainMenuOnBackButtonClick;

    public Options(final Main main, AssetManager assetManager, InputMultiplexer inputMultiplexer, final Music music) {
        stage = new Stage();
        this.inputMultiplexer = inputMultiplexer;
        this.music = music;

        Skin skin = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.SKIN_PATH).path());

        final float height = Gdx.graphics.getHeight();

        final float TOP_PADDING = 25f;
        final float LEFT_PADDING = 25f;

        final float SLIDER_HEIGHT = 30f;
        final float SLIDER_LENGTH = 450f;
        final float SLIDER_PADDING = 50f;

        final float LABEL_HEIGHT = 30f;
        final float LABEL_PADDING = 10f;

        final float CHECKBOX_PADDING = 30f;
        final float CHECKBOX_WIDTH = 60f;
        final float CHECKBOX_HEIGHT = 60f;

        // Back button
        Texture backButtonUp = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.BACK_BUTTON_UP_PATH).path());
        Texture backButtonDown = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.BACK_BUTTON_DOWN_PATH).path());
        ImageButton.ImageButtonStyle backButtonStyle = new ImageButton.ImageButtonStyle(new TextureRegionDrawable(new TextureRegion(backButtonUp)), new TextureRegionDrawable(new TextureRegion(backButtonDown)), null, null, null, null);
        ImageButton backButton = new ImageButton(backButtonStyle);
        backButton.setSize(70f, 70f);
        backButton.setPosition(LEFT_PADDING, TOP_PADDING);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                main.savePreferences();
                if(showMainMenuOnBackButtonClick) {
                    main.loadMainMenu();
                } else {
                    main.loadHUD();
                }
            }
        });
        stage.addActor(backButton);

        // Create master volume label
        final Label masterVolumeLabel = new Label("Master volume: " + (int)(com.miv.Options.MASTER_VOLUME*100) + "%", skin);
        masterVolumeLabel.setFontScale(2f);
        masterVolumeLabel.setSize(SLIDER_LENGTH, LABEL_HEIGHT);
        masterVolumeLabel.setPosition(LEFT_PADDING, height - TOP_PADDING - masterVolumeLabel.getHeight());
        masterVolumeLabel.setColor(Color.BLACK);
        stage.addActor(masterVolumeLabel);
        // Create master volume slider
        final Slider masterVolume = new Slider(0, 100, 1, false, skin);
        masterVolume.setValue((int)(com.miv.Options.MASTER_VOLUME*100));
        masterVolume.setSize(SLIDER_LENGTH, SLIDER_HEIGHT);
        masterVolume.setPosition(LEFT_PADDING, masterVolumeLabel.getY() - SLIDER_HEIGHT - LABEL_PADDING);
        masterVolume.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                com.miv.Options.MASTER_VOLUME = masterVolume.getValue()/100f;
                if(Options.this.music != null) {
                    Options.this.music.setVolume(com.miv.Options.MASTER_VOLUME * com.miv.Options.MUSIC_VOLUME);
                }
                if(audioPlayer != null) {
                    audioPlayer.onVolumeChange();
                }
                masterVolumeLabel.setText("Master volume: " + (int)(com.miv.Options.MASTER_VOLUME*100) + "%");
            }
        });
        stage.addActor(masterVolume);

        // Create music volume label
        final Label musicVolumeLabel = new Label("Music volume: " + (int)(com.miv.Options.MUSIC_VOLUME*100) + "%", skin);
        musicVolumeLabel.setFontScale(2f);
        musicVolumeLabel.setSize(SLIDER_LENGTH, LABEL_HEIGHT);
        musicVolumeLabel.setPosition(LEFT_PADDING, masterVolume.getY() - LABEL_HEIGHT - SLIDER_PADDING);
        musicVolumeLabel.setColor(Color.BLACK);
        stage.addActor(musicVolumeLabel);
        // Create music volume slider
        final Slider musicVolume = new Slider(0, 100, 1, false, skin);
        musicVolume.setValue((int)(com.miv.Options.MUSIC_VOLUME*100));
        musicVolume.setSize(SLIDER_LENGTH, SLIDER_HEIGHT);
        musicVolume.setPosition(LEFT_PADDING, musicVolumeLabel.getY() - SLIDER_HEIGHT - LABEL_PADDING);
        musicVolume.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                com.miv.Options.MUSIC_VOLUME = musicVolume.getValue()/100f;
                if(Options.this.music != null) {
                    Options.this.music.setVolume(com.miv.Options.MASTER_VOLUME * com.miv.Options.MUSIC_VOLUME);
                }
                if(audioPlayer != null) {
                    audioPlayer.onVolumeChange();
                }
                musicVolumeLabel.setText("Music volume: " + (int)(com.miv.Options.MUSIC_VOLUME*100) + "%");
            }
        });
        stage.addActor(musicVolume);

        // Create sound volume label
        final Label soundVolumeLabel = new Label("Sound effects volume: " + (int)(com.miv.Options.SOUND_VOLUME*100) + "%", skin);
        soundVolumeLabel.setFontScale(2f);
        soundVolumeLabel.setSize(SLIDER_LENGTH, LABEL_HEIGHT);
        soundVolumeLabel.setPosition(LEFT_PADDING, musicVolume.getY() - LABEL_HEIGHT - SLIDER_PADDING);
        soundVolumeLabel.setColor(Color.BLACK);
        stage.addActor(soundVolumeLabel);
        // Create sound volume slider
        final Slider soundVolume = new Slider(0, 100, 1, false, skin);
        soundVolume.setValue((int)(com.miv.Options.SOUND_VOLUME*100));
        soundVolume.setSize(SLIDER_LENGTH, SLIDER_HEIGHT);
        soundVolume.setPosition(LEFT_PADDING, soundVolumeLabel.getY() - SLIDER_HEIGHT - LABEL_PADDING);
        soundVolume.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                com.miv.Options.SOUND_VOLUME = soundVolume.getValue()/100f;
                soundVolumeLabel.setText("Sound effects volume: " + (int)(com.miv.Options.SOUND_VOLUME*100) + "%");
            }
        });
        stage.addActor(soundVolume);

        // Create player health bar toggle
        final CheckBox playerHealthBar = new CheckBox("Show player health bars", skin);
        playerHealthBar.setChecked(com.miv.Options.SHOW_PLAYER_HEALTH_BARS);
        playerHealthBar.getLabelCell().padLeft(15f);
        playerHealthBar.getLabel().setColor(Color.BLACK);
        playerHealthBar.getLabel().setFontScale(2f);
        playerHealthBar.align(Align.left);
        playerHealthBar.setSize(CHECKBOX_WIDTH, CHECKBOX_HEIGHT);
        playerHealthBar.setPosition(masterVolume.getX() + SLIDER_LENGTH + 120f, masterVolume.getY());
        playerHealthBar.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                com.miv.Options.SHOW_PLAYER_HEALTH_BARS = !com.miv.Options.SHOW_PLAYER_HEALTH_BARS;
                playerHealthBar.setChecked(com.miv.Options.SHOW_PLAYER_HEALTH_BARS);
            }
        });
        stage.addActor(playerHealthBar);

        // Create player health bar toggle
        final CheckBox enemyHealthBar = new CheckBox("Show enemy health bars", skin);
        enemyHealthBar.setChecked(com.miv.Options.SHOW_ENEMY_HEALTH_BARS);
        enemyHealthBar.getLabelCell().padLeft(15f);
        enemyHealthBar.getLabel().setColor(Color.BLACK);
        enemyHealthBar.getLabel().setFontScale(2f);
        enemyHealthBar.align(Align.left);
        enemyHealthBar.setSize(CHECKBOX_WIDTH, CHECKBOX_HEIGHT);
        enemyHealthBar.setPosition(playerHealthBar.getX(), playerHealthBar.getY() - CHECKBOX_HEIGHT - CHECKBOX_PADDING);
        enemyHealthBar.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                com.miv.Options.SHOW_ENEMY_HEALTH_BARS = !com.miv.Options.SHOW_ENEMY_HEALTH_BARS;
                enemyHealthBar.setChecked(com.miv.Options.SHOW_ENEMY_HEALTH_BARS);
            }
        });
        stage.addActor(enemyHealthBar);

        // Create pp gain display toggle
        final CheckBox ppGainDisplay = new CheckBox("Show pp gain text", skin);
        ppGainDisplay.setChecked(com.miv.Options.SHOW_ENEMY_HEALTH_BARS);
        ppGainDisplay.getLabelCell().padLeft(15f);
        ppGainDisplay.getLabel().setColor(Color.BLACK);
        ppGainDisplay.getLabel().setFontScale(2f);
        ppGainDisplay.align(Align.left);
        ppGainDisplay.setSize(CHECKBOX_WIDTH, CHECKBOX_HEIGHT);
        ppGainDisplay.setPosition(enemyHealthBar.getX(), enemyHealthBar.getY() - CHECKBOX_HEIGHT - CHECKBOX_PADDING);
        ppGainDisplay.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                com.miv.Options.SHOW_PP_GAIN_FLOATING_TEXT = !com.miv.Options.SHOW_PP_GAIN_FLOATING_TEXT;
                ppGainDisplay.setChecked(com.miv.Options.SHOW_PP_GAIN_FLOATING_TEXT);
            }
        });
        stage.addActor(ppGainDisplay);

        // Delete save button
        TextButton deleteSave = new TextButton("Delete save", skin);
        deleteSave.getLabel().setColor(Color.WHITE);
        deleteSave.getLabel().setFontScale(0.6f);
        deleteSave.setSize(250, 80);
        deleteSave.setPosition(Gdx.graphics.getWidth() - LEFT_PADDING - deleteSave.getWidth(), TOP_PADDING);
        deleteSave.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                main.deleteSave();
            }
        });
        stage.addActor(deleteSave);
    }

    @Override
    public void show() {
        inputMultiplexer.addProcessor(stage);
        if(music != null) {
            music.play();
            music.setVolume(com.miv.Options.MASTER_VOLUME * com.miv.Options.MUSIC_VOLUME);
        }
    }

    @Override
    public void render(float delta) {
        // Background color
        Gdx.gl.glClearColor(240/255f, 1, 1, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        //TODO: background image

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {
        if(music != null) {
            music.pause();
        }
    }

    @Override
    public void resume() {
        if(music != null) {
            music.play();
            music.setVolume(com.miv.Options.MASTER_VOLUME * com.miv.Options.MUSIC_VOLUME);
        }
    }

    @Override
    public void hide() {
        inputMultiplexer.removeProcessor(stage);
    }

    @Override
    public void dispose() {

    }

    public void stopMusic() {
        if(music != null) {
            music.stop();
        }
        if(audioPlayer != null) {
            audioPlayer.stop();
        }
    }

    public void setMusic(Music music) {
        this.music = music;
    }

    public void setShowMainMenuOnBackButtonClick(boolean showMainMenuOnBackButtonClick) {
        this.showMainMenuOnBackButtonClick = showMainMenuOnBackButtonClick;
    }

    public void setAudioPlayer(AudioPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;
    }
}
