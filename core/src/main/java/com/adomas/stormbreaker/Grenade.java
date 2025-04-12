package com.adomas.stormbreaker;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Grenade {
    float x, y;
    float vx, vy;
    float radius = 6f;
    float speed = 300f;
    private float targetX, targetY;
    private float fuseTime;
    private float timeAlive = 0f;

    public Grenade(float x, float y, float tx, float ty, float fuseTime) {
        float dx = tx - x;
        float dy = ty - y;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        this.vx = (dx / length) * speed;
        this.vy = (dy / length) * speed;
        this.x = x;
        this.y = y;
        this.targetX = tx;
        this.targetY = ty;
        this.fuseTime = fuseTime;
    }

    public void update(float delta) {
        timeAlive += delta;

        if (!hasReachedTarget()) {
            x += vx * delta;
            y += vy * delta;

            if (hasReachedTarget()) {
                vx = 0;
                vy = 0;
            }
        }
        // bouncing logic TBI
    }

    public void render(ShapeRenderer sr) {
        sr.circle(x, y, radius);
    }

    public boolean hasReachedTarget() {
        float dx = targetX - x;
        float dy = targetY - y;
        final float EPSILON = 3.5f;
        return dx * dx + dy * dy < EPSILON * EPSILON;
    }
    
    public boolean hasExploded() {
        return fuseTime <= 0;
    }
    
    public boolean isExpired() {
        return timeAlive >= fuseTime;
    }
}