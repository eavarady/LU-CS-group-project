package com.adomas.stormbreaker.weapons;

import com.adomas.stormbreaker.Bullet;
import com.adomas.stormbreaker.Character;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

public abstract class Weapon {
    protected String name;
    protected float fireRate;  // shots per second
    protected float shotCooldown;  // cooldown between shots in seconds
    protected int damage;
    protected float spreadAngle;  // in degrees
    protected float reticleExpansionRate;
    protected float reticleContractionRate;
    protected int magazineSize;
    protected int currentAmmo;
    protected float timeSinceLastShot;
    protected Sound fireSound;
    
    public Weapon(String name, float fireRate, int damage, float spreadAngle, 
                 float reticleExpansionRate, float reticleContractionRate, int magazineSize, String soundPath) {
        this.name = name;
        this.fireRate = fireRate;
        this.shotCooldown = 1.0f / fireRate;
        this.damage = damage;
        this.spreadAngle = spreadAngle;
        this.reticleExpansionRate = reticleExpansionRate;
        this.reticleContractionRate = reticleContractionRate;
        this.magazineSize = magazineSize;
        this.currentAmmo = magazineSize;
        this.timeSinceLastShot = shotCooldown; // Ready to fire initially
        this.fireSound = Gdx.audio.newSound(Gdx.files.internal(soundPath));
    }
    
    public void update(float delta) {
        timeSinceLastShot += delta;
    }
    
    /**
     * Attempts to fire the weapon
     * @return The created bullet if fired, null otherwise
     */
    public abstract Bullet fire(float x, float y, float dirX, float dirY, Character owner);
    
    /**
     * Attempts to fire the weapon with spread multiplier
     * @return The created bullet if fired, null otherwise
     */
    public abstract Bullet fire(float x, float y, float dirX, float dirY, Character owner, float spreadMultiplier);
    
    public void reload() {
        currentAmmo = magazineSize;
    }
    
    public boolean canFire() {
        return currentAmmo > 0 && timeSinceLastShot >= shotCooldown;
    }
    
    public String getName() {
        return name;
    }
    
    public int getDamage() {
        return damage;
    }
    
    public float getSpreadAngle() {
        return spreadAngle;
    }
    
    public float getReticleExpansionRate() {
        return reticleExpansionRate;
    }
    
    public float getReticleContractionRate() {
        return reticleContractionRate;
    }
    
    public int getMagazineSize() {
        return magazineSize;
    }
    
    public int getCurrentAmmo() {
        return currentAmmo;
    }
    
    public float getFireRate() {
        return fireRate;
    }

    protected void playFireSound() {
        if (fireSound != null) {
            fireSound.play(0.6f);
        }
    }

    public void dispose() {
        if (fireSound != null) {
            fireSound.dispose();
        }
    }

}
