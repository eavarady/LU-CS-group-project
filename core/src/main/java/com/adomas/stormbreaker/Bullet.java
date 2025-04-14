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
    float speed = 7500f;
    float radius = 2.0f;
    private boolean stopped = false; // Flag to indicate if the bullet has stopped

    public Bullet(float x, float y, float vx, float vy) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void update(float delta, Array<Enemy> enemies, Array<CollisionRectangle> obstacles) {
        if (stopped) return; // If the bullet is stopped, do nothing

        float startX = x;
        float startY = y;

        // Update bullet position
        x += vx * speed * delta;
        y += vy * speed * delta;

        // Check for collisions with enemies
        for (Enemy enemy : enemies) {
            if (enemy.isDead()) continue;

            // Check if the bullet's path intersects the enemy's hitbox
            if (intersectsLine(startX, startY, x, y, enemy)) {
                enemy.takeDamage(50);
                stopped = true; // Stop the bullet after hitting an enemy
                return;
            }
        }

        // Check for collisions with obstacles
        for (CollisionRectangle obstacle : obstacles) {
            if (intersectsRectangle(startX, startY, x, y, obstacle)) {
                stopped = true; // Stop the bullet after hitting an obstacle
                return;
            }
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
        float radius = enemy.getRadius(); // Use the enemy's hitbox radius

        // Check if the line segment intersects the circle (enemy hitbox)
        return Intersector.intersectSegmentCircle(
            new Vector2(startX, startY),
            new Vector2(endX, endY),
            new Vector2(enemyX, enemyY),
            radius * radius
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