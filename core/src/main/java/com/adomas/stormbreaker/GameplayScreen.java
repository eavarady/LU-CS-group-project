package com.adomas.stormbreaker;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GameplayScreen implements Screen {

    private final StormbreakerGame game;
    private final float speed = 200; // pixels per second, we can adjust later
    private ShapeRenderer shapeRenderer;
    private SpriteBatch spriteBatch;
    private Player player;

    private OrthographicCamera camera;
    private Viewport viewport;

    public GameplayScreen(StormbreakerGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(1920, 1080, camera); // Set initial virtual resolution
        viewport.apply();

        shapeRenderer = new ShapeRenderer();
        spriteBatch = new SpriteBatch();
        player = new Player(100, 100, speed, "PlayerSprite.png", camera);

        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
    }

    @Override
    public void render(float delta) {
        // Clear the screen with black so that areas outside the world remain black.
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update player and camera.
        player.update(delta);
        player.clampPosition(viewport.getWorldWidth(), viewport.getWorldHeight(), viewport.getWorldWidth(), viewport.getWorldHeight());
        
        // For a following camera, either set directly:
        camera.position.set(player.getX(), player.getY(), 0);
        // or use a smooth follow????
        //updateCameraPositionSmooth(delta); 
        camera.update();
        
        // Set the projection matrix.
        spriteBatch.setProjectionMatrix(camera.combined);

        // (Optional) Draw the world background.
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        shapeRenderer.end();

        // Render game elements (e.g., player).
        spriteBatch.begin();
        player.render(spriteBatch);
        spriteBatch.end();

        // Draw the border around the world.
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        shapeRenderer.end();
    }

    @Override
    public void resize(int width, int height) {
        System.out.println("Resizing to: " + width + "x" + height);
        viewport.update(width, height);
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
    }

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