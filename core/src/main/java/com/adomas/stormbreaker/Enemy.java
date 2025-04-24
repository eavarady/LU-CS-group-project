package com.adomas.stormbreaker;

import com.adomas.stormbreaker.tools.AStarPathfinder;
import com.adomas.stormbreaker.tools.CollisionRectangle;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Enemy extends NPC {

    private int health = 100;
    private boolean dead = false;
    private final float enemyRadius;
    // the radius of the enemy for player detection, 200 degrees in front of the enemy
    // this is used to check if the player is in the enemy's field of view
    private final float visionAngle = 100f;
    // Shot cooldown time
    private final float shotCooldown = 0.15f;
    // Time since last shot
    private float timeSinceLastShot = 0f;
    // Collision rectangle for the enemy
    private final CollisionRectangle collisionRectangle;
    // Vision distance for the enemy
    private final float visionDistance = 600f;
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

    // For dynamic unstuck logic
    private Vector2 lastPosition = null;
    private float stuckTime = 0f;
    private static final float STUCK_THRESHOLD = 0.5f; // seconds
    private static final float MIN_MOVE_DIST = 2f; // minimum distance considered as movement

    public Enemy(float x, float y, float speed, String texturePath) {
        super(x, y, speed, texturePath);
        this.enemyRadius = texture.getWidth() / 2f;
        this.collisionRectangle = new CollisionRectangle(x - (texture.getWidth() / 4f), y - (texture.getHeight() / 4f), texture.getWidth() / 2, texture.getHeight() / 2);
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
    }
    
    // Unified update method: handles both vision and sound detection
    public void update(float delta, Player player, Array<CollisionRectangle> mapCollisions, Array<SoundEvent> soundEvents) {
        if (dead) return;

        // --- SOUND DETECTION ---
        for (SoundEvent se : soundEvents) {
            float dist = Vector2.dst(x, y, se.getPosition().x, se.getPosition().y);
            if (dist <= se.getCurrentRadius()) {
                // No line-of-sight check for sound anymore
                // --- LOS check commented out below in case we want it back ---
                /*
                boolean blocked = false;
                float dx = se.getPosition().x - x;
                float dy = se.getPosition().y - y;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);
                int steps = (int) (distance / 10f);
                for (int i = 1; i <= steps; i++) {
                    float t = i / (float) steps;
                    float checkX = x + dx * t;
                    float checkY = y + dy * t;
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
                */
                lastKnownPlayerPos = new Vector2(se.getPosition());
                playerRecentlySeen = false;
                state = EnemyState.CAUTIOUS;
                currentPath = null;
                pathIndex = 0;
                currentRotationSpeed = fastRotationSpeed; // Turn fast towards sound
                break; // Only react to the first valid sound event
                //}
            }
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
                    return;
                }
            }
        }
        // If here, player is not visible
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
        // Instantly set state to ALERTED so AI logic can take over
        this.state = EnemyState.ALERTED;
        this.currentRotationSpeed = fastRotationSpeed; // Turn fast when hit
        // Optionally, snap rotation or let update() handle smooth turning
        // Here, we just set the target, update() will rotate smoothly
    }

    public void render(SpriteBatch batch) {
        if (!dead) {
            super.render(batch);
        }
    }

    public void takeDamage(int baseDamage) {
        if (!dead) {
            DamageModel.HitResult hit = DamageModel.getHitResult();
            System.out.println("BodyPart Hit: " + hit.part);
            int finalDamage = Math.round(baseDamage * hit.multiplier);
            health -= finalDamage;
            // Optionally: handle bleed effect here using hit.bleedChance
            // Example: if (Math.random() < hit.bleedChance) { /* apply bleed */ }
            if (health <= 0) {
                dead = true;
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
}