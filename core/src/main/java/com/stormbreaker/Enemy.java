package com.stormbreaker;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.stormbreaker.tools.AStarPathfinder;
import com.stormbreaker.tools.CollisionRectangle;

public class Enemy extends NPC {

    public enum EnemyType {
        AGGRESSIVE,
        PASSIVE,
        BOMBER
    }
    private EnemyType type = EnemyType.AGGRESSIVE;

    private int health = 100;
    private Texture[] deathFrames; // array for death frames textures
    private float deathElapsedTime = 0f;
    private float deathFrameDuration = 0.2f; // seconds per frame
    private Sound deathSound;
    private Sound shootSound; // Add this field for shooting sound
    private boolean dead = false;
    private final float enemyRadius;
    // the radius of the enemy for player detection, 200 degrees in front of the enemy
    // this is used to check if the player is in the enemy's field of view
    private final float visionAngle = 100f;
    // Shot cooldown time
    private final float shotCooldown = 0.15f;
    // Time since last shot
    private float timeSinceLastShot = 0f;
    // Reaction timer for shooting delay
    private float reactionTimer = 0f;
    // Minimum time player must be visible before shooting
    private static final float REACTION_TIME = 0.5f;
    // Collision rectangle for the enemy
    private final CollisionRectangle collisionRectangle;
    // Vision distance for the enemy
    private final float visionDistance = 1000f;
    // Bool to check if the enemy wants to shoot
    private boolean wantsToShoot = false;
    // When the enemy wants to shoot
    private float shootDirX = 0f;
    private float shootDirY = 0f;
    // Rotation speed in degrees per second
    private final float rotationSpeed = 120f;
    private final float fastRotationSpeed = 360f; // Fast turn speed for alert/hit
    private float currentRotationSpeed = rotationSpeed;
    // Target rotation
    private float targetRotation = 0f;
    private Float soundTargetRotation = null;
    // Track last known player position
    private Vector2 lastKnownPlayerPos = null;
    private boolean playerRecentlySeen = false;
    private final float ARRIVAL_THRESHOLD = 8f; // How close is 'arrived' at last known pos
    private AStarPathfinder pathfinder;
    private Array<Vector2> currentPath = null;
    private int pathIndex = 0;
    private float pathRecalcCooldown = 0f;
    private static final float PATH_RECALC_INTERVAL = 0.5f; // seconds

    public enum EnemyState {
        UNAWARE,
        CAUTIOUS,
        ALERTED,
        MOVING
    }

    private EnemyState state = EnemyState.UNAWARE;
    
    //death animation variables 
    private float timeSinceDeath = 0f;
    private boolean disposeAfterDeath = false;

    private boolean checkedAggressiveConversion = false;

    // For dynamic unstuck logic
    private Vector2 lastPosition = null;
    private float stuckTime = 0f;
    private static final float STUCK_THRESHOLD = 0.5f; // seconds
    private static final float MIN_MOVE_DIST = 2f; // minimum distance considered as movement

    private boolean hasExploded = false;
    
 // drop item system
    public enum DropType { PISTOL_AMMO, SHOTGUN_AMMO, CARBINE_AMMO, MEDKIT }

    private DropType dropType = null;
    private Texture dropTexture = null;
    private boolean dropCollected = false;
    
 // drop icons
    private static final Texture pistolDropIcon = new Texture(Gdx.files.internal("pistolammo.png"));
    private static final Texture shotgunDropIcon = new Texture(Gdx.files.internal("shotgunammo.png"));
    private static final Texture carbineDropIcon = new Texture(Gdx.files.internal("carbineammo.png"));
    private static final Texture medkitDropIcon = new Texture(Gdx.files.internal("medkit.png"));



    // Add this interface for grenade explosions
    public interface LevelScreenListener {
        void createGrenadeExplosion(float x, float y);
    }

    private LevelScreenListener levelScreenListener;

    // --- SCANNING FIELDS FOR PASSIVE CAUTIOUS ENEMIES ---
    private float scanTimer = 0f;
    private float scanInterval = 1.5f; // seconds between scans
    private Float scanTargetAngle = null;

    // --- ALERTED STATE TIMER ---
    private float alertedTimer = 0f;
    private static final float MIN_ALERTED_TIME = 1.0f; // seconds

    public Enemy(float x, float y, float speed, String texturePath) {
        super(x, y, speed, texturePath);
        this.enemyRadius = texture.getWidth() / 2f;
        this.collisionRectangle = new CollisionRectangle(x - (texture.getWidth() / 4f), y - (texture.getHeight() / 4f), texture.getWidth() / 2, texture.getHeight() / 2);
     // Load death animation frames
        deathFrames = new Texture[] {
        	
            new Texture(Gdx.files.internal("death11.png")),
            new Texture(Gdx.files.internal("death22.png")),
            new Texture(Gdx.files.internal("death33.png")),
            new Texture(Gdx.files.internal("death44.png"))
            
        };
        
     // randon death frame for enemy death
        Texture[] finalDeathFrames = new Texture[] {
            new Texture(Gdx.files.internal("newdeath1.png")),
            new Texture(Gdx.files.internal("newdeath2.png")),
            new Texture(Gdx.files.internal("newdeath3.png")),
            new Texture(Gdx.files.internal("newdeath4.png")),
            new Texture(Gdx.files.internal("newdeath5.png")),
            new Texture(Gdx.files.internal("death33.png"))
        };

        // replace last death frame with a random one
        int randomIndex = (int)(Math.random() * finalDeathFrames.length);
        deathFrames[deathFrames.length - 1] = finalDeathFrames[randomIndex];


        
     // Load death sound from assets
        deathSound = Gdx.audio.newSound(Gdx.files.internal("deathaudio.ogg"));
        // Load shoot sound from assets (reuse pistol for now)
        shootSound = Gdx.audio.newSound(Gdx.files.internal("carbine_shot.ogg"));

    }

    public Enemy(float x, float y, float speed, String texturePath, EnemyType type) {
        this(x, y, speed, texturePath);
        this.type = type;
    }

    public float getX() {
        return x;
    }
    
    public float getY() {
        return y;
    }
    //Empty update method for the enemy class to satisfy the abstract method in the NPC class
    
    @Override
    public void update(float delta) {
        if (dead) {
            timeSinceDeath += delta;
            // if (timeSinceDeath >= 5f) {
            //     disposeAfterDeath = true;
            // }
        }
    }

    
    // Unified update method: handles both vision and sound detection
    public void update(float delta, Player player, Array<CollisionRectangle> mapCollisions, Array<SoundEvent> soundEvents) {
    	// Animate death and dispose after 5 seconds
    	if (dead && !disposeAfterDeath) {
    	    deathElapsedTime += delta;
    	    // if (deathElapsedTime >= deathFrameDuration * deathFrames.length) {
    	    //     disposeAfterDeath = true;
    	    // }
    	}

        if (dead) return;

        // ALERTED STATE TIMER LOGIC
        if (state == EnemyState.ALERTED && alertedTimer > 0f) {
            alertedTimer -= delta;
            // Always rotate towards targetRotation while timer is active
            smoothRotateTowards(targetRotation, delta);
            // Prevent state from being changed away from ALERTED until timer expires
            // (return early to skip other state logic)
            if (alertedTimer > 0f) return;
        }

        // SOUND DETECTION
        for (SoundEvent se : soundEvents) {
            float dist = Vector2.dst(x, y, se.getPosition().x, se.getPosition().y);
            if (dist <= se.getCurrentRadius()) {
                if (type == EnemyType.PASSIVE) {
                    float dx = se.getPosition().x - x;
                    float dy = se.getPosition().y - y;
                    float angleToSound = (float) Math.toDegrees(Math.atan2(dy, dx));
                    this.soundTargetRotation = angleToSound;
                    if (state == EnemyState.UNAWARE) state = EnemyState.CAUTIOUS;
                    wantsToShoot = true;
                    // Set cautious sound turn speed to rotationSpeed * 2
                    if (state == EnemyState.CAUTIOUS) {
                        this.currentRotationSpeed = rotationSpeed * 2f;
                    }
                    break;
                } else {
                    // AGGRESSIVE (default) logic
                    lastKnownPlayerPos = new Vector2(se.getPosition());
                    playerRecentlySeen = false;
                    state = EnemyState.CAUTIOUS;
                    currentPath = null;
                    pathIndex = 0;
                    currentRotationSpeed = fastRotationSpeed;
                    break;
                }
            }
        }

        if (type == EnemyType.PASSIVE) {
            // --- PASSIVE VISION LOGIC ---
            if (state == EnemyState.CAUTIOUS && soundTargetRotation != null) {
                smoothRotateTowards(soundTargetRotation, delta);
                // Optionally, clear soundTargetRotation if close enough
                float angleDiff = Math.abs(((rotation - soundTargetRotation + 540) % 360) - 180);
                if (angleDiff < 2f) {
                    soundTargetRotation = null;
                }
            }
            // SCANNING LOGIC FOR CAUTIOUS, PASSIVE ENEMIES
            if (state == EnemyState.CAUTIOUS && soundTargetRotation == null) {
                scanTimer += delta;
                if (scanTargetAngle == null || Math.abs(((rotation - scanTargetAngle + 540) % 360) - 180) < 2f) {
                    // If reached target or no target, wait for interval then pick next scan angle
                    if (scanTimer >= scanInterval) {
                        scanTimer = 0f;
                        // Next scan angle: rotate 90 degrees clockwise
                        float nextAngle = (rotation + 90f) % 360f;
                        scanTargetAngle = nextAngle;
                    }
                }
                if (scanTargetAngle != null) {
                    smoothRotateTowards(scanTargetAngle, delta);
                }
            } else {
                // Reset scan if not in CAUTIOUS state
                scanTimer = 0f;
                scanTargetAngle = null;
            }
            float dx = player.getX() - this.x;
            float dy = player.getY() - this.y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            boolean playerVisible = false;
            if (distance <= visionDistance) {
                float angleToPlayer = (float) Math.toDegrees(Math.atan2(dy, dx));
                float enemyFacingAngle = rotation;
                angleToPlayer = (angleToPlayer + 360) % 360;
                enemyFacingAngle = (enemyFacingAngle + 360) % 360;
                float angleDifference = Math.abs(angleToPlayer - enemyFacingAngle);
                if (angleDifference > 180) angleDifference = 360 - angleDifference;
                if (angleDifference <= visionAngle / 2f) {
                    // Line-of-sight check
                    int steps = (int) (distance / 10f);
                    boolean blocked = false;
                    for (int i = 1; i <= steps; i++) {
                        float t = i / (float) steps;
                        float checkX = this.x + dx * t;
                        float checkY = this.y + dy * t;
                        for (CollisionRectangle rect : mapCollisions) {
                            if (rect.getX() <= checkX && checkX <= rect.getX() + rect.getWidth() &&
                                rect.getY() <= checkY && checkY <= rect.getY() + rect.getHeight()) {
                                blocked = true;
                                break;
                            }
                        }
                        if (blocked) break;
                    }
                    if (!blocked) {
                        playerVisible = true;
                    }
                }
            }
            if (playerVisible) {
                if (state != EnemyState.ALERTED) {
                    // Only check conversion on transition to ALERTED
                    if (type == EnemyType.PASSIVE && !checkedAggressiveConversion) {
                        if (Math.random() < 0.2) {
                            type = EnemyType.AGGRESSIVE;
                        }
                        checkedAggressiveConversion = true;
                    }
                }
                state = EnemyState.ALERTED;
                // Turn and shoot at player
                float angleToPlayer = (float) Math.toDegrees(Math.atan2(dy, dx));
                this.targetRotation = angleToPlayer;
                // Set fast turn speed when player enters cone of vision
                this.currentRotationSpeed = fastRotationSpeed;
                smoothRotateTowards(targetRotation, delta);
                // Reaction timer logic
                reactionTimer += delta;
                if (reactionTimer >= REACTION_TIME) {
                    timeSinceLastShot += delta;
                    if (timeSinceLastShot >= shotCooldown) {
                        wantsToShoot = true;
                        float normShoot = (float)Math.sqrt(dx * dx + dy * dy);
                        shootDirX = dx / normShoot;
                        shootDirY = dy / normShoot;
                        timeSinceLastShot = 0f;
                    } else {
                        wantsToShoot = false;
                    }
                } else {
                    wantsToShoot = false;
                }
            } else {
                // Reset reaction timer if player not visible
                reactionTimer = 0f;
                if (state == EnemyState.ALERTED) {
                    state = EnemyState.CAUTIOUS;
                    checkedAggressiveConversion = false; // Reset when losing sight
                }
                wantsToShoot = false;
            }
            return;
        } else if (type == EnemyType.AGGRESSIVE) {
            // --- AGGRESSIVE CAUTIOUS SCANNING LOGIC ---
            if (state == EnemyState.CAUTIOUS && soundTargetRotation != null) {
                smoothRotateTowards(soundTargetRotation, delta);
                float angleDiff = Math.abs(((rotation - soundTargetRotation + 540) % 360) - 180);
                if (angleDiff < 2f) {
                    soundTargetRotation = null;
                }
            }
            if (state == EnemyState.CAUTIOUS && soundTargetRotation == null) {
                scanTimer += delta;
                if (scanTargetAngle == null || Math.abs(((rotation - scanTargetAngle + 540) % 360) - 180) < 2f) {
                    if (scanTimer >= scanInterval) {
                        scanTimer = 0f;
                        float nextAngle = (rotation + 90f) % 360f;
                        scanTargetAngle = nextAngle;
                    }
                }
                if (scanTargetAngle != null) {
                    smoothRotateTowards(scanTargetAngle, delta);
                }
            } else {
                scanTimer = 0f;
                scanTargetAngle = null;
            }
        }

        // BOMBER logic: chase like AGGRESSIVE, but no shooting, explodes on proximity
        if (type == EnemyType.BOMBER) {
            // Increase speed by 50%
            float bomberSpeed = speed * 1.5f;
            // Calculate vector to player
            float dx = player.getX() - this.x;
            float dy = player.getY() - this.y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            boolean playerVisible = false;
            if (distance <= visionDistance) {
                float angleToPlayer = (float) Math.toDegrees(Math.atan2(dy, dx));
                float enemyFacingAngle = rotation;
                angleToPlayer = (angleToPlayer + 360) % 360;
                enemyFacingAngle = (enemyFacingAngle + 360) % 360;
                float angleDifference = Math.abs(angleToPlayer - enemyFacingAngle);
                if (angleDifference > 180) angleDifference = 360 - angleDifference;
                if (angleDifference <= visionAngle / 2f) {
                    // Line-of-sight check (step-based raycast)
                    int steps = (int) (distance / 10f);
                    boolean blocked = false;
                    for (int i = 1; i <= steps; i++) {
                        float t = i / (float) steps;
                        float checkX = this.x + dx * t;
                        float checkY = this.y + dy * t;
                        for (CollisionRectangle rect : mapCollisions) {
                            if (rect.getX() <= checkX && checkX <= rect.getX() + rect.getWidth() &&
                                rect.getY() <= checkY && checkY <= rect.getY() + rect.getHeight()) {
                                blocked = true;
                                break;
                            }
                        }
                        if (blocked) break;
                    }
                    if (!blocked) {
                        playerVisible = true;
                    }
                }
            }
            if (playerVisible) {
                lastKnownPlayerPos = new Vector2(player.getX(), player.getY());
                currentPath = null;
                pathIndex = 0;
                // Move directly toward player
                float moveSpeed = bomberSpeed * delta;
                float norm = (float) Math.sqrt(dx * dx + dy * dy);
                if (norm > 1e-3) {
                    float moveX = (dx / norm) * moveSpeed;
                    float moveY = (dy / norm) * moveSpeed;
                    collisionRectangle.move(x + moveX - (texture.getWidth() / 4f), y + moveY - (texture.getHeight() / 4f));
                    boolean collides = false;
                    for (CollisionRectangle rect : mapCollisions) {
                        if (collisionRectangle.collisionCheck(rect)) {
                            collides = true;
                            break;
                        }
                    }
                    if (!collides) {
                        x += moveX;
                        y += moveY;
                        state = EnemyState.MOVING;
                    } else {
                        collisionRectangle.move(x - (texture.getWidth() / 4f), y - (texture.getHeight() / 4f));
                    }
                }
                // Set target rotation to match the angle to player (in degrees)
                targetRotation = (float) Math.toDegrees(Math.atan2(dy, dx));
                smoothRotateTowards(targetRotation, delta);
                // If close enough, explode
                if (!hasExploded && distance < 40f) {
                    hasExploded = true;
                    dead = true;
                    if (deathSound != null) deathSound.play();
                    // Use grenade explosion logic
                    if (levelScreenListener != null) {
                        levelScreenListener.createGrenadeExplosion(x, y);
                    }
                    return;
                }
            } else if (lastKnownPlayerPos != null) {
                // Pathfind to last known position
                pathRecalcCooldown -= delta;
                if (pathfinder != null && (currentPath == null || pathIndex >= currentPath.size || pathRecalcCooldown <= 0f)) {
                    currentPath = pathfinder.findPath(new Vector2(x, y), lastKnownPlayerPos);
                    pathIndex = 0;
                    pathRecalcCooldown = PATH_RECALC_INTERVAL;
                }
                if (currentPath != null && pathIndex < currentPath.size) {
                    Vector2 target = currentPath.get(pathIndex);
                    float toTargetX = target.x - x;
                    float toTargetY = target.y - y;
                    float distToTarget = (float)Math.sqrt(toTargetX * toTargetX + toTargetY * toTargetY);
                    if (distToTarget > ARRIVAL_THRESHOLD) {
                        float moveSpeed = bomberSpeed * delta;
                        float norm = (float) Math.sqrt(toTargetX * toTargetX + toTargetY * toTargetY);
                        if (norm > 1e-3) {
                            float moveX = (toTargetX / norm) * moveSpeed;
                            float moveY = (toTargetY / norm) * moveSpeed;
                            collisionRectangle.move(x + moveX - (texture.getWidth() / 4f), y + moveY - (texture.getHeight() / 4f));
                            boolean collides = false;
                            for (CollisionRectangle rect : mapCollisions) {
                                if (collisionRectangle.collisionCheck(rect)) {
                                    collides = true;
                                    break;
                                }
                            }
                            if (!collides) {
                                x += moveX;
                                y += moveY;
                                state = EnemyState.MOVING;
                            } else {
                                collisionRectangle.move(x - (texture.getWidth() / 4f), y - (texture.getHeight() / 4f));
                            }
                        }
                        // Rotate towards target
                        float angleToTarget = (float)Math.toDegrees(Math.atan2(toTargetY, toTargetX));
                        targetRotation = angleToTarget;
                        smoothRotateTowards(targetRotation, delta);
                    } else {
                        pathIndex++;
                        state = EnemyState.CAUTIOUS;
                    }
                } else {
                    // Arrived at last known position, stop searching
                    lastKnownPlayerPos = null;
                    currentPath = null;
                    pathIndex = 0;
                    state = EnemyState.CAUTIOUS;
                }
            } else {
                state = EnemyState.UNAWARE;
            }
            wantsToShoot = false; // Never shoot
            return;
        }

        // VISION/AI STATE LOGIC
        if (state == EnemyState.ALERTED || state == EnemyState.MOVING) {
            if (lastPosition == null) lastPosition = new Vector2(x, y);
            float distMoved = lastPosition.dst(x, y);
            if (distMoved < MIN_MOVE_DIST) {
                stuckTime += delta;
            } else {
                stuckTime = 0f;
                lastPosition.set(x, y);
            }
            if (stuckTime > STUCK_THRESHOLD) {
                if (pathfinder != null && lastKnownPlayerPos != null) {
                    currentPath = pathfinder.findPath(new Vector2(x, y), lastKnownPlayerPos);
                    pathIndex = 0;
                    pathRecalcCooldown = PATH_RECALC_INTERVAL;
                }
                // Nudge enemy in a random direction to try to get unstuck
                float angle = (float)(Math.random() * Math.PI * 2);
                float nudgeDist = 5f;
                float nudgeX = (float)Math.cos(angle) * nudgeDist;
                float nudgeY = (float)Math.sin(angle) * nudgeDist;
                collisionRectangle.move(x + nudgeX - (texture.getWidth() / 4f), y + nudgeY - (texture.getHeight() / 4f));
                boolean collides = false;
                for (CollisionRectangle rect : mapCollisions) {
                    if (collisionRectangle.collisionCheck(rect)) {
                        collides = true;
                        break;
                    }
                }
                if (!collides) {
                    x += nudgeX;
                    y += nudgeY;
                } else {
                    collisionRectangle.move(x - (texture.getWidth() / 4f), y - (texture.getHeight() / 4f));
                }
                stuckTime = 0f;
                lastPosition.set(x, y);
            }
        }

        // Update the collision rectangle position
        collisionRectangle.move(
            x - (texture.getWidth() / 4f),
            y - (texture.getHeight() / 4f)
        );

        // Calculate vector to player
        float dx = player.getX() - this.x;
        float dy = player.getY() - this.y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        // Check distance
        if (distance > visionDistance) {
            playerRecentlySeen = false;
            state = EnemyState.UNAWARE;
        } else {
            // Calculate angle to player in world space
            float angleToPlayer = (float) Math.toDegrees(Math.atan2(dy, dx));
            float enemyFacingAngle = rotation;
            angleToPlayer = (angleToPlayer + 360) % 360;
            enemyFacingAngle = (enemyFacingAngle + 360) % 360;
            float angleDifference = Math.abs(angleToPlayer - enemyFacingAngle);
            if (angleDifference > 180) angleDifference = 360 - angleDifference;

            // Check if player is outside vision cone
            if (angleDifference <= visionAngle / 2f) {
                // Line-of-sight check (simple step-based raycast)
                int steps = (int) (distance / 10f);
                boolean blocked = false;
                for (int i = 1; i <= steps; i++) {
                    float t = i / (float) steps;
                    float checkX = this.x + dx * t;
                    float checkY = this.y + dy * t;
                    for (CollisionRectangle rect : mapCollisions) {
                        if (rect.getX() <= checkX && checkX <= rect.getX() + rect.getWidth() &&
                            rect.getY() <= checkY && checkY <= rect.getY() + rect.getHeight()) {
                            blocked = true;
                            break;
                        }
                    }
                    if (blocked) break;
                }
                if (!blocked) {
                    // === At this point, player is visible! ===
                    lastKnownPlayerPos = new Vector2(player.getX(), player.getY());
                    playerRecentlySeen = true;
                    // Clear path when player is visible
                    currentPath = null;
                    pathIndex = 0;
                    state = EnemyState.ALERTED;

                    // Move towards the player
                    float moveSpeed = speed * delta;
                    float norm = (float) Math.sqrt(dx * dx + dy * dy);
                    if (norm > 1e-3) {
                        float moveX = (dx / norm) * moveSpeed;
                        float moveY = (dy / norm) * moveSpeed;
                        collisionRectangle.move(x + moveX - (texture.getWidth() / 4f), y + moveY - (texture.getHeight() / 4f));
                        boolean collides = false;
                        for (CollisionRectangle rect : mapCollisions) {
                            if (collisionRectangle.collisionCheck(rect)) {
                                collides = true;
                                break;
                            }
                        }
                        if (!collides) {
                            x += moveX;
                            y += moveY;
                            state = EnemyState.MOVING;
                        } else {
                            collisionRectangle.move(x - (texture.getWidth() / 4f), y - (texture.getHeight() / 4f));
                        }
                    }
                    // Set target rotation to match the angle to player (in degrees)
                    targetRotation = angleToPlayer;
                    smoothRotateTowards(targetRotation, delta);
                    // Reaction timer logic
                    reactionTimer += delta;
                    if (reactionTimer >= REACTION_TIME) {
                        timeSinceLastShot += delta;
                        if (timeSinceLastShot >= shotCooldown) {
                            wantsToShoot = true;
                            float normShoot = (float)Math.sqrt(dx * dx + dy * dy);
                            shootDirX = dx / normShoot;
                            shootDirY = dy / normShoot;
                            timeSinceLastShot = 0f;
                        } else {
                            wantsToShoot = false;
                        }
                    } else {
                        wantsToShoot = false;
                    }
                    return;
                }
            }
        }
        // If here, player is not visible
        reactionTimer = 0f;
        wantsToShoot = false;
        // --- FIX: Always investigate lastKnownPlayerPos if set, even if playerRecentlySeen is false ---
        if (lastKnownPlayerPos != null) {
            pathRecalcCooldown -= delta;
            if (pathfinder != null && (currentPath == null || pathIndex >= currentPath.size || pathRecalcCooldown <= 0f)) {
                currentPath = pathfinder.findPath(new Vector2(x, y), lastKnownPlayerPos);
                pathIndex = 0;
                pathRecalcCooldown = PATH_RECALC_INTERVAL;
            }
            if (currentPath != null && pathIndex < currentPath.size) {
                Vector2 target = currentPath.get(pathIndex);
                float toTargetX = target.x - x;
                float toTargetY = target.y - y;
                float distToTarget = (float)Math.sqrt(toTargetX * toTargetX + toTargetY * toTargetY);
                if (distToTarget > ARRIVAL_THRESHOLD) {
                    float moveSpeed = speed * delta;
                    float norm = (float) Math.sqrt(toTargetX * toTargetX + toTargetY * toTargetY);
                    if (norm > 1e-3) {
                        float moveX = (toTargetX / norm) * moveSpeed;
                        float moveY = (toTargetY / norm) * moveSpeed;
                        collisionRectangle.move(x + moveX - (texture.getWidth() / 4f), y + moveY - (texture.getHeight() / 4f));
                        boolean collides = false;
                        for (CollisionRectangle rect : mapCollisions) {
                            if (collisionRectangle.collisionCheck(rect)) {
                                collides = true;
                                break;
                            }
                        }
                        if (!collides) {
                            x += moveX;
                            y += moveY;
                            state = EnemyState.MOVING;
                        } else {
                            collisionRectangle.move(x - (texture.getWidth() / 4f), y - (texture.getHeight() / 4f));
                        }
                    }
                    // Rotate towards target
                    float angleToTarget = (float)Math.toDegrees(Math.atan2(toTargetY, toTargetX));
                    targetRotation = angleToTarget;
                    smoothRotateTowards(targetRotation, delta);
                } else {
                    pathIndex++;
                    state = EnemyState.CAUTIOUS;
                }
            } else {
                // Arrived at last known position, stop searching
                lastKnownPlayerPos = null;
                currentPath = null;
                pathIndex = 0;
                state = EnemyState.CAUTIOUS;
            }
        } else {
            state = EnemyState.UNAWARE;
        }
    }
    
    // Helper method to smoothly rotate towards a target angle
    private void smoothRotateTowards(float targetAngle, float delta) {
        // Calculate the direct angular difference
        float angleDiff = targetAngle - rotation;
        
        // Normalize to [-180, 180] range to find shortest rotation path
        while (angleDiff > 180) angleDiff -= 360;
        while (angleDiff < -180) angleDiff += 360;
        
        // Calculate maximum rotation amount this frame
        float maxRotation = currentRotationSpeed * delta;
        
        // Apply rotation, limited by max rotation speed
        if (Math.abs(angleDiff) <= maxRotation) {
            // Close enough, snap to target
            rotation = targetAngle;
            currentRotationSpeed = rotationSpeed; // Reset to normal after turn
        } else {
            // Move towards target at maximum speed (in the correct direction)
            rotation += Math.signum(angleDiff) * maxRotation;
        }
    }

    // Public method to alert and turn towards a given position (e.g., player)
    public void alertAndTurnTo(float targetX, float targetY) {
        if (dead) return;
        float dx = targetX - this.x;
        float dy = targetY - this.y;
        float angleToTarget = (float) Math.toDegrees(Math.atan2(dy, dx));
        this.targetRotation = angleToTarget;
        this.state = EnemyState.ALERTED;
        this.currentRotationSpeed = fastRotationSpeed;
        this.alertedTimer = MIN_ALERTED_TIME; // Start/reset the alert timer
    }

    @Override
    public void render(SpriteBatch batch) {
        if (!dead) {
            super.render(batch);
        } else if (!disposeAfterDeath) {
            // Determine current frame based on deathElapsedTime
            int frame = (int) (deathElapsedTime / deathFrameDuration);
            if (frame >= deathFrames.length) frame = deathFrames.length - 1; // Clamp to last frame

            Texture currentFrame = deathFrames[frame];
            float drawWidth = currentFrame.getWidth() * 0.8f;   
            float drawHeight = currentFrame.getHeight() * 0.8f;

            batch.draw(
                currentFrame,
                x - drawWidth / 2f,
                y - drawHeight / 2f,
                drawWidth,
                drawHeight
            );
        }
    }
    
 // draw drop above enemy body
    public void renderDrop(SpriteBatch batch) {
        if (dead && dropTexture != null && !dropCollected) {
            float iconSize = 16f;
            batch.draw(dropTexture, x - iconSize / 2, y - iconSize / 2, iconSize, iconSize);
        }
    }


    public void takeDamage(int amount) {
        if (!dead) {
            // Probabilistic damage model
            DamageModel.HitResult hit = DamageModel.getHitResult();
            int finalDamage = Math.round(amount * hit.multiplier);
            health -= finalDamage;
            // Uncomment below for flat damage instead of probabilistic
            // health -= amount;
            // AGGRESSIVE: 10% chance to become BOMBER if health <= 25
            if (type == EnemyType.AGGRESSIVE && health <= 25 && Math.random() < 0.99) {
                type = EnemyType.BOMBER;
            }

            if (health <= 0) {
                dead = true;

                // assign random drop on death
                int roll = (int)(Math.random() * 4);
                switch (roll) {
                    case 0:
                        dropType = DropType.PISTOL_AMMO;
                        dropTexture = pistolDropIcon;
                        break;
                    case 1:
                        dropType = DropType.SHOTGUN_AMMO;
                        dropTexture = shotgunDropIcon;
                        break;
                    case 2:
                        dropType = DropType.CARBINE_AMMO;
                        dropTexture = carbineDropIcon;
                        break;
                    case 3:
                        dropType = DropType.MEDKIT;
                        dropTexture = medkitDropIcon;
                        break;
                }

                // Play death sound once
                deathSound.play();

                // BOMBER: 25% chance to detonate on death
                if (type == EnemyType.BOMBER && !hasExploded && Math.random() < 0.25) {
                    hasExploded = true;
                    if (levelScreenListener != null) {
                        levelScreenListener.createGrenadeExplosion(x, y);
                    }
                }
            }
        }
    }

    

    public boolean isDead() {
        return dead;
    }
    
    public float getRadius() {
        return enemyRadius;
    }

    public boolean wantsToShoot() {
        return wantsToShoot;
    }
    
    public float getShootDirX() {
        return shootDirX;
    }
    
    public float getShootDirY() {
        return shootDirY;
    }

    //reset shooting direction
    public void setShootDirX(float shootDirX) {
        this.shootDirX = shootDirX;
    }

    public void setShootDirY(float shootDirY) {
        this.shootDirY = shootDirY;
    }

    public void setWantsToShoot(boolean wantsToShoot) {
        this.wantsToShoot = wantsToShoot;
    }

    public CollisionRectangle getCollisionRectangle() {
        return collisionRectangle;
    }

    public float getVisionDistance() {
        return visionDistance;
    }

    public float getVisionAngle() {
        return visionAngle;
    }

    // Get rotation of the enemy
    public float getRotation() {
        return rotation;
    }

    public void setPathfinder(AStarPathfinder pathfinder) {
        this.pathfinder = pathfinder;
    }

    public EnemyState getState() {
        return state;
    }

    public boolean isDisposed() {
        return disposeAfterDeath;
    }

    public EnemyType getType() {
        return type;
    }

    public void setType(EnemyType type) {
        this.type = type;
    }

    public void setLevelScreenListener(LevelScreenListener listener) {
        this.levelScreenListener = listener;
    }

    // Call this when the enemy shoots
    public void playShootSound() {
        if (shootSound != null) {
            shootSound.play(0.5f);
        }
    }

    @Override
    public void dispose() {
        if (deathSound != null) deathSound.dispose();
        if (shootSound != null) shootSound.dispose();
        super.dispose();
    }
    
 // accessors for drops
    public DropType getDropType() {
        return dropType;
    }

    public boolean isDropCollected() {
        return dropCollected;
    }

    public void markDropCollected() {
        dropCollected = true;
    }

    public float getDropX() {
        return x;
    }

    public float getDropY() {
        return y;
    }

}
