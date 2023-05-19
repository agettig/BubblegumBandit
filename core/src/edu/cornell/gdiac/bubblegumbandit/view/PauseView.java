package edu.cornell.gdiac.bubblegumbandit.view;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.controllers.BubblegumController;
import edu.cornell.gdiac.bubblegumbandit.controllers.modes.Screens;
import edu.cornell.gdiac.bubblegumbandit.models.level.LevelModel;
import edu.cornell.gdiac.util.ScreenListener;

public class PauseView {

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

    public boolean getResumeClicked() {
        return resumeClicked;
    }

    private boolean retryClicked;

    public boolean getRetryClicked() {
        return retryClicked;
    }


    private boolean levelSelectClicked;

    public boolean getLevelSelectClicked() {
        return levelSelectClicked;
    }


    private boolean settingsClicked;

    public boolean getSettingsClicked() {
        return settingsClicked;
    }


    private boolean quitClicked;

    public boolean getQuitClicked() {
        return quitClicked;
    }


    private Table pauseTable;

    private ScreenListener listener;

    private BitmapFont font;

    private ShapeRenderer shape;

    private TextButton.TextButtonStyle style;

    public final Color bubblegumPink = new Color(1, 149 / 255f, 138 / 255f, 1);

    public final Color pressTint = new Color(70 / 255f, 153 / 255f, 167 / 255f, 1);

    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }

    public void resizeViewport(int width, int height) {
        stage.getViewport().update(width, height);
    }

    public PauseView() {
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

        shape = new ShapeRenderer();

    }


    public void makePauseTable() {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle(null, null,
                null, this.font);
        style.overFontColor = bubblegumPink;
        style.fontColor = Color.WHITE;
        style.downFontColor = pressTint;
        ;

        resumeButton = new TextButton("Resume", style);
        retryButton = new TextButton("Retry", style);
        settingsButton = new TextButton("Settings", style);
        levelSelectButton = new TextButton("Exit to Level Select", style);
        quitButton = new TextButton("Quit to Title", style);

        resumeButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {

                resumeClicked = true;
            }

//      public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
//        resumeButton.getLabel().setColor(bubblegumPink);
//      }
//
//      public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
//        resumeButton.getLabel().setColor(Color.WHITE);
//      }
        });

        retryButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                retryClicked = true;
            }
        });

        levelSelectButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {

                levelSelectClicked = true;
            }

        });

        settingsButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {

                settingsClicked = true;
            }
        });

        quitButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {

                quitClicked = true;
            }
        });

        pauseTable.row();
        pauseTable.add(resumeButton).pad(160, 160, 32, 455);
        pauseTable.row();
        pauseTable.add(retryButton).pad(0, 160, 32, 455);
        pauseTable.row();
        pauseTable.add(settingsButton).pad(0, 160, 32, 455);
        pauseTable.row();
        pauseTable.add(levelSelectButton).pad(0, 160, 32, 455);
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
        assert !(this.font == null);

        this.font = font;
        makePauseTable();
    }

    public void show() {
        resumeClicked = false;
        retryClicked = false;
        levelSelectClicked = false;
        settingsClicked = false;
        quitClicked = false;
        Gdx.input.setInputProcessor(stage);
    }

    public void update() {
        stage.act();
    }

    public void setViewport(Viewport view) {
        stage.setViewport(view);
    }

    public boolean hasViewport() {
        return stage.getViewport() != null;
    }

    public void draw() {
        stage.getViewport().apply();
        stage.getBatch().begin();
        //stage.getBatch().draw(background.getRegion(), 0, 0, stage.getViewport().getWorldWidth(), stage.getViewport().getWorldHeight());

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(new Color(0, 0, 0, 0.5f));
        shape.rect(0, 0, stage.getViewport().getWorldWidth(), stage.getViewport().getWorldHeight());
        shape.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        stage.getBatch().end();

        stage.draw();
    }

    public void dispose() {
        internal.unloadAssets();
        internal.dispose();
//        stage.dispose();
    }
}