package com.adomas.stormbreaker;

import com.badlogic.gdx.Game;



public class StormbreakerGame extends Game {

    @Override
    public void create() {
        setScreen(new SplashScreen(this));
    }

}
