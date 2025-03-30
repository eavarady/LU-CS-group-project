package com.adomas.stormbreaker;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.Gdx;

public abstract class Character {
    protected float x, y;
    protected float rotation;
    protected float speed;
    protected Texture texture;

    public Character(float x, float y, float speed, String texturePath) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.texture = new Texture(Gdx.files.internal(texturePath));
    }

    public void render(SpriteBatch batch) {
        batch.draw(
            texture,
            x - texture.getWidth() / 2f,
            y - texture.getHeight() / 2f,
            texture.getWidth() / 2f,
            texture.getHeight() / 2f,
            texture.getWidth(),
            texture.getHeight(),
            1f, 1f,
            rotation,
            0, 0,
            texture.getWidth(),
            texture.getHeight(),
            false, false
        );
    }

    public void dispose() {
        texture.dispose();
    }

    public abstract void update(float delta);
}