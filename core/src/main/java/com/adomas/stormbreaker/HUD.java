package com.adomas.stormbreaker;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class HUD {

    private BitmapFont font;
    private ShapeRenderer shapeRenderer;

    public HUD(BitmapFont font) {
        this.font = font;
        font.getData().setScale(2f);
        this.shapeRenderer = new ShapeRenderer();
    }

    public void render(SpriteBatch batch, Player player) {
        float health = Math.max(0, Math.min(100, player.getHealth()));
        float red = (100 - health) / 100f;
        float green = health / 100f;
        Color healthColor = new Color(red, green, 0, 1);

        // --- Dimensiones y posición del rectángulo ---
        float rectX = 10;
        float rectY = Gdx.graphics.getHeight() - 170;
        float rectWidth = 360;
        float rectHeight = 130;

        // --- Padding y líneas ---
        float paddingX = rectX + 20;
        float paddingY = rectY + rectHeight - 20;
        float lineSpacing = 35;

        // --- Fondo translúcido ---
        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0, 0, 0, 0.4f));
        shapeRenderer.rect(rectX, rectY, rectWidth, rectHeight);
        shapeRenderer.end();
        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);

        // --- Borde blanco ---
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(rectX, rectY, rectWidth, rectHeight);
        shapeRenderer.end();

        // --- Texto: Health ---
        batch.begin();
        font.setColor(healthColor);
        font.draw(batch, "Health: " + (int) health, paddingX, paddingY);

        // --- Texto: Weapon ---
        font.setColor(Color.WHITE);
        // Get the weapon name instead of using the object directly
        font.draw(batch, "Weapon: " + player.getCurrentWeaponName(), paddingX, paddingY - lineSpacing * 2);
        batch.end();

        // --- Barra de vida ---
        float barX = paddingX;
        float barY = paddingY - lineSpacing - 8;
        float barWidth = 250;
        float barHeight = 15;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);

        shapeRenderer.setColor(healthColor);
        shapeRenderer.rect(barX, barY, (health / 100f) * barWidth, barHeight);
        shapeRenderer.end();
    }
}
