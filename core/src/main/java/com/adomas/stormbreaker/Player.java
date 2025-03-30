package com.adomas.stormbreaker;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class Player extends Character {

    public Player(float x, float y, float speed, String texturePath) {
        super(x, y, speed, texturePath);
    }

    @Override
    public void update(float delta) {
        float moveAmount = speed * delta;

        // wasd movement
        if (Gdx.input.isKeyPressed(Input.Keys.W)) y += moveAmount;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) y -= moveAmount;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) x -= moveAmount;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) x += moveAmount;

        // rotate to face mouse
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
        float dx = mouseX - x;
        float dy = mouseY - y;

        if (dx != 0 || dy != 0) {
            rotation = (float)Math.toDegrees(Math.atan2(dy, dx)) - 90;
        }
    }
}