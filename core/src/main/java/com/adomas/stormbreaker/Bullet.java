package com.adomas.stormbreaker;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Bullet {
    float x, y;
    float vx, vy;
    float speed = 10000f;
    float radius = 1.0f;

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

    public void update(float delta, Array<Enemy> enemies) {
        float startX = x;
        float startY = y;

        // Update bullet position
        x += vx * speed * delta;
        y += vy * speed * delta;

        // Check for collisions along the path
        for (Enemy enemy : enemies) {
            if (enemy.isDead()) continue;

            // Check if the bullet's path intersects the enemy's hitbox
            if (intersectsLine(startX, startY, x, y, enemy)) {
                enemy.takeDamage(25);
                // Mark bullet for removal or stop its movement
                // This can be handled in the calling class (e.g., TestLevelScreen)
                break;
            }
        }
    }

    public void render(ShapeRenderer sr) {
        sr.circle(x, y, radius);
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
}