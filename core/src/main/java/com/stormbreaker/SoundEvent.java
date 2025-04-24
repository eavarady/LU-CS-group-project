package com.stormbreaker;

import com.badlogic.gdx.math.Vector2;

public class SoundEvent {
    public enum Type {
        GUNSHOT, GRENADE, FOOTSTEP
    }

    private final Vector2 position;
    private float currentRadius;
    private final float maxRadius;
    private final float duration; // seconds
    private float elapsed; // seconds
    private final Type type;
    private boolean expired;

    public SoundEvent(Vector2 position, float maxRadius, float duration, Type type) {
        this.position = new Vector2(position);
        this.currentRadius = 0f;
        this.maxRadius = maxRadius;
        this.duration = duration;
        this.elapsed = 0f;
        this.type = type;
        this.expired = false;
    }

    public void update(float delta) {
        elapsed += delta;
        float progress = Math.min(elapsed / duration, 1f);
        currentRadius = maxRadius * progress;
        if (elapsed >= duration) {
            expired = true;
        }
    }

    public Vector2 getPosition() { return position; }
    public float getCurrentRadius() { return currentRadius; }
    public float getMaxRadius() { return maxRadius; }
    public Type getType() { return type; }
    public boolean isExpired() { return expired; }
}
