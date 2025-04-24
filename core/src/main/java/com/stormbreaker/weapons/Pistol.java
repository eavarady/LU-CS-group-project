package com.stormbreaker.weapons;

import com.badlogic.gdx.math.MathUtils;
import com.stormbreaker.Bullet;
import com.stormbreaker.Character;

public class Pistol extends Weapon {
    
    public Pistol() {
        super(
            "Pistol",      // name
            8f,          // fireRate (shots per second)
            20,            // damage
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
