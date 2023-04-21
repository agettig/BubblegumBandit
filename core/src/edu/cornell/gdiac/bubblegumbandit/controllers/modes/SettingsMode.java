package edu.cornell.gdiac.bubblegumbandit.controllers.modes;


import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.controllers.SoundController;
import edu.cornell.gdiac.util.ScreenListener;
import org.w3c.dom.Text;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

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

    private TextButton backButtonSettings;

    private TextButton strafeLeftButton;

    private TextButton strafeRightButton;

    private TextButton toggleGravityUpButton;

    private TextButton toggleGravityDownButton;

    private TextButton toggleMinimapButton;

    private TextButton unstickGumButton;

    private TextButton shootGumButton;

    private TextButton controlsButton;

    private TextButton controlsBackButton;

    private boolean backButtonClicked;


    /**
     * Whether or not this player mode is still active
     */
    private boolean active;

    /** The container for laying out all HUD elements */
    private Table settingsTable;

    private Table controlsTable;

    private ScreenListener listener;

    private BitmapFont labelFont;

    private BitmapFont titleFont;

    private Slider musicSlider;
    private Slider soundEffectsSlider;

    private Drawable sliderTexture;

    private Drawable sliderKnob;


    private Drawable sliderBeforeKnob;

    private Label.LabelStyle settingsStyle;

    private Label.LabelStyle labelStyle;

    public final Color bubblegumPink = new Color(1, 149/255f, 138/255f, 1);

    public final Color spaceBlue = new Color(55/255f, 226/255f, 226/255f, 1);

    private String keyPressed;

    private ArrayList<TextButton> inputButtons;

    public SettingsMode() {
        internal = new AssetDirectory("jsons/settings.json");
        internal.loadAssets();
        internal.finishLoading();

        Texture slider= internal.getEntry("sliderTexture", Texture.class);
        Texture knob = internal.getEntry("knob", Texture.class);
        Texture sliderBeforeKnobTexture = internal.getEntry("sliderBeforeKnob", Texture.class);


        background = new TextureRegionDrawable(internal.getEntry("background", Texture.class));
        sliderBeforeKnob = new TextureRegionDrawable(new TextureRegion(slider));
        sliderKnob = new TextureRegionDrawable(new TextureRegion(knob));
        sliderTexture = new TextureRegionDrawable(sliderBeforeKnobTexture);


        stage = new Stage();
        settingsTable = new Table();
        settingsTable.align(Align.center);
        controlsTable = new Table();
        controlsTable.align(Align.center);
        settingsTable.setFillParent(true);
        controlsTable.setFillParent(true);
        stage.addActor(settingsTable);
        settingsTable.debug();
        keyPressed = "";
        inputButtons = new ArrayList<>();
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


    public void makeSettingsTable(){
        settingsStyle = new Label.LabelStyle(this.titleFont, spaceBlue);
        labelStyle = new Label.LabelStyle(this.labelFont, Color.WHITE);
        Label settings = new Label("Settings", settingsStyle);
        Label music = new Label("Music", labelStyle);
        Label soundEffects = new Label("Sound Effects", labelStyle);

        createSliders();

        TextButton.TextButtonStyle backButtonStyle = new TextButton.TextButtonStyle(null, null, null, this.titleFont);

        backButtonSettings = new TextButton("Back",backButtonStyle);
        backButtonSettings.getLabel().setFontScale(.5f);

        backButtonSettings.addListener(new ClickListener(){
            public void clicked(InputEvent event, float x, float y){
                System.out.println("clicked");
                backButtonClicked = true;
            }
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {

                backButtonSettings.getLabel().setColor(bubblegumPink);
            }

            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                backButtonSettings.getLabel().setColor(Color.WHITE);
            }
        });

        TextButton.TextButtonStyle controlsButtonStyle = new TextButton.TextButtonStyle(null, null, null, this.labelFont);

        controlsButton = new TextButton("Controls", controlsButtonStyle);
        controlsButton.addListener(new ClickListener(){
            public void clicked(InputEvent event, float x, float y){
                stage.clear();
                stage.addActor(controlsTable);
            }
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {

                controlsButton.getLabel().setColor(bubblegumPink);
            }

            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                controlsButton.getLabel().setColor(Color.WHITE);
            }
        });


        settingsTable.row();
        settingsTable.add(settings);
        settingsTable.row();
        settingsTable.add(music);
        settingsTable.add(musicSlider).width(460);
        settingsTable.row();
        settingsTable.add(soundEffects);
        settingsTable.add(soundEffectsSlider).width(460);
        settingsTable.row();
        settingsTable.add(controlsButton);
        settingsTable.row();
        settingsTable.add(backButtonSettings);
        settingsTable.row();

        for (Cell cell: settingsTable.getCells()){
            cell.align(Align.left);
            if (cell.getColumn()==0){
                cell.pad(10,10,10,100);
            }
            else{
                cell.pad(10);
            }
        }
        settingsTable.columnDefaults(1).setActorWidth(400);
        settingsTable.columnDefaults(1).fillX();
    }

    private void makeControlsTable() {
        Label controls = new Label("Controls", settingsStyle);
        Label strafeLeft = new Label("Strafe Left", labelStyle);
        Label strafeRight = new Label("Strafe Right", labelStyle);
        Label toggleGravityUp = new Label("Toggle Gravity Up", labelStyle);
        Label toggleGravityDown = new Label("Toggle Gravity Down", labelStyle);
        Label shootGum = new Label("Shoot Gum", labelStyle);
        Label unstickGum = new Label("Unstick Gum", labelStyle);
        Label toggleMinimap = new Label("Toggle Minimap", labelStyle);
        TextButton.TextButtonStyle backButtonStyle = new TextButton.TextButtonStyle(null, null, null, this.titleFont);

        TextButton.TextButtonStyle controlsButtonStyle = new TextButton.TextButtonStyle(null, null, null, this.labelFont);
        controlsButtonStyle.checkedFontColor = bubblegumPink;



        strafeLeftButton = new TextButton("A", controlsButtonStyle);
        strafeLeftButton.addListener(new ClickListener(){
            public void clicked(InputEvent event, float x, float y){

            }
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {

                strafeLeftButton.getLabel().setColor(bubblegumPink);
            }

            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                strafeLeftButton.getLabel().setColor(Color.WHITE);
            }
        });

        strafeRightButton = new TextButton("D", controlsButtonStyle);
        strafeRightButton.addListener(new ClickListener(){
            public void clicked(InputEvent event, float x, float y){

            }
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {

                strafeRightButton.getLabel().setColor(bubblegumPink);
            }

            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                strafeRightButton.getLabel().setColor(Color.WHITE);
            }
        });

        toggleGravityUpButton = new TextButton("W", controlsButtonStyle);
        toggleGravityUpButton.addListener(new ClickListener(){
            public void clicked(InputEvent event, float x, float y){

            }
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {

                toggleGravityUpButton.getLabel().setColor(bubblegumPink);
            }

            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                toggleGravityUpButton.getLabel().setColor(Color.WHITE);
            }
        });

        toggleGravityDownButton = new TextButton("W", controlsButtonStyle);
        toggleGravityDownButton.addListener(new ClickListener(){
            public void clicked(InputEvent event, float x, float y){

            }
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {

                toggleGravityDownButton.getLabel().setColor(bubblegumPink);
            }

            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                toggleGravityDownButton.getLabel().setColor(Color.WHITE);
            }
        });

        toggleMinimapButton = new TextButton("SHIFT", controlsButtonStyle);
        toggleMinimapButton.addListener(new ClickListener(){
            public void clicked(InputEvent event, float x, float y){

            }
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {

                toggleMinimapButton.getLabel().setColor(bubblegumPink);
            }

            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                toggleMinimapButton.getLabel().setColor(Color.WHITE);
            }
        });

        shootGumButton = new TextButton("LEFT CLICK", controlsButtonStyle);

        shootGumButton.addListener(new ClickListener(){


            public void clicked(InputEvent event, float x, float y){
                for (TextButton button: inputButtons){
                    if (button != shootGumButton){
                        button.setChecked(false);
                    }
                }
            }
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {

//                shootGumButton.getLabel().setColor(bubblegumPink);
            }

            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
//                shootGumButton.getLabel().setColor(Color.WHITE);
            }
        });
        unstickGumButton = new TextButton("RIGHT CLICK", controlsButtonStyle);
        unstickGumButton.addListener(new ClickListener(){


            public void clicked(InputEvent event, float x, float y){
                for (TextButton button: inputButtons){
                    if (button != unstickGumButton){
                        button.setChecked(false);
                    }
                }
            }
        });

        controlsBackButton = new TextButton("Back", backButtonStyle);
        controlsBackButton.getLabel().setFontScale(.5f);

        controlsBackButton.addListener(new ClickListener(){
            public void clicked(InputEvent event, float x, float y){
                stage.clear();
                stage.addActor(settingsTable);
            }
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {

                controlsBackButton.getLabel().setColor(bubblegumPink);
            }

            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                controlsBackButton.getLabel().setColor(Color.WHITE);
            }
        });


        inputButtons.add(strafeLeftButton);
        inputButtons.add(strafeRightButton);
        inputButtons.add(toggleGravityUpButton);
        inputButtons.add(toggleGravityDownButton);
        inputButtons.add(toggleMinimapButton);
        inputButtons.add(shootGumButton);
        inputButtons.add(unstickGumButton);

        controlsTable.row();
        controlsTable.add(controls);
        controlsTable.row();
        controlsTable.add(strafeLeft);
        controlsTable.add(strafeLeftButton);
        controlsTable.row();
        controlsTable.add(strafeRight);
        controlsTable.add(strafeRightButton);
        controlsTable.row();
        controlsTable.add(toggleGravityUp);
        controlsTable.add(toggleGravityUpButton);
        controlsTable.row();
        controlsTable.add(toggleGravityDown);
        controlsTable.add(toggleGravityDownButton);
        controlsTable.row();
        controlsTable.add(toggleMinimap);
        controlsTable.add(toggleMinimapButton);
        controlsTable.row();
        controlsTable.add(shootGum);
        controlsTable.add(shootGumButton);
        controlsTable.row();
        controlsTable.add(unstickGum);
        controlsTable.add(unstickGumButton);
        controlsTable.row();
        controlsTable.add(controlsBackButton);

        for (Cell cell: controlsTable.getCells()){
            cell.align(Align.left);
            if (cell.getColumn()==0){
                cell.pad(10,10,10,100);
            }
            else{
                cell.pad(10);
            }
        }
    }

    public void initialize(BitmapFont labelFont, BitmapFont projectSpace){
        if (this.labelFont != null) {
            stage.clear();
            stage.addActor(settingsTable);
            return;
        }
        this.labelFont = labelFont;
        this.titleFont = projectSpace;
        makeSettingsTable();
        makeControlsTable();

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
        SettingsInputProcessor settingsInputProcessor = new SettingsInputProcessor();
        InputMultiplexer inputMultiplexer = new InputMultiplexer(stage, settingsInputProcessor);
        Gdx.input.setInputProcessor(inputMultiplexer);
    }


    @Override
    public void render(float delta) {
        if (active) {
            stage.act();
            stage.getBatch().begin();
            stage.getBatch().draw(background.getRegion(), 0, 0, stage.getWidth(), stage.getHeight());
            stage.getBatch().end();
            keyPressed = "";
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

    public class SettingsInputProcessor extends InputAdapter {
        @Override
        public boolean keyUp(int keycode) {
            for (TextButton inputButton: inputButtons){
                if(inputButton.isChecked()){
                    keyPressed = Input.Keys.toString(keycode);
                    inputButton.setText(keyPressed.toUpperCase());
                }
            }
            return true;
        }

        @Override
        public boolean touchUp (int screenX, int screenY, int pointer, int button) {

            for (TextButton inputButton: inputButtons){
                if (inputButton.isChecked()){
                    if (button == Input.Buttons.LEFT){
                        inputButton.setText("LEFT CLICK");
                    } else if (button == Input.Buttons.RIGHT) {
                        inputButton.setText("RIGHT CLICK");
                    }
                }
            }
            return true;
        }
    }

}
