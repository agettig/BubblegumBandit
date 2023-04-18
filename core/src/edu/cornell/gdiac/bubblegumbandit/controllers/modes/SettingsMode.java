package edu.cornell.gdiac.bubblegumbandit.controllers.modes;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.controllers.SoundController;
import edu.cornell.gdiac.util.ScreenListener;

public class SettingsMode implements Screen{

    /** The stage, contains all HUD elements */
    private Stage stage;

    /**
     * Internal assets for this settings screen
     */
    private AssetDirectory internal;

    /**
     * Background texture for settings
     */
    private TextureRegionDrawable background;

    /**
     * Label for controls
     */
    private Texture controlsLabel;

    /**
     * Label for music controls
     */
    private Texture musicLabel;

    /**
     * Label for sound effects
     */
    private Texture soundEffectsLabel;

    /**
     * Label for display mode
     */
    private Texture displayModeLabel;

    private TextButton backButton;

    private boolean backButtonClicked;


    /**
     * Whether or not this player mode is still active
     */
    private boolean active;

    /** The container for laying out all HUD elements */
    private Table table;

    private ScreenListener listener;

    private BitmapFont font;

    private Slider musicSlider;
    private Slider soundEffectsSlider;

    private Drawable sliderTexture;

    private Drawable sliderKnob;


    private Drawable sliderBeforeKnob;

    public SettingsMode() {
        internal = new AssetDirectory("jsons/settings.json");
        internal.loadAssets();
        internal.finishLoading();

        Texture slider= internal.getEntry("sliderTexture", Texture.class);
        Texture knob = internal.getEntry("knob", Texture.class);
        Texture sliderBeforeKnobTexture = internal.getEntry("sliderBeforeKnob", Texture.class);


        background = new TextureRegionDrawable(internal.getEntry("background", Texture.class));
        sliderTexture = new TextureRegionDrawable(new TextureRegion(slider));
        sliderKnob = new TextureRegionDrawable(new TextureRegion(knob));
        sliderBeforeKnob = new TextureRegionDrawable(sliderBeforeKnobTexture);


        stage = new Stage();
        table = new Table();
        table.align(Align.center);
        stage.addActor(table);
        table.setFillParent(true);
    }
    public void setViewport(Viewport view) {
        stage.setViewport(view);
        view.apply(true);
    }

    public void createSliders(){
        Slider.SliderStyle sliderStyle = new Slider.SliderStyle(sliderTexture, sliderKnob);
        sliderStyle.knobBefore = sliderBeforeKnob;

        musicSlider = new Slider(0, 1, .05f, false, sliderStyle);
        musicSlider.sizeBy(2,1);
        musicSlider.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundController.getInstance().setMusicVolume(musicSlider.getValue());
            }
        });
        musicSlider.setValue(.5f);

        soundEffectsSlider = new Slider(0, 1, .05f, false, sliderStyle);
        soundEffectsSlider.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundController.getInstance().setEffectsVolume(soundEffectsSlider.getValue());
            }
        });
        soundEffectsSlider.setValue(.5f);
    }


    public void initialize(BitmapFont f){
        if (this.font != null) return;
        font = f;
        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);
        Label controls = new Label("Controls", labelStyle);
        Label music = new Label("Music", labelStyle);
        Label soundEffects = new Label("Sound Effects", labelStyle);
        Label displayMode = new Label("Display Mode", labelStyle);

        createSliders();


        backButton = new TextButton("Back", new TextButton.TextButtonStyle(null, null, null, this.font));
        backButton.addListener(new ClickListener(){
            public void clicked(InputEvent event, float x, float y){
                System.out.println("clicked");
                backButtonClicked = true;
            }
        });

        table.row();
        table.add(controls);
        table.row();
        table.add(music);
        table.add(musicSlider).width(450);
        table.row();
        table.add(soundEffects);
        table.add(soundEffectsSlider).width(450);
        table.row();
        table.add(backButton);
        table.row();

        for (Cell cell: table.getCells()){
            cell.align(Align.left);
            if (cell.getColumn()==0){
                cell.pad(10,10,10,100);
            }
            else{
                cell.pad(10);
            }
        }
        table.columnDefaults(1).setActorWidth(400);
        table.columnDefaults(1).fillX();
    }
    /**
     * Sets the ScreenListener for this mode
     * <p>
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }


    public void draw() {
        stage.draw();}

    @Override
    public void show() {
        active = true;
        backButtonClicked = false;
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        if (active) {
            stage.getBatch().begin();
            stage.getBatch().draw(background.getRegion(), 0, 0, stage.getWidth(), stage.getHeight());
            stage.getBatch().end();
            draw();
            if (backButtonClicked) {
                listener.exitScreen(this, 0);
            }
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
