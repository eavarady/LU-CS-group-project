/* 
 Simple gameplay screen prototype where a dot (player) moves towards the mouse cursor.
 Will need to create a separate player class at soeme point.
 */

package com.adomas.stormbreaker;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class GameplayScreen implements Screen {

    private final StormbreakerGame game;
    private float dotX = 100, dotY = 100;
    private final float speed = 200; // pixels per second
    private ShapeRenderer shapeRenderer;

    public GameplayScreen(StormbreakerGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1); // gray background for now
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // get mouse position
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY(); // flip y to match gdx coordinates

        // calculate vector from dot to mouse
        float dx = mouseX - dotX;
        float dy = mouseY - dotY;

        // calculate distance
        float length = (float)Math.sqrt(dx * dx + dy * dy);

        // avoid division by zero
        if (length != 0) {
            // normalize the direction vector
            float dirX = dx / length;
            float dirY = dy / length;

            // perpendicular vector for left/right movement
            float perpX = -dirY;
            float perpY = dirX;

            float moveAmount = speed * delta;

            // move toward mouse
            if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.W)) {
                dotX += dirX * moveAmount;
                dotY += dirY * moveAmount;
            }

            // move away from mouse
            if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.S)) {
                dotX -= dirX * moveAmount;
                dotY -= dirY * moveAmount;
            }

            // strafe left
            if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.A)) {
                dotX += perpX * moveAmount;
                dotY += perpY * moveAmount;
            }

            // strafe right
            if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.D)) {
                dotX -= perpX * moveAmount;
                dotY -= perpY * moveAmount;
            }
        }

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 1); // black
        shapeRenderer.circle(dotX, dotY, 10); // small circle for the dot
        shapeRenderer.end();
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
    }
}
