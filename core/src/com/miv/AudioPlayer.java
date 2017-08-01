package com.miv;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.MathUtils;

import java.util.Random;

/**
 * Created by Miv on 7/29/2017.
 */
public class AudioPlayer {
    private int lastPlayed;
    private Music[] songs;
    private boolean[] played;

    public AudioPlayer(AssetManager assetManager, String[] songPaths) {
        songs = new Music[songPaths.length];
        played = new boolean[songPaths.length];

        for(int i = 0; i < songs.length; i++) {
            songs[i] = assetManager.get(assetManager.getFileHandleResolver().resolve(songPaths[i]).path());
        }
    }

    public void pause() {
        songs[lastPlayed].pause();
    }

    public void resume() {
        songs[lastPlayed].play();
    }

    public void stop() {
        songs[lastPlayed].stop();
    }

    public void reset() {
        songs[lastPlayed].stop();
        for(int i = 0; i < played.length; i++) {
            played[i] = false;
        }
    }

    public void playRandomMusic() {
        playMusic(MathUtils.random(songs.length - 1));
    }

    public void playMusic(final int songIndex) {
        songs[lastPlayed].stop();
        lastPlayed = songIndex;
        songs[songIndex].play();
        played[songIndex] = true;

        songs[songIndex].setOnCompletionListener(new Music.OnCompletionListener() {
            @Override
            public void onCompletion(Music music) {
                int rand = new Random().nextInt(songs.length);
                for(int i = rand; i < rand + songs.length; i++) {
                    if(!played[i % songs.length]) {
                        playMusic(i % songs.length);
                        return;
                    }
                }
                reset();
                // Prevent same song from playing twice in a row
                played[songIndex] = true;
                onCompletion(music);
            }
        });
    }
}
