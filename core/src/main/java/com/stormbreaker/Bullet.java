package com.stormbreaker;

import com.stormbreaker.tools.CollisionRectangle;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.math.MathUtils;

public class Bullet {
    float x, y;
    float vx, vy;
    float speed = 3000f;
    float radius = 2.0f;
    private int damage = 50; 
    private boolean stopped = false;
    private final Character owner; 

    // bullet texture
    private static Texture bulletTexture;
    private static boolean textureLoaded = false;

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

    public void update(float delta, Array<Enemy> enemies, Array<CollisionRectangle> obstacles, Player player) {
        if (stopped) return; 

        float stepSize = 5f; // max distance the bullet can travel in one sub-step
        float distance = speed * delta; // Total distance the bullet will travel this frame
        int steps = Math.max(1, (int) (distance / stepSize)); // Number of sub-steps
        float stepDelta = delta / steps; // Time per sub-step

        for (int i = 0; i < steps; i++) {
            float startX = x;
            float startY = y;

            // new position for this sub-step
            float endX = x + vx * speed * stepDelta;
            float endY = y + vy * speed * stepDelta;

            // Check for collisions with enemies along the path
            for (Enemy enemy : enemies) {
                // Skip if the enemy is dead or if the bullet belongs to the enemy
                if (enemy.isDead()) continue;
                if (this.getOwner() == enemy) continue;

                if (intersectsLine(startX, startY, endX, endY, enemy)) {
                    enemy.takeDamage(damage); // Now uses probabilistic model
                    // Make enemy turn towards the player if hit
                    if (owner instanceof Player playerOwner) {
                        enemy.alertAndTurnTo(playerOwner.getX(), playerOwner.getY());
                    }
                    stopped = true; // Stop the bullet after hitting an enemy
                    return;
                }
            }

            // Check for collision with player (if bullet is not from player)
            if (player != null && this.getOwner() != player) {
                float playerX = player.getX();
                float playerY = player.getY();
                float playerRadius = player.getCollisionRectangle().getWidth() / 2f;
                if (Intersector.intersectSegmentCircle(
                        new Vector2(startX, startY),
                        new Vector2(endX, endY),
                        new Vector2(playerX, playerY),
                        playerRadius * playerRadius)) {
                    player.takeDamage(damage);
                    stopped = true;
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

    // original circle drawing (not needed but left it since it dont break game)
    public void render(ShapeRenderer sr) {
        if (!stopped) {
            sr.circle(x, y, radius);
        }
    }

    // drawing bullet texture
    public void render(SpriteBatch sb) {
        if (!stopped) {
            if (!textureLoaded) {
                bulletTexture = new Texture("bullet1.png");
                textureLoaded = true;
            }

            float drawWidth = 16f;  
            float drawHeight = 5f;

            float angle = (float) Math.atan2(vy, vx) * MathUtils.radiansToDegrees;

            sb.draw(
                bulletTexture,
                x - drawWidth / 2f, y - drawHeight / 2f,
                drawWidth / 2f, drawHeight / 2f,
                drawWidth, drawHeight,
                1f, 1f,
                angle,
                0, 0,
                bulletTexture.getWidth(), bulletTexture.getHeight(),
                false, false
            );
        }
    }

    public boolean isOffScreen(float worldWidth, float worldHeight) {
        return x < 0 || x > worldWidth || y < 0 || y > worldHeight;
    }

    // Helper method to check if a line intersects an enemy's hitbox
    private boolean intersectsLine(float startX, float startY, float endX, float endY, Enemy enemy) {
        float enemyX = enemy.getX();
        float enemyY = enemy.getY();
        float enemyRadius = enemy.getRadius();

        // Check if the line segment intersects the circle 
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
