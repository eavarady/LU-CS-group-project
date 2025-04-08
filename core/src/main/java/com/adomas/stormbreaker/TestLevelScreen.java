package com.adomas.stormbreaker;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * this fully replicates GameplayScreen logic but extends LevelScreen.
 * it should behave identically to GameplayScreen
 */
public class TestLevelScreen extends LevelScreen {

    private final float speed = 100f;
    private Player player;

    private OrthographicCamera camera;
    private Viewport viewport;

    public TestLevelScreen(StormbreakerGame game) {
        super(game);
    }

    @Override
    protected void initializeLevel() {

        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 720, camera); // same resolution
        viewport.apply();

        // create the player
        player = new Player(100, 100, speed, "Player_sprite_v1.png", camera);

        // center camera
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // update player
        player.update(delta);

        // clamp position to viewport's width/height
        player.clampPosition(viewport.getWorldWidth(), viewport.getWorldHeight(),
                             viewport.getWorldWidth(), viewport.getWorldHeight());

        // camera follows player
        camera.position.set(player.getX(), player.getY(), 0);
        camera.update();

        // set projection for spriteBatch
        spriteBatch.setProjectionMatrix(camera.combined);

        // optional: draw the dark-gray background with shapeRenderer
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        shapeRenderer.end();

        // draw the player
        spriteBatch.begin();
        player.render(spriteBatch);
        spriteBatch.end();

        // draw dynamic crosshair based on distance from player to mouse
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);

        // get mouse position in screen coordinates and convert to world coordinates
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.input.getY();
        Vector3 mouseWorld = new Vector3(mouseX, mouseY, 0);
        viewport.unproject(mouseWorld);

        // calculate distance from player to mouse
        float dx = mouseWorld.x - player.getX();
        float dy = mouseWorld.y - player.getY();
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        // map distance to crosshair spacing (clamp between 10 and beyond)
        float expansionFactor = 2.5f; // lower = slower expansion, higher = faster. we'll adjust as needed
        float spacing = Math.max(10f, expansionFactor * (float) Math.sqrt(distance));

        // draw crosshair lines
        float cx = mouseWorld.x;
        float cy = mouseWorld.y;
        shapeRenderer.line(cx - spacing, cy, cx - spacing / 2, cy); // left
        shapeRenderer.line(cx + spacing / 2, cy, cx + spacing, cy); // right
        shapeRenderer.line(cx, cy - spacing, cx, cy - spacing / 2); // down
        shapeRenderer.line(cx, cy + spacing / 2, cx, cy + spacing); // up
        // remove mouse cursor and only keep the crosshair
        Gdx.input.setCursorCatched(true);

        shapeRenderer.end();

        // draw white border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        shapeRenderer.end();

        // Escape key to exit
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
            Gdx.input.setCursorCatched(false); // show mouse cursor again
            game.setScreen(new MainMenuScreen(game)); // go back to main menu
        }
    }

    @Override
    public void resize(int width, int height) {
        System.out.println("Resizing to: " + width + "x" + height);
        viewport.update(width, height);
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
    }

    @Override
    public void dispose() {
        super.dispose();
        player.dispose();
    }
}