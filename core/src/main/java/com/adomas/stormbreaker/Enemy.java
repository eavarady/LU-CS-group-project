package com.adomas.stormbreaker;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Enemy extends NPC {

    private int health = 100;
    private boolean dead = false;

    public Enemy(float x, float y, float speed, String texturePath) {
        super(x, y, speed, texturePath);
    }

    public float getX() {
        return x;
    }
    
    public float getY() {
        return y;
    }

    @Override
    public void update(float delta) {
        // basic enemies don't move yet, so this is empty for now
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
}