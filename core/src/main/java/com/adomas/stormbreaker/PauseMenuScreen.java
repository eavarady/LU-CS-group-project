package com.adomas.stormbreaker;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class PauseMenuScreen implements Screen {

    private final StormbreakerGame game;
    private final Screen previousScreen;

    private Stage stage;
    private Skin skin;

    public PauseMenuScreen(StormbreakerGame game, Screen previousScreen) {
        this.game = game;
        this.previousScreen = previousScreen; // come back to the game
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.internal("uiskin.json")); 
        BitmapFont font = new BitmapFont(
            Gdx.files.internal("default.fnt"), 
            Gdx.files.internal("default.png"), 
            false
        );

        if (skin.has("default-font", BitmapFont.class)) {
            skin.remove("default-font", BitmapFont.class);
        }
        skin.add("default-font", font);

        TextButton.TextButtonStyle textButtonStyle = skin.get("default", TextButton.TextButtonStyle.class);
        textButtonStyle.font = font;
        skin.add("default", textButtonStyle);

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Menu buttons
        TextButton resumeButton = new TextButton("RESUME", skin);
        TextButton restartButton = new TextButton("RESTART", skin);
        TextButton exitButton = new TextButton("EXIT", skin);

        // Listeners
        resumeButton.addListener(e -> {
            if (resumeButton.isPressed()) {
                game.setScreen(previousScreen); 
                Gdx.input.setCursorCatched(true);
                return true;
            }
            return false;
        });

        restartButton.addListener(e -> {
            if (restartButton.isPressed()) {
                game.setScreen(new TestLevelScreen(game)); // restart level
                return true;
            }
            return false;
        });

        exitButton.addListener(e -> {
            if (exitButton.isPressed()) {
                game.setScreen(new MainMenuScreen(game)); // Main Menu screen
                return true;
            }
            return false;
        });

        table.add(resumeButton).pad(10).row();
        table.add(restartButton).pad(10).row();
        table.add(exitButton).pad(10);
        table.setDebug(true);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 0.7f); // opaque background
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
        skin.dispose();
    }
}
