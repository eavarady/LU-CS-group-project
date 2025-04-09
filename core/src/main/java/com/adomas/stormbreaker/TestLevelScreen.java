package com.adomas.stormbreaker;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.adomas.stormbreaker.Bullet;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.math.MathUtils;
import com.adomas.stormbreaker.Enemy;

/**
 * this fully replicates GameplayScreen logic but extends LevelScreen.
 * it should behave identically to GameplayScreen. 
 * GameplayScreen is redundant right now.
 */
public class TestLevelScreen extends LevelScreen {

    private final float speed = 100f;
    private Player player;
    private Array<Bullet> bullets = new Array<>();
    private Array<Enemy> enemies = new Array<>();

    private OrthographicCamera camera;
    private Viewport viewport;

    private float shootCooldown = 0.18f; // seconds between shots to prevent spammming
    private float timeSinceLastShot = 0f;

    public TestLevelScreen(StormbreakerGame game) {
        super(game);
    }

    @Override
    protected void initializeLevel() {

        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 720, camera); // same resolution
        viewport.apply();

        // create the player
        player = new Player(100, 100, speed, "Player_sprite_v1.png", camera);

        // add a few static enemies
        enemies.add(new Enemy(400, 300, 0, "enemy_blob.png"));
        enemies.add(new Enemy(600, 400, 0, "enemy_blob.png"));
        enemies.add(new Enemy(800, 200, 0, "enemy_blob.png"));
        enemies.add(new Enemy(1000, 500, 0, "enemy_blob.png"));

        // center camera
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // update player
        player.update(delta);

        // clamp position to viewport's width/height
        player.clampPosition(viewport.getWorldWidth(), viewport.getWorldHeight(),
                             viewport.getWorldWidth(), viewport.getWorldHeight());

        // camera follows player
        camera.position.set(player.getX(), player.getY(), 0);
        camera.update();

        // set projection for spriteBatch
        spriteBatch.setProjectionMatrix(camera.combined);

        // draw a dark-gray background 
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        shapeRenderer.end();

        // draw the player
        spriteBatch.begin();
        player.render(spriteBatch);
        // render enemies
        for (Enemy e : enemies) {
            e.render(spriteBatch);
        }
        spriteBatch.end();

        // draw dynamic crosshair based on distance from player to mouse
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);

        // get mouse position in screen coordinates and convert to world coordinates
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.input.getY();
        Vector3 mouseWorld = new Vector3(mouseX, mouseY, 0);
        viewport.unproject(mouseWorld);

        // calculate distance from player to mouse
        float dx = mouseWorld.x - player.getX();
        float dy = mouseWorld.y - player.getY();
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        // map distance to crosshair spacing (clamp between 10 and beyond)
        float expansionFactor = 1.5f; // lower = slower expansion, higher = faster. we'll adjust as needed
        float spacing = Math.max(5f, expansionFactor * (float) Math.sqrt(distance));

        // draw crosshair lines
        float cx = mouseWorld.x;
        float cy = mouseWorld.y;
        shapeRenderer.line(cx - spacing, cy, cx - spacing / 2, cy); // left
        shapeRenderer.line(cx + spacing / 2, cy, cx + spacing, cy); // right
        shapeRenderer.line(cx, cy - spacing, cx, cy - spacing / 2); // down
        shapeRenderer.line(cx, cy + spacing / 2, cx, cy + spacing); // up
        // remove mouse cursor and only keep the crosshair
        Gdx.input.setCursorCatched(true);

        shapeRenderer.end();

        ////////////////
        // bullet and enemy code
        timeSinceLastShot += delta;
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && timeSinceLastShot >= shootCooldown) {
        // if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) { // MINIGUN MODE
            float bulletX = player.getX();
            float bulletY = player.getY();
            float dirX = dx / distance;
            float dirY = dy / distance;

            // apply random spread angle
            float spreadAngle = 3.4f; // degree interval
            float angle = MathUtils.random(-spreadAngle, spreadAngle);
            float radians = angle * MathUtils.degreesToRadians;

            float spreadX = dirX * (float) Math.cos(radians) - dirY * (float) Math.sin(radians);
            float spreadY = dirX * (float) Math.sin(radians) + dirY * (float) Math.cos(radians);

            bullets.add(new Bullet(bulletX, bulletY, spreadX, spreadY));
            timeSinceLastShot = 0f; // reset cooldown
        }

        // bullet is a black dot for now
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.BLACK);
        for (int i = bullets.size - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            b.update(delta);
            b.render(shapeRenderer);
            // check bullet collision with enemies
            for (int j = enemies.size - 1; j >= 0; j--) {
                Enemy e = enemies.get(j);
                if (e.isDead()) continue;
                float dxE = e.getX() - b.getX();
                float dyE = e.getY() - b.getY();
                float distSq = dxE * dxE + dyE * dyE;
                float hitRadius = 15.5f; // we can adjust this hitbox size later
                if (distSq < hitRadius * hitRadius) {
                    e.takeDamage(25);
                    bullets.removeIndex(i);
                    break; // only one bullet per enemy per frame
                }
            }
            if (b.isOffScreen(viewport.getWorldWidth(), viewport.getWorldHeight())) {
                bullets.removeIndex(i);
            }
        }
        shapeRenderer.end();
        //////////////


        // draw white border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        shapeRenderer.end();

        // Escape key to exit
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
            Gdx.input.setCursorCatched(false); // show mouse cursor again
            game.setScreen(new MainMenuScreen(game)); // go back to main menu
        }
    }

    @Override
    public void resize(int width, int height) {
        System.out.println("Resizing to: " + width + "x" + height);
        viewport.update(width, height);
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
    }

    @Override
    public void dispose() {
        super.dispose();
        player.dispose();
    }
}