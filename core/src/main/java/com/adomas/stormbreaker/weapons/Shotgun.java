package com.adomas.stormbreaker.weapons;

import com.adomas.stormbreaker.Bullet;
import com.adomas.stormbreaker.Character;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class Shotgun extends Weapon {
    private int pelletCount = 12;
    
    public Shotgun() {
        super(
            "Shotgun",     // name
            2.5f,          // fireRate (shots per second)
            15,            // damage per pellet
            8.0f,         // spreadAngle (degrees)
            0.5f,          // reticleExpansionRate
            1.0f,          // reticleContractionRate
            1000           // magazineSize (set to 1000 for practically unlimited ammo)
        );
    }
    
    @Override
    public Bullet fire(float x, float y, float dirX, float dirY, Character owner) {
        if (!canFire()) {
            return null;
        }
        

        // Reset cooldown and decrease ammo
        timeSinceLastShot = 0f;
        currentAmmo--;

        
        // Create a single bullet for now (center pellet)
        Bullet bullet = new Bullet(x, y, dirX, dirY, owner);
        bullet.setDamage(damage);
        
        return bullet;
    }
    
    public Array<Bullet> fireShotgun(float x, float y, float dirX, float dirY, Character owner) {
        if (!canFire()) {
            return null;
        }
        
        // Reset cooldown and decrease ammo
        timeSinceLastShot = 0f;
        currentAmmo--;
        
        Array<Bullet> pellets = new Array<>();
        
        // Create multiple pellets in a spread pattern
        for (int i = 0; i < pelletCount; i++) {
            // Calculate spread for this pellet
            float pelletSpread = MathUtils.random(-spreadAngle, spreadAngle);
            float radians = pelletSpread * MathUtils.degreesToRadians;
            float spreadX = dirX * (float) Math.cos(radians) - dirY * (float) Math.sin(radians);
            float spreadY = dirX * (float) Math.sin(radians) + dirY * (float) Math.cos(radians);
            
            // Create the pellet
            Bullet pellet = new Bullet(x, y, spreadX, spreadY, owner);
            pellet.setDamage(damage);
            pellets.add(pellet);
        }
        
        return pellets;
    }
}
