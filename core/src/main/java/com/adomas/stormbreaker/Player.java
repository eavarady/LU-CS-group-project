package com.adomas.stormbreaker;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.adomas.stormbreaker.tools.CollisionRectangle;
import java.util.Random;

public class Player extends Character implements Disposable {
    private OrthographicCamera camera;
    private CollisionRectangle collisionRectangle;
    private float playerRadius;

    private Sound stepSound;
    private long stepSoundId = -1; // ID for the currently looping sound
    private boolean isWalking = false;
    private Random rand = new Random(); // used to randomize pitch

    public Player(float x, float y, float speed, String texturePath, OrthographicCamera camera) {
        super(x, y, speed, texturePath);
        this.camera = camera;
        this.collisionRectangle = new CollisionRectangle(x, y, texture.getWidth() / 2, texture.getHeight() / 2);
        this.playerRadius = texture.getWidth() / 2f;

        stepSound = Gdx.audio.newSound(Gdx.files.internal("footsteps-on-tile-31653.ogg"));
    }

    public void update(float delta, Array<Enemy> enemies, Array<CollisionRectangle> mapCollisions) {
        float moveAmount = speed * delta;
        float proposedX = x;
        float proposedY = y;

        // Check horizontal movement
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            proposedX = x - moveAmount;
            collisionRectangle.move(proposedX - texture.getWidth() / 4f, y - texture.getHeight() / 4f);
            if (!isCollidingWithMap(collisionRectangle, mapCollisions) && !isCollidingWithEnemies(proposedX, y, enemies)) {
                x = proposedX;
            }
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            proposedX = x + moveAmount;
            collisionRectangle.move(proposedX - texture.getWidth() / 4f, y - texture.getHeight() / 4f);
            if (!isCollidingWithMap(collisionRectangle, mapCollisions) && !isCollidingWithEnemies(proposedX, y, enemies)) {
                x = proposedX;
            }
        }

        // Check vertical movement
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            proposedY = y + moveAmount;
            collisionRectangle.move(x - texture.getWidth() / 4f, proposedY - texture.getHeight() / 4f);
            if (!isCollidingWithMap(collisionRectangle, mapCollisions) && !isCollidingWithEnemies(x, proposedY, enemies)) {
                y = proposedY;
            }
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            proposedY = y - moveAmount;
            collisionRectangle.move(x - texture.getWidth() / 4f, proposedY - texture.getHeight() / 4f);
            if (!isCollidingWithMap(collisionRectangle, mapCollisions) && !isCollidingWithEnemies(x, proposedY, enemies)) {
                y = proposedY;
            }
        }

        // Rotate to face mouse
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.input.getY();
        Vector3 worldCoordinates = camera.unproject(new Vector3(mouseX, mouseY, 0));
        float dx = worldCoordinates.x - x;
        float dy = worldCoordinates.y - y;

        if (dx != 0 || dy != 0) {
            rotation = (float) Math.toDegrees(Math.atan2(dy, dx)) - 90;
        }

        // Update collision rectangle position
        collisionRectangle.move(
            x - (texture.getWidth() / 4f),
            y - (texture.getHeight() / 4f)
        );

        boolean anyKeyPressed = Gdx.input.isKeyPressed(Input.Keys.W) ||
                                Gdx.input.isKeyPressed(Input.Keys.A) ||
                                Gdx.input.isKeyPressed(Input.Keys.S) ||
                                Gdx.input.isKeyPressed(Input.Keys.D);

        if (anyKeyPressed && !isWalking && stepSoundId == -1) {
            float volume = 0.5f;
            float pitch = 0.9f + rand.nextFloat() * 0.2f;
            stepSoundId = stepSound.loop(volume);
            stepSound.setPitch(stepSoundId, pitch);
            isWalking = true;
        } else if (!anyKeyPressed && isWalking) {
            stepSound.stop(stepSoundId);
            stepSoundId = -1;
            isWalking = false;
        }
    }

    @Override
    public void update(float delta) {
        // Fallback for legacy compatibility, no enemy or map awareness
        update(delta, new Array<Enemy>(), new Array<CollisionRectangle>());
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

    public CollisionRectangle getCollisionRectangle() {
        return collisionRectangle;
    }

    private boolean isCollidingWithMap(CollisionRectangle playerRect, Array<CollisionRectangle> mapCollisions) {
        for (CollisionRectangle rect : mapCollisions) {
            if (playerRect.collisionCheck(rect)) {
                return true;
            }
        }
        return false;
    }

    private boolean isCollidingWithEnemies(float proposedX, float proposedY, Array<Enemy> enemies) {
        for (Enemy e : enemies) {
            if (e.isDead()) continue; // Skip dead enemies
            float dx = proposedX - e.getX();
            float dy = proposedY - e.getY();
            float distanceSq = dx * dx + dy * dy;
            float minDist = playerRadius + e.getTexture().getWidth() / 2f;
            if (distanceSq < minDist * minDist) {
                return true;
            }
        }
        return false;
    }

    public float getRadius() {
        return playerRadius;
    }

    @Override
    public void dispose() {
        if (stepSound != null) {
            stepSound.dispose(); // free sound resource(not sure if im supposed to put it here but saw in video)
        }
        super.dispose();
    }
}
