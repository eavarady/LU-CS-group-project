package com.adomas.stormbreaker;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;

public class Player extends Character {
    private OrthographicCamera camera;

    public Player(float x, float y, float speed, String texturePath, OrthographicCamera camera) {
        super(x, y, speed, texturePath);
        this.camera = camera;
    }

    @Override
    public void update(float delta) {
        float moveAmount = speed * delta;

        // WASD movement
        if (Gdx.input.isKeyPressed(Input.Keys.W)) y += moveAmount;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) y -= moveAmount;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) x -= moveAmount;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) x += moveAmount;

        // Rotate to face mouse
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.input.getY();

        // Convert screen coordinates to world coordinates
        Vector3 worldCoordinates = camera.unproject(new Vector3(mouseX, mouseY, 0));
        float dx = worldCoordinates.x - x;
        float dy = worldCoordinates.y - y;

        if (dx != 0 || dy != 0) {
            rotation = (float) Math.toDegrees(Math.atan2(dy, dx)) - 90;
        }
    }

    public void clampPosition(float worldWidth, float worldHeight, float screenWidth, float screenHeight) {
        // Compute conversion factors based on the viewport's effective screen dimensions
        float conversionFactorX = camera.viewportWidth / screenWidth;
        float conversionFactorY = camera.viewportHeight / screenHeight;
    
        // Calculate half the texture size in world units
        float halfWidth = (texture.getWidth() / 2f) * conversionFactorX;
        float halfHeight = (texture.getHeight() / 2f) * conversionFactorY;
    
        // Clamp the player's position within the world dimensions
        x = Math.max(halfWidth, Math.min(x, worldWidth - halfWidth));
        y = Math.max(halfHeight, Math.min(y, worldHeight - halfHeight));
    }

    public float getX() {
        return x;
    }
    
    public float getY() {
        return y;
    }
}