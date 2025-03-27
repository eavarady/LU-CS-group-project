/*
Basic gameplay screen where a sprite moves wth WASD and aims with the mouse cursor.
Will need to create a separate player class a some point.
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
    private final float speed = 200; // pixels per second, we can adjust later
    private ShapeRenderer shapeRenderer;
    private Texture soldierTexture;
    private SpriteBatch spriteBatch;
    private float soldierX = 100, soldierY = 100;
    private float soldierRotation = 0; // field for rotation angle

    public GameplayScreen(StormbreakerGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        shapeRenderer = new ShapeRenderer();
        spriteBatch = new SpriteBatch();
        soldierTexture = new Texture(Gdx.files.internal("PlayerSprite.png"));
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
            // calculate angle in degrees from the soldier to the mouse
            soldierRotation = (float) Math.toDegrees(Math.atan2(dy, dx)) - 90;
        }

        float moveAmount = speed * delta;

        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.W)) {
            soldierY += moveAmount;
        }
        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.S)) {
            soldierY -= moveAmount;
        }
        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.A)) {
            soldierX -= moveAmount;
        }
        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.D)) {
            soldierX += moveAmount;
        }

        spriteBatch.begin();
        spriteBatch.draw(
            soldierTexture,
            soldierX - soldierTexture.getWidth() / 2f, // x
            soldierY - soldierTexture.getHeight() / 2f, // y
            soldierTexture.getWidth() / 2f,  // originX (center)
            soldierTexture.getHeight() / 2f, // originY (center)
            soldierTexture.getWidth(),       // width
            soldierTexture.getHeight(),      // height
            1f, 1f,            // scaleX, scaleY
            soldierRotation,                 // rotation angle
            0, 0,                  // srcX, srcY
            soldierTexture.getWidth(),       // srcWidth
            soldierTexture.getHeight(),      // srcHeight
            false, false         // flipX, flipY
        );
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
