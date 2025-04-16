package com.adomas.stormbreaker;

import com.adomas.stormbreaker.tools.CollisionRectangle;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

public class Enemy extends NPC {

    private int health = 100;
    private boolean dead = false;
    private float enemyRadius;
    // the radius of the enemy for player detection, 200 degrees in front of the enemy
    // this is used to check if the player is in the enemy's field of view
    private final float visionAngle = 200f;
    // Shot cooldown time
    private final float shotCooldown = 0.15f;
    // Time since last shot
    private float timeSinceLastShot = 0f;
    // Collision rectangle for the enemy
    private final CollisionRectangle collisionRectangle;
    // Vision distance for the enemy
    private final float visionDistance = 500f;

    public Enemy(float x, float y, float speed, String texturePath) {
        super(x, y, speed, texturePath);
        this.enemyRadius = texture.getWidth() / 2f;
        this.collisionRectangle = new CollisionRectangle(x, y, texture.getWidth() / 2, texture.getHeight() / 2);
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

        //TO DO:
        // Spawn bullet/s towards player in MainGameplayScreen

        // Move the enemy towards the player if enemy is aggressive type

        // Maybe defensive type that moves away from player/camps
        // Chance of defensive type rushing towards player if player is too close
        // or if loses sight of player

        //Patrolling behavior for agressive type

        // Sound detection for enemy, they turn towards the sound
        // and move towards it if they are aggressive type

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
}