package com.adomas.stormbreaker;

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
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class MainMenuScreen implements Screen {

    private final StormbreakerGame game;
    private Stage stage;
    private Skin skin;
    private Texture backgroundTexture;
    private Texture playTexture;
    private Texture exitTexture;
    private Sound clickSound;


    public MainMenuScreen(StormbreakerGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.internal("uiskin.json"));

        backgroundTexture = new Texture(Gdx.files.internal("Main.png"));
        playTexture = new Texture(Gdx.files.internal("playcrop.png"));
        exitTexture = new Texture(Gdx.files.internal("exitcrop.png"));

        Image background = new Image(new TextureRegionDrawable(backgroundTexture));
        background.setFillParent(true);
        stage.addActor(background);

        ImageButton playButton = createHoverButton(playTexture);
        ImageButton quitButton = createHoverButton(exitTexture);

        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.play();
                game.setScreen(new MainGameplayScreen(game));
                game.menuMusic.stop();
                dispose();
            }
        });

        quitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.play();
                game.menuMusic.stop();
                Gdx.app.exit(); 
            }
        });

        
        Table table = new Table();
        table.setFillParent(true);
        table.center();

        float screenHeight = Gdx.graphics.getHeight();
        float buttonHeight = screenHeight * 0.18f;
        float spacing = screenHeight * 0.03f;

        float playAspect = (float) playTexture.getWidth() / playTexture.getHeight();
        float exitAspect = (float) exitTexture.getWidth() / exitTexture.getHeight();

        float playWidth = buttonHeight * playAspect;
        float exitWidth = buttonHeight * exitAspect;

        table.add(playButton).width(playWidth).height(buttonHeight).padBottom(spacing).row();
        table.add(quitButton).width(exitWidth).height(buttonHeight);

        table.setTouchable(Touchable.childrenOnly); // Restrict inputs to the buttons only
        stage.addActor(table);

        clickSound = Gdx.audio.newSound(Gdx.files.internal("click_button.mp3"));
    }
// Method to create the tint effect
    private ImageButton createHoverButton(Texture texture) {
        TextureRegionDrawable up = new TextureRegionDrawable(new TextureRegion(texture));
        Drawable hover = new TextureRegionDrawable(new TextureRegion(texture))
                .tint(new Color(1f, 1f, 1f, 0.5f)); // Hover tint

        ImageButtonStyle style = new ImageButtonStyle();
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
        backgroundTexture.dispose();
        playTexture.dispose();
        exitTexture.dispose();
    }
}
