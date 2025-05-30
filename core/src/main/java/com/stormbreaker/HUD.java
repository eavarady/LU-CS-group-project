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
    private Texture grenadeIcon;
    // for hud icons
    private Texture pistolAmmoIcon;
    private Texture shotgunAmmoIcon;
    private Texture carbineAmmoIcon;

    public HUD(BitmapFont font) {
        this.font = font;
        font.getData().setScale(2f);
        this.shapeRenderer = new ShapeRenderer();
        pistolIcon = new Texture(Gdx.files.internal("hud_pistol1.png"));
        carbineIcon = new Texture(Gdx.files.internal("hud_carbine1.png"));
        shotgunIcon = new Texture(Gdx.files.internal("hud_shotgun1.png"));
        grenadeIcon = new Texture(Gdx.files.internal("Grenade.png"));
        // load icons
        pistolAmmoIcon = new Texture(Gdx.files.internal("pistolammo.png"));
        shotgunAmmoIcon = new Texture(Gdx.files.internal("shotgunammo.png"));
        carbineAmmoIcon = new Texture(Gdx.files.internal("carbineammo.png"));
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
        int currentAmmo = player.getTotalAmmoCount();
        int magazineSize = player.getMagazineSize();
        String ammoInfo = currentAmmo + "/" + magazineSize;

        float ammoTextX = paddingX + textOffsetX;
        float ammoTextY = paddingY - lineSpacing * 2;
        font.draw(batch, ammoInfo, ammoTextX, ammoTextY);

        float ammoIconSize = 16f;

        // show 1 ammo icon next to chamber info with count
        if (weaponName.equalsIgnoreCase("Pistol")) {
            int count = player.getPistolMags();
            float iconX = ammoTextX + 100;
            float iconY2 = ammoTextY - ammoIconSize * 0.75f; // tweaked for visuals in hud
            batch.draw(pistolAmmoIcon, iconX, iconY2, ammoIconSize, ammoIconSize);
            font.draw(batch, "x" + count, iconX + ammoIconSize + 5, iconY2 + ammoIconSize * 0.8f);
        } else if (weaponName.equalsIgnoreCase("Shotgun")) {
            int count = player.getShotgunMags();
            float iconX = ammoTextX + 100;
            float iconY2 = ammoTextY - ammoIconSize * 0.75f;
            batch.draw(shotgunAmmoIcon, iconX, iconY2, ammoIconSize, ammoIconSize);
            font.draw(batch, "x" + count, iconX + ammoIconSize + 5, iconY2 + ammoIconSize * 0.8f);
        } else if (weaponName.equalsIgnoreCase("Carbine")) {
            int count = player.getCarbineMags();
            float iconX = ammoTextX + 100;
            float iconY2 = ammoTextY - ammoIconSize * 0.75f;
            batch.draw(carbineAmmoIcon, iconX, iconY2, ammoIconSize, ammoIconSize);
            font.draw(batch, "x" + count, iconX + ammoIconSize + 5, iconY2 + ammoIconSize * 0.8f);
        }

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

        // draw grenades as individual icons to the right of the health bar
        batch.begin();
        float grenadeIconSize = 24;
        float grenadeSpacing = 5;
        float grenadeStartX = barX + barWidth + 10; // pos to the right of health bar
        float grenadeY = barY - grenadeIconSize / 2 + barHeight / 2;

        // draw one icon for each grenade the player has
        int grenadeCount = player.getGrenadeCount();
        for (int i = 0; i < grenadeCount; i++) {
            batch.draw(grenadeIcon,
                    grenadeStartX + (grenadeIconSize + grenadeSpacing) * i,
                    grenadeY,
                    grenadeIconSize,
                    grenadeIconSize);
        }
        batch.end();
    }
       //no idea if needed in this class but why not
    public void dispose() {
        pistolIcon.dispose();
        carbineIcon.dispose();
        shotgunIcon.dispose();
        grenadeIcon.dispose();
        pistolAmmoIcon.dispose();
        shotgunAmmoIcon.dispose();
        carbineAmmoIcon.dispose();
        shapeRenderer.dispose();
    }
}
