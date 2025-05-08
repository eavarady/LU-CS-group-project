package com.stormbreaker.weapons;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.stormbreaker.Bullet;
import com.stormbreaker.Character;

public class Shotgun extends Weapon {
    private final int pelletCount = 8;
    
    public Shotgun() {
        super(
            "Shotgun", // name
            2.5f, // fireRate (shots per second)
            15, // damage per pellet
            8.0f, // spreadAngle (degrees)
            1.0f, // reticleExpansionRate
            5.0f, // reticleContractionRate
            5,// magazineSize
            2,// startingMags
            6, // maxMags
            "shotgun_shot.wav"
        );
    }
    
    @Override
    public Bullet fire(float x, float y, float dirX, float dirY, Character owner, float spreadMultiplier) {
        if (!canFire()) {
            return null;
        }
        
        float pelletSpread = com.badlogic.gdx.math.MathUtils.random(-spreadAngle * spreadMultiplier, spreadAngle * spreadMultiplier);
        float radians = pelletSpread * com.badlogic.gdx.math.MathUtils.degreesToRadians;
        float spreadX = dirX * (float) Math.cos(radians) - dirY * (float) Math.sin(radians);
        float spreadY = dirX * (float) Math.sin(radians) + dirY * (float) Math.cos(radians);
        
        Bullet pellet = new Bullet(x, y, spreadX, spreadY, owner);
        pellet.setDamage(damage);
        
        playFireSound();
        
        // Reset cooldown
        timeSinceLastShot = 0f;
        
        // Use round in chamber first if it exists
        if (hasRoundInChamber) {
            hasRoundInChamber = false;
        } else {
            // Otherwise use from magazine
            currentAmmo--;
        }
        
        return pellet;
    }
    
    public Array<Bullet> fireShotgun(float x, float y, float dirX, float dirY, Character owner) {
        // Call the version with spread multiplier using default value of 1.0f
        return fireShotgun(x, y, dirX, dirY, owner, 1.0f);
    }
    
    public Array<Bullet> fireShotgun(float x, float y, float dirX, float dirY, Character owner, float spreadMultiplier) {
        if (!canFire()) {
            return null;
        }
        
        // Reset cooldown
        timeSinceLastShot = 0f;
        
        // Use round in chamber first if it exists
        if (hasRoundInChamber) {
            hasRoundInChamber = false;
        } else {
            // Otherwise use from magazine
            currentAmmo--;
        }
        
        Array<Bullet> pellets = new Array<>();
        for (int i = 0; i < pelletCount; i++) {
            float pelletSpread = MathUtils.random(-spreadAngle * spreadMultiplier, spreadAngle * spreadMultiplier);
            float radians = pelletSpread * MathUtils.degreesToRadians;
            float spreadX = dirX * (float) Math.cos(radians) - dirY * (float) Math.sin(radians);
            float spreadY = dirX * (float) Math.sin(radians) + dirY * (float) Math.cos(radians);
            Bullet pellet = new Bullet(x, y, spreadX, spreadY, owner);
            pellet.setDamage(damage);
            pellets.add(pellet);
        }
        playFireSound();
        return pellets;
    }
    
    @Override
    protected void playFireSound() {
        if (fireSound != null) {
            fireSound.play(0.45f); // lower volume for shotgun
        }
    }
}
