package com.adomas.stormbreaker;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Base class for screens that share spriteBatch and shapeRenderer logic.
 * We'll also keep references to them here for convenience.
 * 
 * Extend this instead of Screen, so child classes get the same lifecycle.
 */
public abstract class LevelScreen implements Screen {

    protected final StormbreakerGame game;
    protected SpriteBatch spriteBatch;
    protected ShapeRenderer shapeRenderer;

    public LevelScreen(StormbreakerGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        // We'll create both batch + shapeRenderer here
        spriteBatch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        
        // Let child classes do camera init, player init, etc.
        initializeLevel();
    }

    /**
     * Called once in show() after we create spriteBatch & shapeRenderer.
     * Perfect place for child to set up camera, viewport, etc.
     */
    protected abstract void initializeLevel();

    @Override
    public void dispose() {
        spriteBatch.dispose();
        shapeRenderer.dispose();
    }

    // Provide empty or default implementations for the rest.
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void resize(int width, int height) {}

}