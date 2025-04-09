package com.adomas.stormbreaker;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Bullet {
    float x, y;
    float vx, vy;
    float speed = 1700f;
    float radius = 2.6f;

    public Bullet(float x, float y, float vx, float vy) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
    }

    public void update(float delta) {
        x += vx * speed * delta;
        y += vy * speed * delta;
    }

    public void render(ShapeRenderer sr) {
        sr.circle(x, y, radius);
    }

    public boolean isOffScreen(float worldWidth, float worldHeight) {
        return x < 0 || x > worldWidth || y < 0 || y > worldHeight;
    }
}