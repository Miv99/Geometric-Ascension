package screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.miv.Main;

/**
 * Created by Miv on 6/5/2017.
 */
public class Options implements Screen {
    private Stage stage;
    private Music music;
    private InputMultiplexer inputMultiplexer;

    public Options(InputMultiplexer inputMultiplexer, Music music) {
        stage = new Stage();
        this.inputMultiplexer = inputMultiplexer;
        this.music = music;
    }

    @Override
    public void show() {
        inputMultiplexer.addProcessor(stage);
        music.play();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

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
        inputMultiplexer.removeProcessor(stage);
    }

    @Override
    public void dispose() {

    }
}
