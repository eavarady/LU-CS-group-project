package com.stormbreaker;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class LevelSelectScreen implements Screen {

    private final StormbreakerGame game;
    private Stage stage;
    private Texture backgroundTex;
    private Texture level1Texture, level2Texture, level3Texture;
    private Texture backTexture;
    private Sound clickSound;

    public LevelSelectScreen(StormbreakerGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        backgroundTex = new Texture(Gdx.files.internal("pausebackround.png"));
        level1Texture = new Texture(Gdx.files.internal("level1crop.png"));
        level2Texture = new Texture(Gdx.files.internal("level2crop.png"));
        level3Texture = new Texture(Gdx.files.internal("level3crop.png"));
        backTexture = new Texture(Gdx.files.internal("backcrop.png"));


        Image background = new Image(new TextureRegionDrawable(backgroundTex));
        background.setFillParent(true);
        stage.addActor(background);

        ImageButton level1Button = createHoverButton(level1Texture);
        ImageButton level2Button = createHoverButton(level2Texture);
        ImageButton level3Button = createHoverButton(level3Texture);
        ImageButton backButton = createHoverButton(backTexture);

        clickSound = Gdx.audio.newSound(Gdx.files.internal("click_button.mp3"));

        level1Button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.play();
                game.currentLevelIndex = 0;
                game.menuMusic.stop();
                game.setScreen(new MainGameplayScreen(game));
                dispose();
            }
        });

        level2Button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.play();
                game.currentLevelIndex = 1;
                game.menuMusic.stop();
                game.setScreen(new MainGameplayScreen(game));
                dispose();
            }
        });

        level3Button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.play();
                game.currentLevelIndex = 2;
                game.menuMusic.stop();
                game.setScreen(new MainGameplayScreen(game));
                dispose();
            }
        });

        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.play();
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
        });

        Table table = new Table();
        table.setFillParent(true);
        table.center();

        float screenHeight = Gdx.graphics.getHeight();
        float buttonHeight = screenHeight * 0.15f;
        float spacing = screenHeight * 0.02f;

        float level1Aspect = (float) level1Texture.getWidth() / level1Texture.getHeight();
        float level2Aspect = (float) level2Texture.getWidth() / level2Texture.getHeight();
        float level3Aspect = (float) level3Texture.getWidth() / level3Texture.getHeight();
        float backAspect = (float) backTexture.getWidth() / backTexture.getHeight();

        float level1Width = buttonHeight * level1Aspect;
        float level2Width = buttonHeight * level2Aspect;
        float level3Width = buttonHeight * level3Aspect;
        float backWidth = buttonHeight * backAspect;

        table.add(level1Button).width(level1Width).height(buttonHeight).padBottom(spacing).row();
        table.add(level2Button).width(level2Width).height(buttonHeight).padBottom(spacing).row();
        table.add(level3Button).width(level3Width).height(buttonHeight).padBottom(spacing).row();
        table.add(backButton).width(backWidth).height(buttonHeight);
        table.row().padTop(spacing);
        

        table.setTouchable(Touchable.childrenOnly);
        stage.addActor(table);
    }

    private ImageButton createHoverButton(Texture texture) {
        TextureRegionDrawable up = new TextureRegionDrawable(new TextureRegion(texture));
        Drawable hover = new TextureRegionDrawable(new TextureRegion(texture))
                .tint(new Color(1f, 1f, 1f, 0.5f));

        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.imageUp = up;
        style.imageOver = hover;

        return new ImageButton(style);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
        backgroundTex.dispose();
        level1Texture.dispose();
        level2Texture.dispose();
        level3Texture.dispose();
        backTexture.dispose();
    }
}
