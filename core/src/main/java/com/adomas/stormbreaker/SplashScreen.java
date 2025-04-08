package com.adomas.stormbreaker;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public class SplashScreen implements Screen {

    private final StormbreakerGame game;
    private SpriteBatch batch;
    private Texture image;

    public SplashScreen(StormbreakerGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        image = new Texture("STORMBREAKER_splash_v1.png");
    }

    @Override
public void render(float delta) {
    ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
    batch.begin();
    batch.draw(image, Gdx.graphics.getWidth() / 2 - image.getWidth() / 2, Gdx.graphics.getHeight() / 2 - image.getHeight() / 2 - 150);
    batch.end();

    // Wait for any key or mouse press
    if (Gdx.input.justTouched() || Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY)) {
        game.setScreen(new MainMenuScreen(game));
        dispose(); // Clean up splash resources
    }
}

    @Override
    public void resize(int width, int height) {
        // handle resizing if needed
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
        // Called when this screen is no longer the current one
    }

    @Override
    public void dispose() {
        batch.dispose();
        image.dispose();
    }
}