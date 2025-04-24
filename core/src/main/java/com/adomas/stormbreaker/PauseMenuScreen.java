package com.adomas.stormbreaker;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;


public class PauseMenuScreen implements Screen {

    private final StormbreakerGame game;
    private final Screen previousScreen;

    private Stage stage;

    private Texture backgroundTex;
    private Texture resumeTex, restartTex, exitTex;

    private Sound clickSound;

    public PauseMenuScreen(StormbreakerGame game, Screen previousScreen) {
        this.game = game;
        this.previousScreen = previousScreen;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        game.menuMusic.play();

        // Load assets
        backgroundTex = new Texture(Gdx.files.internal("pausebackround.png"));
        resumeTex = new Texture(Gdx.files.internal("resumenewpause.png"));
        restartTex = new Texture(Gdx.files.internal("restartnewpause.png"));
        exitTex = new Texture(Gdx.files.internal("exitnewpause.png"));

        // Background
        Image background = new Image(new TextureRegionDrawable(backgroundTex));
        background.setFillParent(true);
        background.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled); // absorb input
        stage.addActor(background);

        // Buttons
        ImageButton resumeButton = createHoverButton(resumeTex);
        ImageButton restartButton = createHoverButton(restartTex);
        ImageButton exitButton = createHoverButton(exitTex);

        
        float buttonWidth = 300f;
        float scale = buttonWidth / resumeTex.getWidth();
        float buttonHeight = resumeTex.getHeight() * scale;

        resumeButton.setSize(buttonWidth, buttonHeight);
        restartButton.setSize(buttonWidth, buttonHeight);
        exitButton.setSize(buttonWidth, buttonHeight);

        resumeButton.pack();
        restartButton.pack();
        exitButton.pack();

        
        resumeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.play();
                game.setScreen(previousScreen);
                Gdx.input.setCursorCatched(true);
                game.menuMusic.stop();
                dispose(); 
            }
        });

        restartButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.play();
                game.setScreen(new MainGameplayScreen(game));
                game.menuMusic.stop();
                dispose(); 
            }
        });

        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.play();
                game.setScreen(new MainMenuScreen(game));
                dispose(); // fix ghost clicks
            }
        });

        // Layout
        Table table = new Table();
        table.setFillParent(true);
        table.center();
        table.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.childrenOnly); // only buttons clickable

        table.add(resumeButton).width(buttonWidth).height(buttonHeight).padBottom(25f).row();
        table.add(restartButton).width(buttonWidth).height(buttonHeight).padBottom(25f).row();
        table.add(exitButton).width(buttonWidth).height(buttonHeight);

        stage.addActor(table);

        clickSound = Gdx.audio.newSound(Gdx.files.internal("click_button.mp3"));
    }

    private ImageButton createHoverButton(Texture texture) {
        TextureRegionDrawable up = new TextureRegionDrawable(new TextureRegion(texture));
        Drawable hover = new TextureRegionDrawable(new TextureRegion(texture))
                .tint(new Color(1f, 1f, 1f, 0.5f)); // hover tint

        ImageButtonStyle style = new ImageButtonStyle();
        style.imageUp = up;
        style.imageOver = hover;

        ImageButton button = new ImageButton(style);
        button.setTransform(true); // allow scaling
        button.pack(); // shrink bounds to image
        return button;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 0.7f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(previousScreen);
            Gdx.input.setCursorCatched(true);
            dispose(); // fix ghost clicks when resuming
        }
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
        resumeTex.dispose();
        restartTex.dispose();
        exitTex.dispose();
    }
}
