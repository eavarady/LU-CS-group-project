package com.adomas.stormbreaker.weapons;

import com.adomas.stormbreaker.Bullet;
import com.adomas.stormbreaker.Character;
import com.badlogic.gdx.math.MathUtils;

public class Pistol extends Weapon {
    
    public Pistol() {
        super(
            "Pistol",      // name
            8f,          // fireRate (shots per second)
            25,            // damage
            3.0f,          // spreadAngle (degrees)
            1.0f,          // reticleExpansionRate
            5.0f,          // reticleContractionRate
            15,            // magazineSize
            4,             // startingMags
            8,             // maxMags
            "pistol_shot.wav"
        );
    }
    
    @Override
    public Bullet fire(float x, float y, float dirX, float dirY, Character owner) {
        if (!canFire()) {
            return null;
        }
        
        // Apply random spread
        float angle = MathUtils.random(-spreadAngle, spreadAngle);
        float radians = angle * MathUtils.degreesToRadians;
        float spreadX = dirX * (float) Math.cos(radians) - dirY * (float) Math.sin(radians);
        float spreadY = dirX * (float) Math.sin(radians) + dirY * (float) Math.cos(radians);
        
        // Create bullet with the modified direction
        Bullet bullet = new Bullet(x, y, spreadX, spreadY, owner);
        bullet.setDamage(damage);

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
        
        return bullet;
    }

    @Override
    public Bullet fire(float x, float y, float dirX, float dirY, Character owner, float spreadMultiplier) {
        if (!canFire()) {
            return null;
        }
        
        // Apply random spread using spreadMultiplier
        float angle = MathUtils.random(-spreadAngle * spreadMultiplier, spreadAngle * spreadMultiplier);
        float radians = angle * MathUtils.degreesToRadians;
        float spreadX = dirX * (float) Math.cos(radians) - dirY * (float) Math.sin(radians);
        float spreadY = dirX * (float) Math.sin(radians) + dirY * (float) Math.cos(radians);
        
        // Create bullet with the modified direction
        Bullet bullet = new Bullet(x, y, spreadX, spreadY, owner);
        bullet.setDamage(damage);

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
        
        return bullet;
    }
}
