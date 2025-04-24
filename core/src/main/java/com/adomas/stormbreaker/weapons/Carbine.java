package com.adomas.stormbreaker.weapons;

import com.adomas.stormbreaker.Bullet;
import com.adomas.stormbreaker.Character;
import com.badlogic.gdx.math.MathUtils;

public class Carbine extends Weapon {
    
    public Carbine() {
        super(
            "Carbine",       // name
            8f,              // fireRate (shots per second)
            25,              // damage
            2.5f,            // spreadAngle (degrees)
            1.0f,            // reticleExpansionRate
            5.0f,            // reticleContractionRate
            30,              // magazineSize
            2,               // startingMags
            6,               // maxMags
            "carbine_shot.ogg"
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
    protected void playFireSound() {
        if (fireSound != null) {
            fireSound.play(0.2f); // lower carbine volume 
        }
    }
}
