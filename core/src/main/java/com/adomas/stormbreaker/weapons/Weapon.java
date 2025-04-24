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
    protected boolean hasRoundInChamber = false; // Track if there's a round in the chamber
    
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
        this.currentAmmo = magazineSize; // Start with a full magazine
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
    public Bullet fire(float x, float y, float dirX, float dirY, Character owner) {
        // Default implementation calls the more complex version with a spread multiplier of 1.0f
        return fire(x, y, dirX, dirY, owner, 1.0f);
    }
    
    /**
     * Attempts to fire the weapon with spread multiplier
     * @return The created bullet if fired, null otherwise
     */
    public abstract Bullet fire(float x, float y, float dirX, float dirY, Character owner, float spreadMultiplier);
    
    /**
     * Attempts to reload the weapon
     * @return true if reload was successful, false if no magazines available or already at maximum capacity
     */
    public boolean reload() {
        // Can't reload if:
        // 1. No magazines left
        // 2. Already at maximum capacity (full magazine + round in chamber)
        if (totalMags <= 0 || (currentAmmo == magazineSize && hasRoundInChamber)) {
            return false;
        }
        
        // Tactical reload (there are still rounds in the current magazine)
        boolean tacticalReload = currentAmmo > 0;
        
        // If doing a tactical reload, keep a round in the chamber
        hasRoundInChamber = tacticalReload;
        
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
        // Can fire if there's ammo in the magazine or a round in the chamber
        return (currentAmmo > 0 || hasRoundInChamber) && timeSinceLastShot >= shotCooldown;
    }
    
    /**
     * Checks if weapon can be reloaded (has magazines available and isn't at maximum capacity)
     */
    public boolean canReload() {
        // Can reload if:
        // 1. There are magazines available
        // 2. Not at maximum capacity (full magazine + round in chamber)
        return totalMags > 0 && !(currentAmmo == magazineSize && hasRoundInChamber);
    }
    
    /**
     * Gets the total effective ammo count (magazine + chamber)
     */
    public int getTotalAmmoCount() {
        return currentAmmo + (hasRoundInChamber ? 1 : 0);
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
    
    public boolean hasRoundInChamber() {
        return hasRoundInChamber;
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
