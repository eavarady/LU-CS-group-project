/*
 Simple gameplay screen prototype where a sprite (player) moves towards the mouse cursor.
 Will need to create a separate player class at some point.
 */

package com.adomas.stormbreaker;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class GameplayScreen implements Screen {

    private final StormbreakerGame game;
    private final float speed = 200; // pixels per second
    private ShapeRenderer shapeRenderer;
    private Texture soldierTexture;
    private SpriteBatch spriteBatch;
    private float soldierX = 100, soldierY = 100;

    public GameplayScreen(StormbreakerGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        shapeRenderer = new ShapeRenderer();
        spriteBatch = new SpriteBatch();
        soldierTexture = new Texture(Gdx.files.internal("PlayerSprite.png")); // replace with your actual filename
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();

        float dx = mouseX - soldierX;
        float dy = mouseY - soldierY;
        float length = (float)Math.sqrt(dx * dx + dy * dy);

        if (length != 0) {
            float dirX = dx / length;
            float dirY = dy / length;
            float perpX = -dirY;
            float perpY = dirX;
            float moveAmount = speed * delta;

            if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.W)) {
                soldierX += dirX * moveAmount;
                soldierY += dirY * moveAmount;
            }
            if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.S)) {
                soldierX -= dirX * moveAmount;
                soldierY -= dirY * moveAmount;
            }
            if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.A)) {
                soldierX += perpX * moveAmount;
                soldierY += perpY * moveAmount;
            }
            if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.D)) {
                soldierX -= perpX * moveAmount;
                soldierY -= perpY * moveAmount;
            }
        }

        spriteBatch.begin();
        spriteBatch.draw(soldierTexture, soldierX - soldierTexture.getWidth() / 2f, soldierY - soldierTexture.getHeight() / 2f);
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
        soldierTexture.dispose();
    }
}
