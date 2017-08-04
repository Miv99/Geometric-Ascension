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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.miv.Main;
import com.miv.Mappers;
import com.miv.Save;

import components.HitboxComponent;
import components.PlayerComponent;

/**
 * Created by Miv on 7/31/2017.
 */
public class PlayerBuilder implements Screen {
    private Stage stage;
    private InputMultiplexer inputMultiplexer;
    private Entity player;

    private Label pp;
    private TextButton heal;

    public PlayerBuilder(final Main main, InputMultiplexer inputMultiplexer, AssetManager assetManager, final Entity player) {
        this.inputMultiplexer = inputMultiplexer;
        this.player = player;
        stage = new Stage();

        Skin skin = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.SKIN_PATH).path());

        final float width = Gdx.graphics.getWidth();
        final float height = Gdx.graphics.getHeight();

        final float LEFT_PADDING = 25f;
        final float TOP_PADDING = 25f;

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
                main.loadHUD();
            }
        });
        stage.addActor(backButton);

        pp = new Label("", skin);

        // Heal button
        heal = new TextButton("Heal (" + Math.round(Mappers.hitbox.get(player).getTotalHealingCostInPP()) + "pp)", skin);
        heal.getLabel().setColor(Color.WHITE);
        heal.getLabel().setFontScale(0.6f);
        heal.setSize(450, 80);
        heal.setPosition(width - LEFT_PADDING - heal.getWidth(), height - TOP_PADDING - heal.getHeight());
        heal.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                PlayerComponent playerComponent = Mappers.player.get(player);
                HitboxComponent hitboxComponent = Mappers.hitbox.get(player);

                playerComponent.setPixelPoints(hitboxComponent.heal(playerComponent.getPixelPoints()));
                heal.setText("Heal (" + Math.round(Mappers.hitbox.get(player).getTotalHealingCostInPP()) + "pp)");
                pp.setText(Math.round(Mappers.player.get(player).getPixelPoints()) + "pp");
            }
        });
        stage.addActor(heal);

        pp.setFontScale(2.7f);
        pp.setText(Math.round(Mappers.player.get(player).getPixelPoints()) + "pp");
        pp.pack();
        pp.setPosition(LEFT_PADDING, heal.getY() + 25f);
        pp.setColor(Color.BLACK);
        stage.addActor(pp);
    }

    @Override
    public void show() {
        inputMultiplexer.addProcessor(stage);

        updateText();
    }

    public void updateText() {
        heal.setText("Heal (" + Math.round(Mappers.hitbox.get(player).getTotalHealingCostInPP()) + "pp)");
        pp.setText(Math.round(Mappers.player.get(player).getPixelPoints()) + "pp");
    }

    @Override
    public void render(float delta) {
        // Background color
        Gdx.gl.glClearColor(240/255f, 1, 1, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

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
}
