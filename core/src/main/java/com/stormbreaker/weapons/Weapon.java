package com.stormbreaker.weapons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.stormbreaker.Bullet;
import com.stormbreaker.Character;

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
    protected boolean hasRoundInChamber = false; // track if there is a round in the chamber
    
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
        this.timeSinceLastShot = shotCooldown; 
        this.fireSound = Gdx.audio.newSound(Gdx.files.internal(soundPath));
    }
    
    public void update(float delta) {
        timeSinceLastShot += delta;
    }
    
    public Bullet fire(float x, float y, float dirX, float dirY, Character owner) {
        // Default implementation calls the more complex version with a spread multiplier of 1.0f
        return fire(x, y, dirX, dirY, owner, 1.0f);
    }
    
    public abstract Bullet fire(float x, float y, float dirX, float dirY, Character owner, float spreadMultiplier);

    // return true if reload was successful, false if no magazines available or already at maximum capacity
    public boolean reload() {
        // Can't reload if no magazines left or already at maximum capacity
        if (totalMags <= 0 || (currentAmmo == magazineSize && hasRoundInChamber)) {
            return false;
        }
        
        // tactical reload
        boolean tacticalReload = currentAmmo > 0;
        
        // when doing a tactical reload, keep the round in the chamber
        hasRoundInChamber = tacticalReload;
        
        totalMags--;
        currentAmmo = magazineSize;
        
        return true;
    }
    
    public int addMagazines(int amount) {
        int magsToAdd = Math.min(amount, maxMagsCapacity - totalMags);
        totalMags += magsToAdd;
        return magsToAdd;
    }
    
    public boolean canFire() {
        // can fire if there is ammo in the magazine or a round in the chamber
        return (currentAmmo > 0 || hasRoundInChamber) && timeSinceLastShot >= shotCooldown;
    }
    

     //checks if weapon can be reloaded (has magazines available and not at maximum capacity)
    public boolean canReload() {
        return totalMags > 0 && !(currentAmmo == magazineSize && hasRoundInChamber);
    }
    
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
