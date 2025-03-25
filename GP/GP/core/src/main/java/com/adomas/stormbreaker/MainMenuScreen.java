package com.adomas.stormbreaker;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class MainMenuScreen implements Screen {

    private final StormbreakerGame game;
    private Stage stage;
    private Skin skin;

    public MainMenuScreen(StormbreakerGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.internal("uiskin.json")); 
        BitmapFont font = skin.getFont("default-font");
        System.out.println("Font line height: " + font.getLineHeight());

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        for (Object key : skin.getAll(BitmapFont.class).keys()) {
            System.out.println("Font: " + key);
        }
        System.out.println("Available TextButton styles: " + skin.getAll(TextButton.TextButtonStyle.class).keys());

        TextButton playButton = new TextButton("Play", skin, "default");
        TextButton quitButton = new TextButton("Quit", skin, "default");

        playButton.addListener(e -> {
            if (playButton.isPressed()) {
                // game.setScreen(new GameplayScreen(game)); // TBI
                Gdx.app.exit(); // temporary: exits the game instead of starting gameplay
                return true;
            }
            return false;
        });

        quitButton.addListener(e -> {
            if (quitButton.isPressed()) {
                Gdx.app.exit();
                return true;
            }
            return false;
        });

        table.add(playButton).pad(10).row();
        table.add(quitButton).pad(10);

        Label.LabelStyle debugStyle = new Label.LabelStyle();
        // debugStyle.font = skin.getFont("default-font");
        debugStyle.font = font;  // our scaled-up font
        debugStyle.fontColor = com.badlogic.gdx.graphics.Color.RED;
        Label testLabel = new Label("Hello from MainMenu", debugStyle);

        table.row();
        table.add(testLabel).pad(10);
        table.setDebug(true);
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

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}

