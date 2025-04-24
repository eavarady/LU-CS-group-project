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
    protected int totalMags;     // Total number of magazines player has
    protected int maxMagsCapacity; // Maximum number of magazines player can carry
    protected float timeSinceLastShot;
    protected Sound fireSound;
    
    public Weapon(String name, float fireRate, int damage, float spreadAngle, 
                 float reticleExpansionRate, float reticleContractionRate, int magazineSize, int startingMags, int maxMags, String soundPath) {
        this.name = name;
        this.fireRate = fireRate;
        this.shotCooldown = 1.0f / fireRate;
        this.damage = damage;
        this.spreadAngle = spreadAngle;
        this.reticleExpansionRate = reticleExpansionRate;
        this.reticleContractionRate = reticleContractionRate;
        this.magazineSize = magazineSize;
        this.currentAmmo = magazineSize;
        this.totalMags = startingMags;
        this.maxMagsCapacity = maxMags;
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
    
    /**
     * Attempts to reload the weapon if magazines are available
     * @return true if reload was successful, false if no magazines available
     */
    public boolean reload() {
        if (totalMags <= 0 || currentAmmo == magazineSize) {
            return false;
        }
        
        totalMags--;
        currentAmmo = magazineSize;
        return true;
    }
    
    /**
     * Add magazines to the weapon
     * @param amount Number of magazines to add
     * @return Actual number of magazines added (may be less if at capacity)
     */
    public int addMagazines(int amount) {
        int magsToAdd = Math.min(amount, maxMagsCapacity - totalMags);
        totalMags += magsToAdd;
        return magsToAdd;
    }
    
    public boolean canFire() {
        return currentAmmo > 0 && timeSinceLastShot >= shotCooldown;
    }
    
    /**
     * Checks if weapon can be reloaded (has magazines available and isn't at full ammo)
     */
    public boolean canReload() {
        return totalMags > 0 && currentAmmo < magazineSize;
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
    
    public int getTotalMags() {
        return totalMags;
    }
    
    public int getMaxMagsCapacity() {
        return maxMagsCapacity;
    }

    protected void playFireSound() {
        if (fireSound != null) {
            fireSound.play(0.5f);
        }
    }

    public void dispose() {
        if (fireSound != null) {
            fireSound.dispose();
        }
    }

}
