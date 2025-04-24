package com.adomas.stormbreaker;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;


public class StormbreakerGame extends Game {
    public Music menuMusic;

    @Override
    public void create() {
        menuMusic = Gdx.audio.newMusic(Gdx.files.internal("MusicMenuDef.mp3")); 
        menuMusic.setLooping(true); 
        menuMusic.setVolume(1.0f); 
        setScreen(new SplashScreen(this));
    }

}
