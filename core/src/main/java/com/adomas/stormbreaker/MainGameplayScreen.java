package com.adomas.stormbreaker;

import java.util.Iterator;

import com.adomas.stormbreaker.tools.AStarPathfinder;
import com.adomas.stormbreaker.tools.CollisionRectangle;
import com.adomas.stormbreaker.tools.MapManager;
import com.adomas.stormbreaker.weapons.Carbine;
import com.adomas.stormbreaker.weapons.Shotgun;
import com.adomas.stormbreaker.weapons.Weapon;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class MainGameplayScreen extends LevelScreen {

    private final float speed = 100f;
    private Player player;
    private Array<Bullet> bullets = new Array<>();
    private Array<Grenade> grenades = new Array<>();
    private Array<Enemy> enemies = new Array<>();
    private Array<SoundEvent> soundEvents = new Array<>();
    private MapManager mapManager;
    private World world;

    private OrthographicCamera camera;
    private Viewport viewport;

    private final float shotCooldown = 0.15f; // seconds between shots to prevent spammming
    private float timeSinceLastShot = 0f;
    private boolean wasGKeyPressedLastFrame = false;

    private HUD hud;
    private BitmapFont hudFont;
    private SpriteBatch hudBatch;

    // Add these fields for tracking reticle expansion state
    private float currentSpreadMultiplier = 1.0f; // Current spread multiplier (1.0 = default, increases when firing)
    private float maxSpreadMultiplier = 6.0f;     // Maximum spread multiplier when firing continuously
    private boolean shotFiredThisFrame = false;   // Flag to track if a shot was successfully fired this frame

    // Add these constants for reticle drawing
    private final float RETICLE_HAIR_LENGTH = 11.0f; // Fixed length of reticle hairs

    public MainGameplayScreen(StormbreakerGame game) {
        super(game);
    }

    @Override
    protected void initializeLevel() {
        // Initialize MapManager with the path to your Tiled map
        //System.out.println(Gdx.files.internal("maps/test_map.tmx").file().getAbsolutePath());
        mapManager = new MapManager("maps/test_map.tmx");
        // Get map dimensions in world units
        float mapWidth = mapManager.getMapWidth(); // Map width in world units
        float mapHeight = mapManager.getMapHeight(); // Map height in world units

        // Initialize camera and viewport with map dimensions
        camera = new OrthographicCamera();
        viewport = new FitViewport(mapWidth, mapHeight, camera); // Set viewport size to match map
        viewport.apply();

        world = new World(new Vector2(0, 0), true); // no gravity, allow sleeping

        // Create the player
        player = new Player(100, 100, speed, "Player_sprite_v1.png", camera);

        // Add a few static enemies
        enemies.add(new Enemy(400, 300, 80, "enemy_blob.png"));
        enemies.add(new Enemy(600, 400, 80, "enemy_blob.png"));
        enemies.add(new Enemy(800, 200, 80, "enemy_blob.png"));
        enemies.add(new Enemy(800, 500, 80, "enemy_blob.png"));

        // --- Ensure all enemies have a pathfinder for A* navigation ---
        float cellSize = 32f; // You can adjust this for pathfinding granularity
        float enemyBuffer = enemies.size > 0 ? enemies.get(0).getRadius() : 16f; // Use enemy radius as buffer
        AStarPathfinder pathfinder = new AStarPathfinder(
            mapManager.getMapWidth(),
            mapManager.getMapHeight(),
            cellSize,
            mapManager.getCollisionRectangles(),
            enemyBuffer
        );
        for (Enemy e : enemies) {
            e.setPathfinder(pathfinder);
        }

        // Center camera on the map
        camera.position.set(mapWidth / 2, mapHeight / 2, 0);
        camera.update();

        hudFont = new BitmapFont();
        hudBatch = new SpriteBatch();
        hud = new HUD(hudFont);

        // Create static Box2D bodies for map collision rectangles so grenades can bounce off them
        for (CollisionRectangle rect : mapManager.getCollisionRectangles()) {
            BodyDef bodyDef = new BodyDef();
            bodyDef.type = BodyDef.BodyType.StaticBody;
            bodyDef.position.set((rect.getX() + rect.getWidth() / 2f) / Grenade.PPM, (rect.getY() + rect.getHeight() / 2f) / Grenade.PPM);

            Body body = world.createBody(bodyDef);
            
            PolygonShape shape = new PolygonShape();
            shape.setAsBox(rect.getWidth() / 2f / Grenade.PPM, rect.getHeight() / 2f / Grenade.PPM);

            FixtureDef fixtureDef = new FixtureDef();
            fixtureDef.shape = shape;
            fixtureDef.density = 1f;
            fixtureDef.restitution = 0.6f; // adjust for bounce feel

            body.createFixture(fixtureDef);
            shape.dispose();
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        world.step(delta, 6, 2); // advance the physics simulation

        // update player
        player.update(delta, enemies, mapManager.getCollisionRectangles());

        // clamp position to viewport's width/height
        player.clampPosition(viewport.getWorldWidth(), viewport.getWorldHeight(),
                             viewport.getWorldWidth(), viewport.getWorldHeight());

        // camera follows player
        camera.position.set(player.getX(), player.getY(), 0);
        camera.update();

        // Render the map
        mapManager.render(camera);

        // set projection for spriteBatch
        spriteBatch.setProjectionMatrix(camera.combined);

        // draw a dark-gray background 
        shapeRenderer.setProjectionMatrix(camera.combined);
        // shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        // shapeRenderer.setColor(Color.DARK_GRAY);
        // shapeRenderer.rect(0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        // shapeRenderer.end();

        // draw the player
        spriteBatch.begin();
        player.render(spriteBatch);
        // render enemies
        for (Enemy e : enemies) {
            e.render(spriteBatch);
        }
        spriteBatch.end();

        // Aim cone
        float spreadAngle = 3f;

        // draw dynamic crosshair based on distance from player to mouse
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);

        // get mouse position in screen coordinates
        int mouseX = Gdx.input.getX();
        int mouseY = Gdx.input.getY();

        // clamp mouse position to screen bounds
        int clampedMouseX = MathUtils.clamp(mouseX, 0, Gdx.graphics.getWidth() - 1);
        int clampedMouseY = MathUtils.clamp(mouseY, 0, Gdx.graphics.getHeight() - 1);

        // if the mouse is outside the screen, reset its position
        if (mouseX != clampedMouseX || mouseY != clampedMouseY) {
            Gdx.input.setCursorPosition(clampedMouseX, clampedMouseY);
        }

        // convert clamped mouse position to world coordinates
        Vector3 mouseWorld = new Vector3(clampedMouseX, clampedMouseY, 0);
        viewport.unproject(mouseWorld);

        // calculate distance from player to mouse
        float dx = mouseWorld.x - player.getX();
        float dy = mouseWorld.y - player.getY();
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        // Reset the shot fired flag at the beginning of the frame
        shotFiredThisFrame = false;

        // Get weapon-specific spread and expansion values
        Weapon currentWeapon = player.getCurrentWeapon();
        float baseSpreadAngle = currentWeapon != null ? currentWeapon.getSpreadAngle() : 3.0f; // Default fallback
        float expansionFactor = currentWeapon != null ? currentWeapon.getReticleExpansionRate() : 1.5f; // Default fallback
        float contractionRate = currentWeapon != null ? currentWeapon.getReticleContractionRate() : 0.5f; // Default fallback
        
        // We'll update the spread multiplier after weapon firing
        // For now, just calculate the initial spreadAngle
        spreadAngle = baseSpreadAngle * currentSpreadMultiplier;

        // Calculate inner circle radius based on spread angle and distance
        float innerCircleRadius = (float) Math.tan(MathUtils.degreesToRadians * spreadAngle) * distance;

        // when G key is held down, enter grenade aim mode
        if (Gdx.input.isKeyPressed(Input.Keys.G)) {
           
            float maxGrenadeDistance = 250f; // max distance for grenade throw aiming, we'll adjust later
            float grenadeAimAngle = MathUtils.atan2(dy, dx);
            distance = Math.min(distance, maxGrenadeDistance);
            float cx = player.getX() + distance * MathUtils.cos(grenadeAimAngle);
            float cy = player.getY() + distance * MathUtils.sin(grenadeAimAngle);
             // draw dashed line from player to cursor
            shapeRenderer.setColor(Color.WHITE);
            float px = player.getX();
            float py = player.getY();

            float dashLength = 20f;
            int segments = Math.max(2, (int)(distance / dashLength) * 2); // ensure even number of segments

            for (int i = 0; i < segments; i += 2) {
                float t1 = i / (float) segments;
                float t2 = (i + 1) / (float) segments;
                float x1 = MathUtils.lerp(px, cx, t1);
                float y1 = MathUtils.lerp(py, cy, t1);
                float x2 = MathUtils.lerp(px, cx, t2);
                float y2 = MathUtils.lerp(py, cy, t2);
                shapeRenderer.line(x1, y1, x2, y2);
            }

            // draw grenade target circle at cursor
            float grenadeRadius = 6f;
            shapeRenderer.circle(cx, cy, grenadeRadius);
        }

        boolean isGKeyCurrentlyPressed = Gdx.input.isKeyPressed(Input.Keys.G);

        // detect G key release to throw grenade
        if (!isGKeyCurrentlyPressed && wasGKeyPressedLastFrame) {
            float maxGrenadeDistance = 250f;
            float grenadeAimAngle = MathUtils.atan2(dy, dx);
            float clampedDistance = Math.min(distance, maxGrenadeDistance);
            float targetX = player.getX() + clampedDistance * MathUtils.cos(grenadeAimAngle);
            float targetY = player.getY() + clampedDistance * MathUtils.sin(grenadeAimAngle);

            Grenade grenade = new Grenade(world, player.getX(), player.getY(), targetX, targetY, 2f); // 2 seconds fuse time
            grenades.add(grenade);
        }

        wasGKeyPressedLastFrame = isGKeyCurrentlyPressed;

        // CROSSHAR AIMING AND ENEMY DETECTION
        // Crosshair center position
        float cx = mouseWorld.x;
        float cy = mouseWorld.y;
        // Check if any enemy is within the inner circle (at the crosshair) and visible (no obstacle in the way)
        // Primitive raycasting
        boolean enemyInCrosshairAndVisible = false;
        for (Enemy e : enemies) {
            float ex = e.getX();
            float ey = e.getY();
            float distToEnemy = Vector2.dst(cx, cy, ex, ey); // Use crosshair center!
            if (distToEnemy <= innerCircleRadius) {
                // Line-of-sight check: step along the line from player to enemy
                boolean blocked = false;
                float playerX = player.getX();
                float playerY = player.getY();
                float dxToEnemy = ex - playerX;
                float dyToEnemy = ey - playerY;
                float distanceToEnemy = Vector2.dst(playerX, playerY, ex, ey);
                int steps = (int)(distanceToEnemy / 10f);
                for (int i = 1; i <= steps; i++) {
                    float t = i / (float) steps;
                    float checkX = playerX + dxToEnemy * t;
                    float checkY = playerY + dyToEnemy * t;
                    for (CollisionRectangle rect : mapManager.getCollisionRectangles()) {
                        if (rect.getX() <= checkX && checkX <= rect.getX() + rect.getWidth() &&
                            rect.getY() <= checkY && checkY <= rect.getY() + rect.getHeight()) {
                            blocked = true;
                            break;
                        }
                    }
                    if (blocked) break;
                }
                if (!blocked) {
                    enemyInCrosshairAndVisible = true;
                    break;
                }
            }
        }
        // Draw crosshair with fixed-length hairs
        shapeRenderer.setColor(enemyInCrosshairAndVisible ? Color.RED : Color.WHITE);
        
        // Left hair (inner radius to inner radius + fixed length)
        shapeRenderer.line(cx - innerCircleRadius - RETICLE_HAIR_LENGTH, cy, cx - innerCircleRadius, cy);
        
        // Right hair (inner radius to inner radius + fixed length)
        shapeRenderer.line(cx + innerCircleRadius, cy, cx + innerCircleRadius + RETICLE_HAIR_LENGTH, cy);
        
        // Bottom hair (inner radius to inner radius + fixed length)
        shapeRenderer.line(cx, cy - innerCircleRadius - RETICLE_HAIR_LENGTH, cx, cy - innerCircleRadius);
        
        // Top hair (inner radius to inner radius + fixed length)
        shapeRenderer.line(cx, cy + innerCircleRadius, cx, cy + innerCircleRadius + RETICLE_HAIR_LENGTH);

        // remove mouse cursor and only keep the crosshair
        Gdx.input.setCursorCatched(true);

        // Draw the invisible circle for debugging
        // shapeRenderer.setColor(Color.RED);
        // shapeRenderer.circle(cx, cy, innerCircleRadius); // Use the inner circle radius

        // Calculate the angle from player to mouse
        float baseAngle = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees;

        // Calculate the left and right edge angles of the cone
        float leftEdgeAngle = baseAngle - spreadAngle;
        float rightEdgeAngle = baseAngle + spreadAngle;

        // Calculate the end points of the cone edges (use a long enough distance, e.g., 1000 units)
        float coneLength = 1000f;
        float leftX = player.getX() + coneLength * MathUtils.cosDeg(leftEdgeAngle);
        float leftY = player.getY() + coneLength * MathUtils.sinDeg(leftEdgeAngle);
        float rightX = player.getX() + coneLength * MathUtils.cosDeg(rightEdgeAngle);
        float rightY = player.getY() + coneLength * MathUtils.sinDeg(rightEdgeAngle);

        // Draw the cone edges
        // shapeRenderer.setColor(Color.YELLOW);
        // shapeRenderer.line(player.getX(), player.getY(), leftX, leftY);
        // shapeRenderer.line(player.getX(), player.getY(), rightX, rightY);

        shapeRenderer.end();

        // --- SOUND EVENT SYSTEM ---
        // Update and remove expired sound events
        for (Iterator<SoundEvent> it = soundEvents.iterator(); it.hasNext(); ) {
            SoundEvent se = it.next();
            se.update(delta);
            if (se.isExpired()) it.remove();
        }
        // Visualize sound radii (expanding circles)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (SoundEvent se : soundEvents) {
            shapeRenderer.setColor(1f, 1f, 1f, 0.25f); // RGBA, alpha=0.25 for translucency
            Vector2 pos = se.getPosition();
            shapeRenderer.circle(pos.x, pos.y, se.getCurrentRadius());
        }
        shapeRenderer.end();

        ////////////////
        // bullet, grenade and enemy code
        // Update calls to fireWeapon and fireShotgun to pass currentSpreadMultiplier
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && !Gdx.input.isKeyPressed(Input.Keys.G)) {
            float bulletX = player.getX();
            float bulletY = player.getY();
            float dirX = dx / distance;
            float dirY = dy / distance;
            if (currentWeapon instanceof Shotgun) {
                Array<Bullet> shotgunPellets = player.fireShotgun(bulletX, bulletY, dirX, dirY, currentSpreadMultiplier);
                if (shotgunPellets != null) {
                    bullets.addAll(shotgunPellets);
                    shotFiredThisFrame = true;
                }
            } else if (!(currentWeapon instanceof Carbine)) {
                Bullet bullet = player.fireWeapon(bulletX, bulletY, dirX, dirY, currentSpreadMultiplier);
                if (bullet != null) {
                    bullets.add(bullet);
                    shotFiredThisFrame = true;
                }
            }
        }
        if (currentWeapon instanceof Carbine && Gdx.input.isButtonPressed(Input.Buttons.LEFT) && !Gdx.input.isKeyPressed(Input.Keys.G)) {
            float bulletX = player.getX();
            float bulletY = player.getY();
            float dirX = dx / distance;
            float dirY = dy / distance;
            Bullet bullet = player.fireWeapon(bulletX, bulletY, dirX, dirY, currentSpreadMultiplier);
            if (bullet != null) {
                bullets.add(bullet);
                shotFiredThisFrame = true;
            }
        }

        // Now update the spread multiplier AFTER weapon firing, when shotFiredThisFrame has been properly set
        if (shotFiredThisFrame) {
            // Increase spread when a shot is actually fired, but cap it at the maximum
            currentSpreadMultiplier = Math.min(currentSpreadMultiplier + expansionFactor, maxSpreadMultiplier);
        } else {
            // Gradually decrease spread when not firing
            currentSpreadMultiplier = Math.max(1.0f, currentSpreadMultiplier - contractionRate * delta);
        }
        
        // Update the spread angle with the new multiplier for the next frame
        spreadAngle = baseSpreadAngle * currentSpreadMultiplier;

        // --- TRIGGER SOUND EVENTS ---
        // Gunshot (when a shot is fired)
        if (shotFiredThisFrame) {
            soundEvents.add(new SoundEvent(
                new Vector2(player.getX(), player.getY()),
                350f, // gunshot max radius
                0.5f, // duration in seconds
                SoundEvent.Type.GUNSHOT
            ));
        }

        // Update and handle enemy shooting
        for (Enemy e : enemies) {
            // Use the new update method that includes soundEvents
            e.update(delta, player, mapManager.getCollisionRectangles(), soundEvents);
            if (e.wantsToShoot()) {
                float bulletX = e.getX();
                float bulletY = e.getY();
                float dirX = e.getShootDirX();
                float dirY = e.getShootDirY();
                // Add spread to enemy bullets (same as player pistol/carbine)
                spreadAngle = 6.0f; // degrees, adjust as needed
                float angle = (float) (Math.atan2(dirY, dirX) + Math.toRadians(com.badlogic.gdx.math.MathUtils.random(-spreadAngle, spreadAngle)));
                float spreadX = (float) Math.cos(angle);
                float spreadY = (float) Math.sin(angle);
                bullets.add(new Bullet(bulletX, bulletY, spreadX, spreadY, e));
                e.setWantsToShoot(false); // Reset shooting intent
                // Sound or muzzle flash here?
            }
        }

        // bullet is a white dot for now
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.WHITE);
        for (int i = bullets.size - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            b.update(delta, enemies, mapManager.getCollisionRectangles()); // Pass obstacles to the bullet
            b.render(shapeRenderer);

            if (b.isOffScreen(viewport.getWorldWidth(), viewport.getWorldHeight())) {
                bullets.removeIndex(i); // Remove bullet if it goes off-screen
            }
        }

        // render grenades
        shapeRenderer.setColor(Color.BLACK);
        for (int i = grenades.size - 1; i >= 0; i--) {
            Grenade g = grenades.get(i);
            g.update(delta);
            Vector2 pos = g.getBody().getPosition();
            shapeRenderer.circle(pos.x * Grenade.PPM, pos.y * Grenade.PPM, g.getRadius());
            if (g.isExpired()) {
                // Create the explosion at the grenade's current position
                createGrenadeExplosion(pos.x * Grenade.PPM, pos.y * Grenade.PPM);
                
                // Clean up the grenade
                world.destroyBody(g.getBody());
                grenades.removeIndex(i);
            }
        }
        shapeRenderer.end();

        // RENDER COLLISION RECTANGLES FOR DEBUGGING
        // Draw map collision rectangles
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.RED);
        for (CollisionRectangle rect : mapManager.getCollisionRectangles()) {
            shapeRenderer.rect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
        }
        //Draw player collision rectangle
        shapeRenderer.setColor(Color.GREEN);
        CollisionRectangle playerRect = player.getCollisionRectangle();
        shapeRenderer.rect(playerRect.getX(), playerRect.getY(), playerRect.getWidth(), playerRect.getHeight());
        shapeRenderer.end();
        // Draw enemy collision rectangles
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.BLUE);
        for (Enemy e : enemies) {
            CollisionRectangle enemyRect = e.getCollisionRectangle();
            shapeRenderer.rect(enemyRect.getX(), enemyRect.getY(), enemyRect.getWidth(), enemyRect.getHeight());
        }
        shapeRenderer.end();
        // Draw enemy vision cones
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.YELLOW);
        for (Enemy e : enemies) {
            float visionDistance = e.getVisionDistance();
            float visionAngle = e.getVisionAngle();
            float angle = e.getRotation() * MathUtils.degreesToRadians;
            float x = e.getX();
            float y = e.getY();
            shapeRenderer.arc(x, y, visionDistance, -visionAngle / 2f + angle * MathUtils.radiansToDegrees, visionAngle);
        }
        shapeRenderer.end();
        //////////////


        // draw white border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        shapeRenderer.end();

        hud.render(hudBatch, player);

        // Escape key to exit
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
            Gdx.input.setCursorCatched(false); // show mouse cursor again
            game.setScreen(new PauseMenuScreen(game, this)); // go back to main menu
        }
    }

    @Override
    public void resize(int width, int height) {
        //System.out.println("Resizing to: " + width + "x" + height);
        viewport.update(width, height);
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
    }

    @Override
    public void dispose() {
        if (world != null) {
            world.dispose();
        }
        super.dispose();
        mapManager.dispose();
        player.dispose();
    }

    public Player getPlayer() {
        return player;
    }

    // creates a grenade explosion with bullets spreading in a snowflake pattern (32 directions)
    private void createGrenadeExplosion(float x, float y) {
        int bulletCount = 32;
        
        // create bullets in a circle (every 11.25 degreees)
        for (int i = 0; i < bulletCount; i++) {
            // calculate angle for each bullet in rads
            float angle = (float) (i * (2 * Math.PI / bulletCount));
            
            // calculate direction vector for each angle
            float dirX = (float) Math.cos(angle);
            float dirY = (float) Math.sin(angle);
            
            // create the bullet and add it to the bullets array
            bullets.add(new Bullet(x, y, dirX, dirY, null)); // null owner means explosion bullet
        }

        // Add grenade sound event
        soundEvents.add(new SoundEvent(
            new Vector2(x, y),
            500f, // grenade explosion max radius
            0.5f, // duration in seconds
            SoundEvent.Type.GRENADE
        ));
    }
}