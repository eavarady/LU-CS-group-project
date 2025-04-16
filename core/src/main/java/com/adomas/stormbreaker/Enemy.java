package com.adomas.stormbreaker;

import com.adomas.stormbreaker.tools.CollisionRectangle;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

public class Enemy extends NPC {

    private int health = 100;
    private boolean dead = false;
    private final float enemyRadius;
    // the radius of the enemy for player detection, 200 degrees in front of the enemy
    // this is used to check if the player is in the enemy's field of view
    private final float visionAngle = 100f;
    // Shot cooldown time
    private final float shotCooldown = 0.15f;
    // Time since last shot
    private float timeSinceLastShot = 0f;
    // Collision rectangle for the enemy
    private final CollisionRectangle collisionRectangle;
    // Vision distance for the enemy
    private final float visionDistance = 600f;
    // Bool to check if the enemy wants to shoot
    private boolean wantsToShoot = false;
    // When the enemy wants to shoot
    private float shootDirX = 0f;
    private float shootDirY = 0f;

    public Enemy(float x, float y, float speed, String texturePath) {
        super(x, y, speed, texturePath);
        this.enemyRadius = texture.getWidth() / 2f;
        this.collisionRectangle = new CollisionRectangle(x - (texture.getWidth() / 4f), y - (texture.getHeight() / 4f), texture.getWidth() / 2, texture.getHeight() / 2);
    }

    public float getX() {
        return x;
    }
    
    public float getY() {
        return y;
    }
    //Empty update method for the enemy class to satisfy the abstract method in the NPC class
    @Override
    public void update(float delta) {
    }
    
    // Pass reference to the player and map collisions to the update method
    public void update(float delta, Player player, Array<CollisionRectangle> mapCollisions) {
        if (dead) return;
        // Update the collision rectangle position
        collisionRectangle.move(
            x - (texture.getWidth() / 4f),
            y - (texture.getHeight() / 4f)
        );
        // Calculate vector to player
        float dx = player.getX() - this.x;
        float dy = player.getY() - this.y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        // Check distance
        if (distance > visionDistance) return; // Player is too far

        // Check angle (cone of vision)
        float angleToPlayer = (float) Math.toDegrees(Math.atan2(dy, dx));
        float enemyFacingAngle = this.rotation; // or whatever your enemy's facing angle is
        // Normalize angles to [0, 360)
        angleToPlayer = (angleToPlayer + 360) % 360;
        enemyFacingAngle = (enemyFacingAngle + 360) % 360;
        float angleDifference = Math.abs(angleToPlayer - enemyFacingAngle);
        if (angleDifference > 180) angleDifference = 360 - angleDifference;

        if (angleDifference > visionAngle / 2f) return; // Player is outside vision cone

        // Line-of-sight check (simple step-based raycast)
        int steps = (int) (distance / 10f);
        boolean blocked = false;
        for (int i = 1; i <= steps; i++) {
            float t = i / (float) steps;
            float checkX = this.x + dx * t;
            float checkY = this.y + dy * t;
            for (CollisionRectangle rect : mapCollisions) {
                if (rect.getX() <= checkX && checkX <= rect.getX() + rect.getWidth() &&
                    rect.getY() <= checkY && checkY <= rect.getY() + rect.getHeight()) {
                    blocked = true;
                    break;
                }
            }
            if (blocked) break;
        }
        if (blocked) return; // Player is not visible

        // At this point player is visible
        
        timeSinceLastShot += delta;
        if (timeSinceLastShot >= shotCooldown) {
            // Set intent to shoot and direction
            wantsToShoot = true;
            float norm = (float)Math.sqrt(dx * dx + dy * dy);
            shootDirX = dx / norm;
            shootDirY = dy / norm;
            timeSinceLastShot = 0f;
        } else {
            wantsToShoot = false;
        }
    }

    public void render(SpriteBatch batch) {
        if (!dead) {
            super.render(batch);
        }
    }

    public void takeDamage(int amount) {
        if (!dead) {
            health -= amount;
            if (health <= 0) {
                dead = true;
            }
        }
    }

    public boolean isDead() {
        return dead;
    }
    
    public float getRadius() {
        return enemyRadius;
    }

    public boolean wantsToShoot() {
        return wantsToShoot;
    }
    
    public float getShootDirX() {
        return shootDirX;
    }
    
    public float getShootDirY() {
        return shootDirY;
    }

    //reset shooting direction
    public void setShootDirX(float shootDirX) {
        this.shootDirX = shootDirX;
    }

    public void setShootDirY(float shootDirY) {
        this.shootDirY = shootDirY;
    }

    public void setWantsToShoot(boolean wantsToShoot) {
        this.wantsToShoot = wantsToShoot;
    }

    public CollisionRectangle getCollisionRectangle() {
        return collisionRectangle;
    }

    public float getVisionDistance() {
        return visionDistance;
    }

    public float getVisionAngle() {
        return visionAngle;
    }

    // Get rotation of the enemy
    public float getRotation() {
        return rotation;
    }
}