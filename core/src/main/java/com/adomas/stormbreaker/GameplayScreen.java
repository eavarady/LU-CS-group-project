package com.adomas.stormbreaker;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class GameplayScreen implements Screen {

    private final StormbreakerGame game;
    private final float speed = 200; // pixels per second, we can adjust later
    private ShapeRenderer shapeRenderer;
    private SpriteBatch spriteBatch;
    private Player player;

    public GameplayScreen(StormbreakerGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        shapeRenderer = new ShapeRenderer();
        spriteBatch = new SpriteBatch();
        player = new Player(100, 100, speed, "PlayerSprite.png");
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        player.update(delta);

        spriteBatch.begin();
        player.render(spriteBatch);
        spriteBatch.end();
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        spriteBatch.dispose();
        player.dispose();
    }
}
