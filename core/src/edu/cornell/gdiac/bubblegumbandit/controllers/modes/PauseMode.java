package edu.cornell.gdiac.bubblegumbandit.controllers.modes;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.util.ScreenListener;

public class PauseMode implements Screen {
    private Stage stage;

    private AssetDirectory internal;

    private TextureRegionDrawable background;

    private TextureRegion pointer;

    private TextButton resumeButton;

    private TextButton retryButton;

    private TextButton levelSelectButton;

    private TextButton settingsButton;

    private TextButton quitButton;

    private boolean resumeClicked;

    public boolean getResumeClicked() {return resumeClicked; }

    private boolean retryClicked;

    public boolean getRetryClicked() {return retryClicked; }


    private boolean levelSelectClicked;

    public boolean getLevelSelectClicked() {return levelSelectClicked; }


    private boolean settingsClicked;

    public boolean getSettingsClicked() {return settingsClicked; }


    private boolean quitClicked;

    public boolean getQuitClicked() {return quitClicked; }


    private boolean active;

    private Table pauseTable;

    private ScreenListener listener;

    private BitmapFont font;

    private TextButton.TextButtonStyle style;

    public final Color bubblegumPink = new Color(1, 149 / 255f, 138 / 255f, 1);

    public PauseMode() {
        internal = new AssetDirectory("jsons/pause.json");
        internal.loadAssets();
        internal.finishLoading();

        background = new TextureRegionDrawable(internal.getEntry("background", Texture.class));
        pointer = new TextureRegion(internal.getEntry("pointer", Texture.class));

        stage = new Stage();
        pauseTable = new Table();
        pauseTable.align(Align.topLeft);
        pauseTable.setFillParent(true);
        stage.addActor(pauseTable);
    }

    public void setViewport(Viewport view) {
        stage.setViewport(view);
        view.apply(true);
    }

    public void makePauseTable() {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle(null, null,
        null, this.font);

        resumeButton = new TextButton("Resume", style);
        retryButton = new TextButton("Retry", style);
        levelSelectButton = new TextButton("Level Select", style);
        settingsButton = new TextButton("Settings", style);
        quitButton = new TextButton("Quit", style);

        resumeButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {

                resumeClicked = true;
            }

            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {

                resumeButton.getLabel().setColor(bubblegumPink);
            }

            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                resumeButton.getLabel().setColor(Color.WHITE);
            }
        });

        retryButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {

                retryClicked = true;
            }

            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {

                retryButton.getLabel().setColor(bubblegumPink);
            }

            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                retryButton.getLabel().setColor(Color.WHITE);
            }
        });

        levelSelectButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {

                levelSelectClicked = true;
            }

            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {

                levelSelectButton.getLabel().setColor(bubblegumPink);
            }

            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                levelSelectButton.getLabel().setColor(Color.WHITE);
            }
        });

        settingsButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {

                settingsClicked = true;
            }

            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {

                settingsButton.getLabel().setColor(bubblegumPink);
            }

            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                settingsButton.getLabel().setColor(Color.WHITE);
            }
        });

        quitButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {

                quitClicked = true;
            }

            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {

                quitButton.getLabel().setColor(bubblegumPink);
            }

            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                quitButton.getLabel().setColor(Color.WHITE);
            }
        });

        pauseTable.row();
        pauseTable.add(resumeButton).pad(160, 160, 32, 455);
        pauseTable.row();
        pauseTable.add(retryButton).pad(0, 160, 32, 455);
        pauseTable.row();
        pauseTable.add(levelSelectButton).pad(0, 160, 32, 455);
        pauseTable.row();
        pauseTable.add(settingsButton).pad(0, 160, 32, 455);
        pauseTable.row();
        pauseTable.add(quitButton).pad(0, 160, 32, 455);
        pauseTable.row();

        for (Cell cell : pauseTable.getCells()) {
            cell.align(Align.left);
        }

        pauseTable.columnDefaults(1).setActorWidth(400);
        pauseTable.columnDefaults(1).fillX();
    }

    public void initialize(BitmapFont font) {
        if (this.font != null) {
            return;
        }
        this.font = font;
        //this.listener = listener;
        makePauseTable();
    }

    public void setScreenListener(ScreenListener listener) {this.listener = listener; }

    public void draw() {
        stage.getBatch().begin();
        stage.getBatch().draw(background.getRegion(), 0, 0, stage.getViewport().getWorldWidth(), stage.getViewport().getWorldHeight());

        stage.getBatch().end();
        stage.draw();
    }
    @Override
    public void show() {
        active = true;
        resumeClicked = false;
        retryClicked = false;
        levelSelectClicked = false;
        settingsClicked = false;
        quitClicked = false;
    }

    @Override
    public void render(float delta) {
        if (active) {
            stage.act();
            draw();
//            if (quitClicked) {
//                listener.exitScreen(this, 0);
//            }
        }
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        active = false;
    }

    @Override
    public void dispose() {
        internal.dispose();
    }
}
