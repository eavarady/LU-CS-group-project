package com.stormbreaker;

import java.util.Random;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

public class Grenade {
    public static final float PPM = 100f; // pixels per meter for scaling for box2d
    private Body body;
    private float radius = 4.5f;
    private float fuseTime;
    private float timeAlive = 0f;
    private float distanceTraveled = 0f;
    private float maxTravelDistance;
    private Vector2 lastPosition;
    private float rotationAngle; // random rotation angle for the grenade sprite

    private static Texture grenadeTexture; // grenade texture
    private static Texture explosionTexture; // explosion sprite frames
    private static Animation<TextureRegion> explosionAnimation; // for explosion animation

    private float explosionTimer = 0f; //tracks time since explosion started
    private boolean exploded = false;
    private Vector2 explosionPosition = null;
    private boolean damageTriggered = false; // to track if explosion damage has been applied

    private static final float BLINK_DURATION = 0.5f; //grenade blinks during last half sec

    private static Sound pistolSound; // static so all grenades share
    private Array<SoundEvent> soundEventsRef; // reference to main soundEvents array

    public Grenade(World world, float x, float y, float tx, float ty, float fuseTime) {
        this.fuseTime = fuseTime;
        this.maxTravelDistance = new Vector2(tx - x, ty - y).len() / PPM;

        // Define the body
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x / PPM, y / PPM);
        this.body = world.createBody(bodyDef);
        
        // Create circular shape
        CircleShape shape = new CircleShape();
        shape.setRadius(radius / PPM);
        
        // Define fixture
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        fixtureDef.restitution = 0.2f; // make it bouncy

        body.createFixture(fixtureDef);
        shape.dispose();

        // Calculate velocity
        float dx = tx - x;
        float dy = ty - y;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        float speed = 350f;
        float vx = (dx / length) * speed / PPM;
        float vy = (dy / length) * speed / PPM;
        body.setLinearVelocity(new Vector2(vx, vy));

        this.lastPosition = new Vector2(body.getPosition());

        // load grenade texture
        if (grenadeTexture == null) {
            grenadeTexture = new Texture("Grenade.png");
        }

        // load explosion sprite sheet and create animation
        if (explosionTexture == null) {
            explosionTexture = new Texture("Explosion.png");
            TextureRegion[][] tmp = TextureRegion.split(explosionTexture, 96, 96);
            TextureRegion[] frames = new TextureRegion[12];
            for (int i = 0; i < 12; i++) {
                frames[i] = tmp[0][i];
            }
            explosionAnimation = new Animation<>(0.05f, frames); // 12 frames, 0.05s per frame
        }

        // generate random rotation angle
        Random random = new Random();
        rotationAngle = random.nextFloat() * 360f;

        this.body.setUserData(this); // So we can identify the grenade in contacts
    }

    public void update(float delta) {
        timeAlive += delta;

        Vector2 currentPos = body.getPosition();
        distanceTraveled += currentPos.dst(lastPosition);
        lastPosition.set(currentPos);

        if (!exploded && distanceTraveled >= maxTravelDistance) {
            body.setLinearVelocity(0, 0);
        }

        if (!exploded && timeAlive >= fuseTime) {
            body.setLinearVelocity(0, 0);
            exploded = true;
            explosionPosition = new Vector2(body.getPosition());
        }

        if (exploded) {
            explosionTimer += delta;
        }
    }

    // render grenade image with blink and scale
    public void render(SpriteBatch batch) {
        if (!exploded) {
            Vector2 pos = body.getPosition();

            // blink during last moments of fuse
            boolean shouldDraw = true;
            if (fuseTime - timeAlive <= BLINK_DURATION) {
                shouldDraw = ((int)(timeAlive * 10) % 2 == 0); // blink every 0.1s
            }

            if (shouldDraw) {
                float scale = 0.01f; // shrink sprite
                float width = grenadeTexture.getWidth() * scale;
                float height = grenadeTexture.getHeight() * scale;
                float drawX = pos.x * PPM - width / 2f; // center horizontally
                float drawY = pos.y * PPM - height / 2f;
                // draw grenade sprite WITH random rotation
                batch.draw(grenadeTexture, drawX, drawY, width / 2f, height / 2f, width, height, 1f, 1f, rotationAngle, 0, 0, grenadeTexture.getWidth(), grenadeTexture.getHeight(), false, false);
            }
        }
    }

    // render explosion animation once grenade detonates
    public void renderExplosion(SpriteBatch batch) {
        if (exploded && explosionTimer <= explosionAnimation.getAnimationDuration()) {
            TextureRegion frame = explosionAnimation.getKeyFrame(explosionTimer, false);
            float drawX = explosionPosition.x * PPM - 48;
            float drawY = explosionPosition.y * PPM - 48;
            batch.draw(frame, drawX, drawY);
        }
    }

    public boolean isExpired() {
        return exploded && explosionTimer >= explosionAnimation.getAnimationDuration();
    }

    public Body getBody() {
        return body;
    }

    public float getRadius() {
        return radius;
    }

    //check if explosion damage should be triggered
    public boolean shouldTriggerDamage() {
        return exploded && !damageTriggered;
    }
    
    // mark the damage as triggered
    public void markDamageTriggered() {
        damageTriggered = true;
    }

    // dispose textures 
    public static void dispose() {
        if (grenadeTexture != null) {
            grenadeTexture.dispose();
            grenadeTexture = null;
        }
        if (explosionTexture != null) {
            explosionTexture.dispose();
            explosionTexture = null;
        }
    }

    public static void setPistolSound(Sound sound) {
        pistolSound = sound;
    }

    public void setSoundEventsRef(Array<SoundEvent> soundEvents) {
        this.soundEventsRef = soundEvents;
    }

    // Call this when the grenade bounces
    public void onBounce() {
        if (pistolSound != null) pistolSound.play(0.5f);
        if (soundEventsRef != null) {
            Vector2 pos = body.getPosition();
            soundEventsRef.add(new SoundEvent(
                new Vector2(pos.x * PPM, pos.y * PPM),
                300f, // radius for bounce sound
                0.05f, // short duration
                SoundEvent.Type.GUNSHOT
            ));
        }
    }
}
