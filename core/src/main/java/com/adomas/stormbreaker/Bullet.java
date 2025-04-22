package com.adomas.stormbreaker;

import com.adomas.stormbreaker.tools.CollisionRectangle;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Bullet {
    float x, y;
    float vx, vy;
    float speed = 2789f;
    float radius = 2.0f;
    private int damage = 50; // Default damage
    private boolean stopped = false; // Flag to indicate if the bullet has stopped
    private final Character owner; // or use a specific type if you want

    public Bullet(float x, float y, float vx, float vy, Character owner) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.owner = owner;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public Character getOwner() {
        return owner;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }
    
    public int getDamage() {
        return damage;
    }

    public void update(float delta, Array<Enemy> enemies, Array<CollisionRectangle> obstacles) {
        if (stopped) return; // If the bullet is stopped, do nothing

        float stepSize = 5f; // Maximum distance the bullet can travel in one sub-step
        float distance = speed * delta; // Total distance the bullet will travel this frame
        int steps = Math.max(1, (int) (distance / stepSize)); // Number of sub-steps
        float stepDelta = delta / steps; // Time per sub-step

        for (int i = 0; i < steps; i++) {
            float startX = x;
            float startY = y;

            // Calculate the new position for this sub-step
            float endX = x + vx * speed * stepDelta;
            float endY = y + vy * speed * stepDelta;

            // Check for collisions with enemies along the path
            for (Enemy enemy : enemies) {
                // Skip if the enemy is dead or if the bullet belongs to the enemy
                if (enemy.isDead()) continue;
                if (this.getOwner() == enemy) continue;

                if (intersectsLine(startX, startY, endX, endY, enemy)) {
                    enemy.takeDamage(damage); // Use the bullet's damage property
                    // Make enemy turn towards the player if hit
                    if (owner instanceof Player player) {
                        enemy.alertAndTurnTo(player.getX(), player.getY());
                    }
                    stopped = true; // Stop the bullet after hitting an enemy
                    return;
                }
            }

            // Check for collisions with obstacles along the path
            for (CollisionRectangle obstacle : obstacles) {
                if (intersectsRectangle(startX, startY, endX, endY, obstacle)) {
                    stopped = true; // Stop the bullet after hitting an obstacle
                    return;
                }
            }

            // Update the bullet's position for this sub-step
            x = endX;
            y = endY;
        }
    }

    public void render(ShapeRenderer sr) {
        if (!stopped) {
            sr.circle(x, y, radius);
        }
    }

    public boolean isOffScreen(float worldWidth, float worldHeight) {
        return x < 0 || x > worldWidth || y < 0 || y > worldHeight;
    }

    // Helper method to check if a line intersects an enemy's hitbox
    private boolean intersectsLine(float startX, float startY, float endX, float endY, Enemy enemy) {
        float enemyX = enemy.getX();
        float enemyY = enemy.getY();
        float enemyRadius = enemy.getRadius(); // Use the enemy's hitbox radius

        // Check if the line segment intersects the circle (enemy hitbox)
        return Intersector.intersectSegmentCircle(
            new Vector2(startX, startY),
            new Vector2(endX, endY),
            new Vector2(enemyX, enemyY),
            enemyRadius * enemyRadius
        );
    }

    // Helper method to check if a line intersects a rectangle
    private boolean intersectsRectangle(float startX, float startY, float endX, float endY, CollisionRectangle rect) {
        Rectangle rectangle = new Rectangle(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
        return Intersector.intersectSegmentRectangle(
            new Vector2(startX, startY),
            new Vector2(endX, endY),
            rectangle
        );
    }
}