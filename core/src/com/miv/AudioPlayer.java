package com.miv;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Timer;

import java.util.Random;

/**
 * Created by Miv on 7/29/2017.
 */
public class AudioPlayer {
    private int lastPlayed;
    private Music[] songs;
    private boolean[] played;
    private boolean isPlaying;

    private float fadeOutVolume;

    public AudioPlayer(AssetManager assetManager, String[] songPaths) {
        songs = new Music[songPaths.length];
        played = new boolean[songPaths.length];

        for(int i = 0; i < songs.length; i++) {
            songs[i] = assetManager.get(assetManager.getFileHandleResolver().resolve(songPaths[i]).path());
        }
        onVolumeChange();
    }

    public void fadeOut(float time) {
        final int songIndex = lastPlayed;
        final float originalVolume = songs[songIndex].getVolume();
        fadeOutVolume = songs[songIndex].getVolume();
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                songs[songIndex].setVolume(fadeOutVolume);
                fadeOutVolume -= originalVolume/100f;
            }
        }, 0f, time/100f);
    }

    public void pause() {
        songs[lastPlayed].pause();
        isPlaying = false;
    }

    public void resume() {
        songs[lastPlayed].play();
        isPlaying = true;
    }

    public void stop() {
        songs[lastPlayed].stop();
        isPlaying = false;
    }

    public void reset() {
        songs[lastPlayed].stop();
        for(int i = 0; i < played.length; i++) {
            played[i] = false;
        }
        isPlaying = false;
    }

    public void playRandomMusic() {
        playMusic(MathUtils.random(songs.length - 1));
    }

    public void playMusic(final int songIndex) {
        songs[lastPlayed].stop();
        lastPlayed = songIndex;
        songs[songIndex].play();
        isPlaying = true;
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

    public void onVolumeChange() {
        for(Music m : songs) {
            m.setVolume(Options.MASTER_VOLUME * Options.MUSIC_VOLUME);
        }
    }

    public boolean isPlaying() {
        return isPlaying;
    }
}
