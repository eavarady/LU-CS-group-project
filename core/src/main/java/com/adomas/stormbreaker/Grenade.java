package com.adomas.stormbreaker;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

public class Grenade {
    public static final float PPM = 100f; // pixels per meter for scalling for box2d
    private Body body;
    private float radius = 4.5f;
    private float fuseTime;
    private float timeAlive = 0f;
    private Vector2 target;
    private boolean reachedTarget = false;

    public Grenade(World world, float x, float y, float tx, float ty, float fuseTime) {
        this.fuseTime = fuseTime;

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
        fixtureDef.restitution = 0.8f; // Make it bouncy

        body.createFixture(fixtureDef);
        shape.dispose();

        // Calculate velocity
        float dx = tx - x;
        float dy = ty - y;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        float speed = 200f;
        float vx = (dx / length) * speed / PPM;
        float vy = (dy / length) * speed / PPM;
        body.setLinearVelocity(new Vector2(vx, vy));
        
        this.target = new Vector2(tx, ty).scl(1 / PPM); // scale target appropriately without mutating during update
    }

    public void update(float delta) {
        timeAlive += delta;

        if (!reachedTarget) {
            Vector2 currentPos = body.getPosition();
            if (currentPos.dst(target) <= radius / (2f * PPM)) {
                body.setLinearVelocity(0, 0);
                reachedTarget = true;
            }
        }

        if (timeAlive >= fuseTime) {
            body.setLinearVelocity(0, 0); // Redundant if already stopped, but safe
        }
    }

    public void render(ShapeRenderer sr) {
        Vector2 pos = body.getPosition();
        sr.circle(pos.x * PPM, pos.y * PPM, radius);
         // DEBUG: print current velocity of the grenade
        System.out.println("Grenade velocity (m/s): " + body.getLinearVelocity());
    }

    public boolean isExpired() {
        return timeAlive >= fuseTime;
    }

    public Body getBody() {
        return body;
    }
    public float getRadius() {
        return radius;
    }
}