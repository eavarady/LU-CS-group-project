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
    private float distanceTraveled = 0f;
    private float maxTravelDistance;
    private Vector2 lastPosition;

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

        // Cgit alculate velocity
        float dx = tx - x;
        float dy = ty - y;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        float speed = 350f;
        float vx = (dx / length) * speed / PPM;
        float vy = (dy / length) * speed / PPM;
        body.setLinearVelocity(new Vector2(vx, vy));

        this.lastPosition = new Vector2(body.getPosition());
    }

    public void update(float delta) {
        timeAlive += delta;

        Vector2 currentPos = body.getPosition();
        distanceTraveled += currentPos.dst(lastPosition);
        lastPosition.set(currentPos);

        if (distanceTraveled >= maxTravelDistance) {
            body.setLinearVelocity(0, 0);
        }

        if (timeAlive >= fuseTime) {
            body.setLinearVelocity(0, 0); 
        }
    }

    public void render(ShapeRenderer sr) {
        Vector2 pos = body.getPosition();
        sr.circle(pos.x * PPM, pos.y * PPM, radius);

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