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
            1000           // magazineSize (set to 1000 for practically unlimited ammo)
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
        
        // Reset cooldown and decrease ammo
        timeSinceLastShot = 0f;
        currentAmmo--;
        
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
        
        // Reset cooldown and decrease ammo
        timeSinceLastShot = 0f;
        currentAmmo--;
        
        return bullet;
    }
}
