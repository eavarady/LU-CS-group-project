package com.stormbreaker;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public class SplashScreen implements Screen {

    private final StormbreakerGame game;
    private SpriteBatch batch;
    private Texture image;
    private BitmapFont font;
    private GlyphLayout layout;
    private float stateTime;

    public SplashScreen(StormbreakerGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        image = new Texture("splash_screen_v2.png");
        font = new BitmapFont();
        font.getData().setScale(2.0f);
        layout = new GlyphLayout();
        stateTime = 0f;
        game.menuMusic.play();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        stateTime += delta;
        batch.begin();
        batch.draw(image,0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        String text = "Press any key to continue";
        float alpha = (float)(0.5f + 0.5f * Math.sin(stateTime * 2));
        font.setColor(1f, 1f, 1f, alpha); 

        layout.setText(font, text);
        float x = (Gdx.graphics.getWidth() - layout.width) / 2f;
        float y = 100f; 

        font.draw(batch, layout, x, y);

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
        font.dispose();
    }
}