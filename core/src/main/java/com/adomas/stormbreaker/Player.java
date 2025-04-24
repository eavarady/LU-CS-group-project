package com.adomas.stormbreaker;

import com.adomas.stormbreaker.weapons.Carbine;
import com.adomas.stormbreaker.weapons.Pistol;
import com.adomas.stormbreaker.weapons.Shotgun;
import com.adomas.stormbreaker.weapons.Weapon;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.adomas.stormbreaker.tools.CollisionRectangle;
import com.adomas.stormbreaker.DamageModel;
import java.util.Random;
import java.util.Map;                         
import java.util.HashMap; 

public class Player extends Character implements Disposable {
    private OrthographicCamera camera;
    private CollisionRectangle collisionRectangle;
    private float playerRadius;

    private float health = 100f;
    // Replace string weapon with actual Weapon class
    private Weapon currentWeapon;
    private Array<Weapon> weapons = new Array<>();

    private Sound stepSound;
    private long stepSoundId = -1; // ID for the currently looping sound
    private boolean isWalking = false;
    private Random rand = new Random(); // used to randomize pitch
    
    private Map<String, Texture> weaponTextures = new HashMap<>();
    
    private boolean isReloading = false;
    private float reloadTimer = 0f;
    private float reloadTime = 1.5f; // Time in seconds to complete a reload

    private Sound switchWeaponSound;
    private long reloadSoundId = -1;
    private long healSoundId = -1;

    private boolean isBleeding = false;
    private float bleedTimer = 0f;
    private static final float BLEED_DAMAGE_PER_SECOND = 1f;
    private float stopBleedTimer = 0f;
    private static final float STOP_BLEED_HOLD_TIME = 5f;
    private static final float BLEED_HEAL_AMOUNT = 20f;

    public Player(float x, float y, float speed, String texturePath, OrthographicCamera camera) {
        super(x, y, speed, texturePath);
        this.camera = camera;
        this.collisionRectangle = new CollisionRectangle(x - (texture.getWidth() / 4f), y - (texture.getHeight() / 4f), texture.getWidth() / 2, texture.getHeight() / 2);
        this.playerRadius = texture.getWidth() / 2f;

        stepSound = Gdx.audio.newSound(Gdx.files.internal("footsteps-on-tile-31653.ogg"));
        switchWeaponSound = Gdx.audio.newSound(Gdx.files.internal("weapon_switch.wav"));
        
        // Initialize weapons
        weapons.add(new Pistol());
        weapons.add(new Carbine());
        weapons.add(new Shotgun());
        
        // Set default weapon (Carbine)
        currentWeapon = weapons.get(1);
        weaponTextures.put("Pistol", new Texture(Gdx.files.internal("player_sprite_pistol.png")));
        weaponTextures.put("Carbine", new Texture(Gdx.files.internal("player_sprite_carbine.png")));
        weaponTextures.put("Shotgun", new Texture(Gdx.files.internal("player_sprite_shotgun.png")));
        this.texture = weaponTextures.get(currentWeapon.getName());
    }

    public void update(float delta, Array<Enemy> enemies, Array<CollisionRectangle> mapCollisions) {
        float moveAmount = speed * delta;
        float proposedX;
        float proposedY;

        // Check horizontal movement
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            proposedX = x - moveAmount;
            collisionRectangle.move(proposedX - texture.getWidth() / 4f, y - texture.getHeight() / 4f);
            if (!isCollidingWithMap(collisionRectangle, mapCollisions) && !isCollidingWithEnemies(proposedX, y, enemies)) {
                x = proposedX;
            }
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            proposedX = x + moveAmount;
            collisionRectangle.move(proposedX - texture.getWidth() / 4f, y - texture.getHeight() / 4f);
            if (!isCollidingWithMap(collisionRectangle, mapCollisions) && !isCollidingWithEnemies(proposedX, y, enemies)) {
                x = proposedX;
            }
        }

        // Check vertical movement
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            proposedY = y + moveAmount;
            collisionRectangle.move(x - texture.getWidth() / 4f, proposedY - texture.getHeight() / 4f);
            if (!isCollidingWithMap(collisionRectangle, mapCollisions) && !isCollidingWithEnemies(x, proposedY, enemies)) {
                y = proposedY;
            }
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            proposedY = y - moveAmount;
            collisionRectangle.move(x - texture.getWidth() / 4f, proposedY - texture.getHeight() / 4f);
            if (!isCollidingWithMap(collisionRectangle, mapCollisions) && !isCollidingWithEnemies(x, proposedY, enemies)) {
                y = proposedY;
            }
        }

        // Rotate to face mouse
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.input.getY();
        Vector3 worldCoordinates = camera.unproject(new Vector3(mouseX, mouseY, 0));
        float dx = worldCoordinates.x - x;
        float dy = worldCoordinates.y - y;

        if (dx != 0 || dy != 0) {
            rotation = (float) Math.toDegrees(Math.atan2(dy, dx)) - 90;
        }

        // Update collision rectangle position
        collisionRectangle.move(
            x - (texture.getWidth() / 4f),
            y - (texture.getHeight() / 4f)
        );

        boolean anyKeyPressed = Gdx.input.isKeyPressed(Input.Keys.W) ||
                                Gdx.input.isKeyPressed(Input.Keys.A) ||
                                Gdx.input.isKeyPressed(Input.Keys.S) ||
                                Gdx.input.isKeyPressed(Input.Keys.D);

        if (anyKeyPressed && !isWalking && stepSoundId == -1) {
            float volume = 0.5f;
            float pitch = 0.9f + rand.nextFloat() * 0.2f;
            stepSoundId = stepSound.loop(volume);
            stepSound.setPitch(stepSoundId, pitch);
            isWalking = true;
        } else if (!anyKeyPressed && isWalking) {
            stepSound.stop(stepSoundId);
            stepSoundId = -1;
            isWalking = false;
        }
        
        // Update weapon cooldowns
        if (currentWeapon != null) {
            currentWeapon.update(delta);
        }
        
        // Check for weapon switching
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            switchToPreviousWeapon();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            switchToNextWeapon();
        }
        
        // Handle reload input
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            startReload();
        }

        // Process reloading
        if (isReloading) {
            if (reloadSoundId == -1) {
                reloadSoundId = switchWeaponSound.loop(0.7f); // lower volume for loop
            }
            reloadTimer += delta;
            if (reloadTimer >= reloadTime) {
                // Reload complete
                finishReload();
                if (reloadSoundId != -1) {
                    switchWeaponSound.stop(reloadSoundId);
                    reloadSoundId = -1;
                }
            }
        } else if (reloadSoundId != -1) {
            switchWeaponSound.stop(reloadSoundId);
            reloadSoundId = -1;
        }

        // Bleeding mechanic
        if (isBleeding) {
            bleedTimer += delta;
            if (bleedTimer >= 1f) {
                health -= BLEED_DAMAGE_PER_SECOND;
                bleedTimer = 0f;
                if (health < 0) health = 0;
            }
            // Handle stopping bleed with F key
            if (Gdx.input.isKeyPressed(Input.Keys.F)) {
                if (healSoundId == -1) {
                    healSoundId = switchWeaponSound.loop(0.7f);
                }
                stopBleedTimer += delta;
                if (stopBleedTimer >= STOP_BLEED_HOLD_TIME) {
                    isBleeding = false;
                    stopBleedTimer = 0f;
                    health = Math.min(health + BLEED_HEAL_AMOUNT, 100f);
                    if (healSoundId != -1) {
                        switchWeaponSound.stop(healSoundId);
                        healSoundId = -1;
                    }
                }
            } else {
                stopBleedTimer = 0f;
                if (healSoundId != -1) {
                    switchWeaponSound.stop(healSoundId);
                    healSoundId = -1;
                }
            }
        } else if (healSoundId != -1) {
            switchWeaponSound.stop(healSoundId);
            healSoundId = -1;
        }
    }

    @Override
    public void update(float delta) {
        // Fallback for legacy compatibility, no enemy or map awareness
        update(delta, new Array<Enemy>(), new Array<CollisionRectangle>());
    }

    public void clampPosition(float worldWidth, float worldHeight, float screenWidth, float screenHeight) {
        // Compute conversion factors based on the viewport's effective screen dimensions
        float conversionFactorX = camera.viewportWidth / screenWidth;
        float conversionFactorY = camera.viewportHeight / screenHeight;

        // Calculate half the texture size in world units
        float halfWidth = (texture.getWidth() / 2f) * conversionFactorX;
        float halfHeight = (texture.getHeight() / 2f) * conversionFactorY;

        // Clamp the player's position within the world dimensions
        x = Math.max(halfWidth, Math.min(x, worldWidth - halfWidth));
        y = Math.max(halfHeight, Math.min(y, worldHeight - halfHeight));
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public CollisionRectangle getCollisionRectangle() {
        return collisionRectangle;
    }

    private boolean isCollidingWithMap(CollisionRectangle playerRect, Array<CollisionRectangle> mapCollisions) {
        for (CollisionRectangle rect : mapCollisions) {
            if (playerRect.collisionCheck(rect)) {
                return true;
            }
        }
        return false;
    }

    private boolean isCollidingWithEnemies(float proposedX, float proposedY, Array<Enemy> enemies) {

        for (Enemy e : enemies) {
            if (e.isDead()) continue; // Skip dead enemies
            CollisionRectangle enemyRect = e.getCollisionRectangle();
            if (collisionRectangle.collisionCheck(enemyRect)) {
                return true;
            }
        }
        return false;
    }

    public float getHealth() {
        return health;
    }
    
    public Weapon getCurrentWeapon() {
        return currentWeapon;
    }
    
    // For backward compatibility with HUD
    public String getCurrentWeaponName() {
        if (currentWeapon != null) {
            return currentWeapon.getName();
        }
        return "None";
    }
    
    // Add method to fire the current weapon
    public Bullet fireWeapon(float x, float y, float dirX, float dirY) {
        if (isReloading) {
            return null; // Can't fire while reloading
        }
        
        if (currentWeapon != null) {
            return currentWeapon.fire(x, y, dirX, dirY, this);
        }
        return null;
    }
    
    // Add method to fire the current weapon with spread multiplier
    public Bullet fireWeapon(float x, float y, float dirX, float dirY, float spreadMultiplier) {
        if (isReloading) {
            return null; // Can't fire while reloading
        }
        
        if (currentWeapon != null) {
            return currentWeapon.fire(x, y, dirX, dirY, this, spreadMultiplier);
        }
        return null;
    }
    
    // Special method for shotgun
    public Array<Bullet> fireShotgun(float x, float y, float dirX, float dirY) {
        if (isReloading) {
            return null; // Can't fire while reloading
        }
        
        if (currentWeapon != null && currentWeapon instanceof Shotgun) {
            return ((Shotgun) currentWeapon).fireShotgun(x, y, dirX, dirY, this);
        }
        return null;
    }

    // Special method for shotgun with spread multiplier
    public Array<Bullet> fireShotgun(float x, float y, float dirX, float dirY, float spreadMultiplier) {
        if (isReloading) {
            return null; // Can't fire while reloading
        }
        
        if (currentWeapon != null && currentWeapon instanceof Shotgun) {
            return ((Shotgun) currentWeapon).fireShotgun(x, y, dirX, dirY, this, spreadMultiplier);
        }
        return null;
    }

    public void switchToNextWeapon() {
        int currentIndex = weapons.indexOf(currentWeapon, true);
        int nextIndex = (currentIndex + 1) % weapons.size;
        currentWeapon = weapons.get(nextIndex);
        this.texture = weaponTextures.get(currentWeapon.getName());
        switchWeaponSound.play(1.0f);
    } 
    
    public void switchToPreviousWeapon() {
        int currentIndex = weapons.indexOf(currentWeapon, true);
        int prevIndex = (currentIndex - 1 + weapons.size) % weapons.size;
        currentWeapon = weapons.get(prevIndex);
        this.texture = weaponTextures.get(currentWeapon.getName());
        switchWeaponSound.play(1.0f);
    }

    @Override
    public void dispose() {
        if (stepSound != null) {
            stepSound.dispose();
        }
        if (switchWeaponSound != null) {
            switchWeaponSound.dispose();
        }
        for (Texture tex : weaponTextures.values()) { 
            tex.dispose();
        }
        super.dispose();
    }

    /**
     * Starts the reload process for the current weapon
     * @return true if reload started, false if not possible to reload
     */
    public boolean startReload() {
        if (isReloading || currentWeapon == null || !currentWeapon.canReload()) {
            return false;
        }
        
        isReloading = true;
        reloadTimer = 0f;
        return true;
    }
    
    /**
     * Completes the reload process
     */
    private void finishReload() {
        if (currentWeapon != null) {
            currentWeapon.reload();
        }
        isReloading = false;
        reloadTimer = 0f;
        if (reloadSoundId != -1) {
            switchWeaponSound.stop(reloadSoundId);
            reloadSoundId = -1;
        }
    }
    
    /**
     * Checks if player is currently reloading
     */
    public boolean isReloading() {
        return isReloading;
    }
    
    /**
     * Returns the reload progress as a value between 0 and 1
     */
    public float getReloadProgress() {
        if (!isReloading) {
            return 0f;
        }
        return reloadTimer / reloadTime;
    }
    
    /**
     * Gets the current ammo in the weapon
     */
    public int getCurrentAmmo() {
        if (currentWeapon == null) {
            return 0;
        }
        return currentWeapon.getCurrentAmmo();
    }
    
    /**
     * Gets the total ammo count including round in chamber if present
     */
    public int getTotalAmmoCount() {
        if (currentWeapon == null) {
            return 0;
        }
        return currentWeapon.getTotalAmmoCount();
    }
    
    /**
     * Gets the magazine size of the current weapon
     */
    public int getMagazineSize() {
        if (currentWeapon == null) {
            return 0;
        }
        return currentWeapon.getMagazineSize();
    }
    
    /**
     * Gets the total magazines remaining for the current weapon
     */
    public int getTotalMags() {
        if (currentWeapon == null) {
            return 0;
        }
        return currentWeapon.getTotalMags();
    }
    
    /**
     * Checks if the current weapon has a round in the chamber
     */
    public boolean hasRoundInChamber() {
        if (currentWeapon == null) {
            return false;
        }
        return currentWeapon.hasRoundInChamber();
    }

    /**
     * Gets the maximum number of magazines the player can carry for the current weapon
     */
    public int getMaxMags() {
        if (currentWeapon == null) {
            return 0;
        }
        return currentWeapon.getMaxMagsCapacity();
    }

    /**
     * Applies damage to the player using a probabilistic damage model
     * @param baseDamage The base damage value
     */
    public void takeDamage(int baseDamage) {
        DamageModel.HitResult hit = DamageModel.getHitResult();
        System.out.println("BodyPart Hit: " + hit.part);
        int finalDamage = Math.round(baseDamage * hit.multiplier);
        health -= finalDamage;
        // Bleed effect
        if (!isBleeding && Math.random() < hit.bleedChance) {
            isBleeding = true;
            bleedTimer = 0f;
        }
        if (health < 0) health = 0;
    }
}
