package com.stormbreaker;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class HUD {

    private BitmapFont font;
    private ShapeRenderer shapeRenderer;
    private Texture pistolIcon;
    private Texture carbineIcon;
    private Texture shotgunIcon;

    public HUD(BitmapFont font) {
        this.font = font;
        font.getData().setScale(2f);
        this.shapeRenderer = new ShapeRenderer();
        pistolIcon = new Texture(Gdx.files.internal("hud_pistol1.png"));
        carbineIcon = new Texture(Gdx.files.internal("hud_carbine1.png"));
        shotgunIcon = new Texture(Gdx.files.internal("hud_shotgun1.png"));
    }

    public void render(SpriteBatch batch, Player player) {
        float health = Math.max(0, Math.min(100, player.getHealth()));
        float red = (100 - health) / 100f;
        float green = health / 100f;
        Color healthColor = new Color(red, green, 0, 1);

        // --- Dimensions of rectangle ---
        float rectX = 10;
        float rectY = Gdx.graphics.getHeight() - 170;
        float rectWidth = 460; // Increased to accommodate ammo display
        float rectHeight = 130;

        // --- Padding ---
        float paddingX = rectX + 20;
        float paddingY = rectY + rectHeight - 20;
        float lineSpacing = 35;

        // --- Background ---
        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0, 0, 0, 0.4f));
        shapeRenderer.rect(rectX, rectY, rectWidth, rectHeight);
        shapeRenderer.end();
        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);

        // --- Border ---
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(rectX, rectY, rectWidth, rectHeight);
        shapeRenderer.end();

        // --- Health ---
        batch.begin();
        font.setColor(healthColor);
        font.draw(batch, "Health: " + (int) health, paddingX, paddingY);

        // --- Weapon with Ammo Information ---
        Texture weaponIcon = null;
        float iconSize = 48;
        float iconY = paddingY - lineSpacing * 2 - 25;
        float textOffsetX = 60;
        String weaponName = player.getCurrentWeaponName();
        
        if (weaponName.equalsIgnoreCase("Pistol")) {
            weaponIcon = pistolIcon;
            iconSize = 48;
            textOffsetX = 65;
        } else if (weaponName.equalsIgnoreCase("Carbine")) {
            weaponIcon = carbineIcon;
            iconSize = 70;
            iconY = paddingY - lineSpacing * 2 - 40;
            textOffsetX = 85;
        } else if (weaponName.equalsIgnoreCase("Shotgun")) {
            weaponIcon = shotgunIcon;
            iconSize = 70;
            iconY = paddingY - lineSpacing * 2 - 55;
            textOffsetX = 85;
        }
        batch.draw(weaponIcon, paddingX, iconY, iconSize, iconSize);
        
        font.setColor(Color.WHITE);
        //int currentAmmo = player.getCurrentAmmo();
        int currentAmmo = player.getTotalAmmoCount();
        int magazineSize = player.getMagazineSize();
        String ammoInfo = currentAmmo + "/" + magazineSize;
        // Add ammo information: totalAmmoCount/magazineSize
        //int totalAmmoCount = player.getTotalAmmoCount();
        //int magazineSize = player.getMagazineSize();
        //weaponDisplay += "  " + totalAmmoCount + "/" + magazineSize;

        int totalMags = player.getTotalMags();
        if (totalMags > 0) {
            ammoInfo += "   ";
            for (int i = 0; i < totalMags; i++) {
                ammoInfo += "M ";
            }
        }
        
        font.draw(batch, ammoInfo, paddingX + textOffsetX, paddingY - lineSpacing * 2);
        batch.end();

        // --- Life bar ---
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
