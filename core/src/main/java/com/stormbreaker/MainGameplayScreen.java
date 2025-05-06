package com.stormbreaker;

import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
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
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.stormbreaker.tools.AStarPathfinder;
import com.stormbreaker.tools.CollisionRectangle;
import com.stormbreaker.tools.MapManager;
import com.stormbreaker.weapons.Carbine;
import com.stormbreaker.weapons.Shotgun;
import com.stormbreaker.weapons.Weapon;

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

    private Sound grenadeExplosionSound;
    private Sound pistolSound; // Add this for pistol sound

    private Music backgroundMusic;
    
    private float deathTimer = 0f;
    private boolean deathTriggered = false;
    
    // fields for "Hold F to heal" text
    private BitmapFont bleedingTextFont;
    private SpriteBatch bleedingTextBatch;
    private float bleedingTextPulseTimer = 0f;
    
    // Fields for "Press R to reload" text
    private BitmapFont reloadTextFont;
    private float reloadTextPulseTimer = 0f;


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
        enemies.add(new Enemy(400, 300, 80, "enemy_blob.png", Enemy.EnemyType.PASSIVE)); // PASSIVE
        enemies.add(new Enemy(600, 400, 80, "enemy_blob.png")); // AGGRESSIVE (default)
        enemies.add(new Enemy(800, 200, 80, "enemy_blob.png", Enemy.EnemyType.PASSIVE)); // PASSIVE
        enemies.add(new Enemy(800, 600, 80, "enemy_blob.png")); // AGGRESSIVE (default)
        enemies.add(new Enemy(100, 500, 80, "enemy_blob.png", Enemy.EnemyType.BOMBER)); // BOMBER
        enemies.add(new Enemy(200, 300, 80, "enemy_blob.png", Enemy.EnemyType.BOMBER)); // BOMBER
        // --- Ensure all enemies have a pathfinder for A* navigation ---
        float cellSize = 32f; // You can adjust this for pathfinding granularity
        // Add 1 pixel to the enemy buffer to ensure a 1-pixel standoff from obstacles
        float enemyBuffer = enemies.size > 0 ? enemies.get(0).getRadius() + 0.5f : 17f; // Use enemy radius + 1 as buffer
        AStarPathfinder pathfinder = new AStarPathfinder(
            mapManager.getMapWidth(),
            mapManager.getMapHeight(),
            cellSize,
            mapManager.getCollisionRectangles(),
            enemyBuffer
        );
        for (Enemy e : enemies) {
            e.setPathfinder(pathfinder);
            // Set grenade explosion callback for BOMBERs
            e.setLevelScreenListener(this::createGrenadeExplosion);
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
        grenadeExplosionSound = Gdx.audio.newSound(Gdx.files.internal("grenade.wav"));
        pistolSound = Gdx.audio.newSound(Gdx.files.internal("pistol_shot.wav"));
        Grenade.setPistolSound(pistolSound); // Provide pistol sound to all grenades

        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("backgroundmusic.mp3"));
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(1.0f);
        backgroundMusic.play();
        
        // bleeding text font and batch
        bleedingTextFont = new BitmapFont();
        bleedingTextFont.getData().setScale(2.0f);
        bleedingTextBatch = new SpriteBatch();
        
        // reload text font (using the same batch as bleeding text)
        reloadTextFont = new BitmapFont();
        reloadTextFont.getData().setScale(2.0f);

        // Add Box2D contact listener for grenade bounces
        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                Fixture fixA = contact.getFixtureA();
                Fixture fixB = contact.getFixtureB();
                // Check if either fixture is a grenade
                Grenade grenade = null;
                if (fixA.getBody().getUserData() instanceof Grenade) {
                    grenade = (Grenade) fixA.getBody().getUserData();
                } else if (fixB.getBody().getUserData() instanceof Grenade) {
                    grenade = (Grenade) fixB.getBody().getUserData();
                }
                if (grenade != null && !grenade.shouldTriggerDamage()) {
                    grenade.setSoundEventsRef(soundEvents); // ensure reference is set
                    grenade.onBounce();
                }
            }
            @Override public void endContact(Contact contact) {}
            @Override public void preSolve(Contact contact, Manifold oldManifold) {}
            @Override public void postSolve(Contact contact, ContactImpulse impulse) {}
        });
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

        // draw the sprites in order: dead enemies first, then player, then live enemies
        spriteBatch.begin();
        // first render dead enemies (player walks over them)
        for (Enemy e : enemies) {
            if (e.isDead()) {
                e.render(spriteBatch);
            }
        }
        // then render the player
        player.render(spriteBatch);
        // and then the live enemies (they walk over the player)
        for (Enemy e : enemies) {
            if (!e.isDead()) {
                e.render(spriteBatch);
            }
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
            // only throw if player has grenades available
            if (player.hasGrenades()) {
                float maxGrenadeDistance = 250f;
                float grenadeAimAngle = MathUtils.atan2(dy, dx);
                float clampedDistance = Math.min(distance, maxGrenadeDistance);
                float targetX = player.getX() + clampedDistance * MathUtils.cos(grenadeAimAngle);
                float targetY = player.getY() + clampedDistance * MathUtils.sin(grenadeAimAngle);

                Grenade grenade = new Grenade(world, player.getX(), player.getY(), targetX, targetY, 2f); // 2 seconds fuse time
                grenades.add(grenade);
                
                // decrement grenade count
                player.useGrenade();
            }
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
                // Only consider alive enemies
                if (e.isDead()) continue;
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
                500f, // gunshot max radius
                0.01f, // duration in seconds
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
                e.playShootSound(); // Play enemy shooting sound
                e.setWantsToShoot(false); // Reset shooting intent
                // Sound or muzzle flash here?
            }
        }

        spriteBatch.begin();
        for (int i = bullets.size - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            b.update(delta, enemies, mapManager.getCollisionRectangles(), player);
            b.render(spriteBatch); // use sprite batch to render bullet with texture
            if (b.isOffScreen(viewport.getWorldWidth(), viewport.getWorldHeight())) {
                bullets.removeIndex(i);
            }
        }
        spriteBatch.end();

     // delay restart after death to show death frame
        if (player.isDead()) { 
            if (!deathTriggered) {
                deathTriggered = true; // mark that we've started the death timer
                deathTimer = 0f;
            } else {
                deathTimer += delta; // incrment timer
                if (deathTimer >= 2f) { 
                    backgroundMusic.stop();
                    game.setScreen(new MainGameplayScreen(game)); // restart the level
                    return;
                }
            }
        }

       

     // update grenades 
        for (int i = grenades.size - 1; i >= 0; i--) {
            Grenade g = grenades.get(i);
            g.update(delta);
            
            // Check if we need to trigger explosion damage right at the start of the explosion
            if (g.shouldTriggerDamage()) {
                Vector2 pos = g.getBody().getPosition();
                createGrenadeExplosion(pos.x * Grenade.PPM, pos.y * Grenade.PPM);
                g.markDamageTriggered();
            }
            
            // Only remove the grenade after its explosion animation is complete
            if (g.isExpired()) {
                world.destroyBody(g.getBody());
                grenades.removeIndex(i);
            }
        }

        // draw grenades
        spriteBatch.begin();
        for (Grenade g : grenades) {
            g.render(spriteBatch); // draw grenade with blinking texture
        }
        spriteBatch.end();

        
        //render grenade explosion animations
        spriteBatch.begin();
        for (Grenade g : grenades) {
            g.renderExplosion(spriteBatch);
        }
        spriteBatch.end();


        // RENDER COLLISION RECTANGLES FOR DEBUGGING
        // Draw map collision rectangles
        // shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        // shapeRenderer.setColor(Color.RED);
        // for (CollisionRectangle rect : mapManager.getCollisionRectangles()) {
        //     shapeRenderer.rect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
        // }
        //Draw player collision rectangle
        // shapeRenderer.setColor(Color.GREEN);
        // CollisionRectangle playerRect = player.getCollisionRectangle();
        // shapeRenderer.rect(playerRect.getX(), playerRect.getY(), playerRect.getWidth(), playerRect.getHeight());
        // shapeRenderer.end();
        // Draw enemy collision rectangles
        // shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        // shapeRenderer.setColor(Color.BLUE);
        // for (Enemy e : enemies) {
        //     CollisionRectangle enemyRect = e.getCollisionRectangle();
        //     shapeRenderer.rect(enemyRect.getX(), enemyRect.getY(), enemyRect.getWidth(), enemyRect.getHeight());
        // }
        // shapeRenderer.end();
        // // Draw enemy vision cones
        // shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        // shapeRenderer.setColor(Color.YELLOW);
        // for (Enemy e : enemies) {
        //     float visionDistance = e.getVisionDistance();
        //     float visionAngle = e.getVisionAngle();
        //     float angle = e.getRotation() * MathUtils.degreesToRadians;
        //     float x = e.getX();
        //     float y = e.getY();
        //     shapeRenderer.arc(x, y, visionDistance, -visionAngle / 2f + angle * MathUtils.radiansToDegrees, visionAngle);
        // }
        // shapeRenderer.end();
        //////////////


        // draw white border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        shapeRenderer.end();

        hud.render(hudBatch, player);

        // print "Hold F to heal" text when player is bleeding
        if (player.isBleeding()) {
            // Update pulse timer
            bleedingTextPulseTimer += delta;
            
            // calculate alpha to create a pulsing effect
            float alpha = (float)(0.5f + 0.5f * Math.sin(bleedingTextPulseTimer * 6));
            
            // text color red with pulsing opacity
            bleedingTextFont.setColor(1f, 0f, 0f, alpha);
            
            // draw text at the bottom center of the screen
            String text = "Hold F to heal";
            bleedingTextBatch.begin();
            
            // calculate text position
            float x = (Gdx.graphics.getWidth() - bleedingTextFont.getData().scaleX * text.length() * 11) / 2f;
            float y = 100f;
            
            bleedingTextFont.draw(bleedingTextBatch, text, x, y);
            bleedingTextBatch.end();
        }

        // "Press R to reload" text when the player's magazine is empty
        if (!player.isReloading() && player.getCurrentAmmo() == 0 && player.getTotalMags() > 0) {
            // update pulse timer
            reloadTextPulseTimer += delta;
            
            // alpha for pulsing effect 
            float alpha = (float)(0.5f + 0.5f * Math.sin(reloadTextPulseTimer * 3));
            
            // yellow text color with pulsing opacity
            reloadTextFont.setColor(1f, 1f, 0f, alpha);
            String text = "Press R to reload";
            bleedingTextBatch.begin();
            
            // calculate text position
            float x = (Gdx.graphics.getWidth() - reloadTextFont.getData().scaleX * text.length() * 11) / 2f;
            float y = player.isBleeding() ? 140f : 100f; // Position above healing text if both are showing
            
            reloadTextFont.draw(bleedingTextBatch, text, x, y);
            bleedingTextBatch.end();
        }

        // Escape key to exit
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
            Gdx.input.setCursorCatched(false); // show mouse cursor again
            backgroundMusic.pause();
            game.setScreen(new PauseMenuScreen(game, this)); // go back to main menu
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
    }

    @Override
    public void dispose() {
        if (world != null) {
            world.dispose();
        }
        grenadeExplosionSound.dispose();
        bleedingTextFont.dispose();
        bleedingTextBatch.dispose();
        reloadTextFont.dispose();
        if (pistolSound != null) pistolSound.dispose();
        super.dispose();
        mapManager.dispose();
        player.dispose();
        backgroundMusic.dispose();
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
            600f, // grenade explosion max radius
            0.5f, // duration in seconds
            SoundEvent.Type.GRENADE
        ));
        grenadeExplosionSound.play(1.0f);  
    }

    public void resumeMusic() {
        if (backgroundMusic != null && !backgroundMusic.isPlaying()) {
            backgroundMusic.play();
        }
    }

    public void stopMusic() {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
        }
    }
}
