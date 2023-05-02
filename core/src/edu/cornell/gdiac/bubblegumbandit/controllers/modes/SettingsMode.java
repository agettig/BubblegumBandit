package edu.cornell.gdiac.bubblegumbandit.controllers.modes;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.controllers.PlayerController;
import edu.cornell.gdiac.bubblegumbandit.controllers.SoundController;
import edu.cornell.gdiac.bubblegumbandit.helpers.SaveData;
import edu.cornell.gdiac.util.ScreenListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SettingsMode implements Screen {

    /**
     * The stage, contains all settings elements
     */
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
     * Hover arrow texture
     */
    private TextureRegion arrow;

    /**
     * Back button to main menu
     */
    private TextButton backButtonSettings;

    /**
     * Move left button
     */
    private TextButton moveLeftButton;

    /**
     * Move right button
     */
    private TextButton moveRightButton;

    /**
     * Toggle gravity up button
     */
    private TextButton toggleGravityUpButton;

    /**
     * Toggle gravity down button
     */
    private TextButton toggleGravityDownButton;

    /**
     * Toggle minimap button
     */
    private TextButton toggleMinimapButton;

//    /**
//     * Unstick gum button
//     */
//    private TextButton unstickGumButton;

    /**
     * Shoot gum button
     */
    private SelectBox shootGumSelect;


    private SelectBox unstickGumSelect;

    /**
     * Controls button
     * Takes the player to controls settings screen
     */
    private TextButton controlsButton;

    /**
     * Default button
     * Resets controls to default values
     */
    private TextButton defaultButton;

    /**
     * Back button on controls screen
     * Returns the player to main settings screen
     */
    private TextButton controlsBackButton;

    /**
     * Reload gum button
     */
    private TextButton reloadGumButton;

    /**
     * Whether back butotn to main menu was clicked
     */
    private boolean backButtonClicked;


    /**
     * Whether this player mode is still active
     */
    private boolean active;

    /**
     * The container for laying out main settings elements
     */
    private Table settingsTable;

    /**
     * The container for laying out controls elements
     */
    private Table controlsTable;

    /**
     * Screen listener
     */
    private ScreenListener listener;

    /**
     * Font used for labels (Codygoon)
     */
    private BitmapFont labelFont;

    /**
     * Font used for title (Space Project)
     */
    private BitmapFont titleFont;

    /**
     * Slider for music volume
     */
    private Slider musicSlider;

    /**
     * Slider for sound effects volume
     */
    private Slider soundEffectsSlider;

    /**
     * Texture for filling slider (blue)
     */
    private Drawable sliderTexture;

    /**
     * Texture for slider knob
     */
    private Drawable sliderKnob;

    /**
     * Texture for not filled slider (white)
     */
    private Drawable sliderBeforeKnob;


    /**
     * Style used for headings
     */
    private Label.LabelStyle headingsStyle;

    private ScrollPane scrollPane;

    /**
     * Style used for labels
     */
    private Label.LabelStyle labelStyle;

    /**
     * Bubblegum Pink color
     * used for hover
     */
    public final Color bubblegumPink = new Color(1, 149 / 255f, 138 / 255f, 1);

    /**
     * Space Blue color
     * used for headings
     */
    public final Color spaceBlue = new Color(55 / 255f, 226 / 255f, 226 / 255f, 1);

    /**
     * Button that is currently selected
     * Note: can be null
     */
    private TextButton checkedButton;

    /**
     * Maps buttons to indices in arrays
     */
    private HashMap<Object, Integer> buttonIndexMap = new HashMap<>();

    private TextureRegionDrawable backgroundButton;

    /**
     * Input multiplexer
     * stage and settings input processor
     */
    private InputMultiplexer inputMultiplexer;

    /**
     * Array of booleans represented if buttons are hovered over
     */
    private boolean[] hoverBooleans = new boolean[]{false, false, false, false, false, false, false, false};

    /**
     * Values representing the keys and buttons pressed for each user control
     */
    private int[] values;

    private final int[] defaultVals = new int[]{
            Input.Keys.A,
            Input.Keys.D,
            Input.Keys.SPACE,
            Input.Keys.SPACE,
            Input.Keys.SHIFT_LEFT,
            Input.Keys.R,
            Input.Buttons.LEFT,
            Input.Buttons.RIGHT
    };

    /**
     * Contructor for making a settings mode
     */
    public SettingsMode() {
        internal = new AssetDirectory("jsons/settings.json");
        internal.loadAssets();
        internal.finishLoading();

        Texture slider = internal.getEntry("sliderTexture", Texture.class);
        Texture knob = internal.getEntry("knob", Texture.class);
        Texture sliderBeforeKnobTexture = internal.getEntry("sliderBeforeKnob", Texture.class);
        Texture buttonBackground = internal.getEntry("backgroundButton", Texture.class);

        background = new TextureRegionDrawable(internal.getEntry("background", Texture.class));
        sliderBeforeKnob = new TextureRegionDrawable(new TextureRegion(slider));
        sliderKnob = new TextureRegionDrawable(new TextureRegion(knob));
        sliderTexture = new TextureRegionDrawable(sliderBeforeKnobTexture);
        arrow = new TextureRegion(internal.getEntry("arrow", Texture.class));
        backgroundButton = new TextureRegionDrawable(buttonBackground);



        stage = new Stage();
        settingsTable = new Table();
        settingsTable.align(Align.topLeft);
        controlsTable = new Table();
        controlsTable.align(Align.topLeft);
        settingsTable.setFillParent(true);
        controlsTable.setFillParent(true);
        stage.addActor(settingsTable);
        values = SaveData.getKeyBindings();

        SettingsInputProcessor settingsInputProcessor = new SettingsInputProcessor();
        inputMultiplexer = new InputMultiplexer(stage, settingsInputProcessor);
    }

    public void resetDefaultBindings() {
        for (int i = 0; i < defaultVals.length; i++) {
            values[i] = defaultVals[i];
        }

        for (Map.Entry<Object, Integer> entry : buttonIndexMap.entrySet()) {
            int index = entry.getValue();
            int value = values[index];
            if (index < 6) {
                ((TextButton) entry.getKey()).setText(Input.Keys.toString(value).toUpperCase());
            }
        }

        unstickGumSelect.setSelectedIndex(0);
        shootGumSelect.setSelectedIndex(1);
    }

    /**
     * Set viewport
     */
    public void setViewport(Viewport view) {
        stage.setViewport(view);
        view.apply(true);
    }


    /**
     * Create music and sound effects sliders
     */
    public void createSliders() {
        Slider.SliderStyle sliderStyle = new Slider.SliderStyle(sliderTexture, sliderKnob);
        sliderStyle.knobBefore = sliderBeforeKnob;

        musicSlider = new Slider(0, 1, .05f, false, sliderStyle);
        musicSlider.sizeBy(2, 1);
        musicSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundController.getInstance().setMusicVolume(musicSlider.getValue());
            }
        });
        musicSlider.setValue(SaveData.getMusicVolume());

        soundEffectsSlider = new Slider(0, 1, .05f, false, sliderStyle);
        soundEffectsSlider.setValue(SaveData.getSFXVolume());
    }


    /**
     * Make settings table
     */
    public void makeSettingsTable() {
        headingsStyle = new Label.LabelStyle(this.titleFont, spaceBlue);
        labelStyle = new Label.LabelStyle(this.labelFont, Color.WHITE);
        Label settings = new Label("Settings", headingsStyle);
        Label music = new Label("Music", labelStyle);
        Label soundEffects = new Label("Sound Effects", labelStyle);

        createSliders();

        TextButton.TextButtonStyle backButtonStyle = new TextButton.TextButtonStyle(null,
                null, null, this.titleFont);

        backButtonSettings = new TextButton("Back", backButtonStyle);
        backButtonSettings.getLabel().setFontScale(.5f);

        backButtonSettings.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
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
        controlsButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
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
        settingsTable.add(settings).pad(100, 100, 40, 0);
        settingsTable.row();
        settingsTable.add(music).pad(0, 160, 32, 455);
        settingsTable.add(musicSlider).width(460).pad(0, 0, 32, 0);
        settingsTable.row();
        settingsTable.add(soundEffects).pad(0, 160, 32, 295);
        ;
        settingsTable.add(soundEffectsSlider).width(460).pad(0, 0, 32, 0);
        ;
        settingsTable.row();
        settingsTable.add(controlsButton).pad(0, 160, 40, 0);
        ;
        settingsTable.row();
        settingsTable.add(backButtonSettings).pad(0, 100, 0, 0);
        settingsTable.row();

        for (Cell cell : settingsTable.getCells()) {
            cell.align(Align.left);
        }
        settingsTable.columnDefaults(1).setActorWidth(400);
        settingsTable.columnDefaults(1).fillX();
    }

    /**
     * Add listeners to controls buttons
     */
    private void addControlButtonListeners() {
        moveLeftButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (checkedButton != null) {
                    checkedButton.setChecked(false);
                }
                if (checkedButton == moveLeftButton) {
                    checkedButton = null;
                } else {
                    checkedButton = moveLeftButton;
                }
            }

            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                hoverBooleans[buttonIndexMap.get(moveLeftButton)] = true;
            }

            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                hoverBooleans[buttonIndexMap.get(moveLeftButton)] = false;
            }
        });
        moveRightButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (checkedButton != null) {
                    checkedButton.setChecked(false);
                }
                if (checkedButton == moveRightButton) {
                    checkedButton = null;
                } else {
                    checkedButton = moveRightButton;
                }
            }

            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                hoverBooleans[buttonIndexMap.get(moveRightButton)] = true;
            }

            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                hoverBooleans[buttonIndexMap.get(moveRightButton)] = false;
            }
        });
        toggleGravityUpButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (checkedButton != null) {
                    checkedButton.setChecked(false);
                }
                if (checkedButton == toggleGravityUpButton) {
                    checkedButton = null;
                } else {
                    checkedButton = toggleGravityUpButton;
                }
            }

            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {

                hoverBooleans[buttonIndexMap.get(toggleGravityUpButton)] = true;
            }

            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                hoverBooleans[buttonIndexMap.get(toggleGravityUpButton)] = false;
            }
        });
        toggleGravityDownButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (checkedButton != null) {
                    checkedButton.setChecked(false);
                }
                if (checkedButton == toggleGravityDownButton) {
                    checkedButton = null;
                } else {
                    checkedButton = toggleGravityDownButton;
                }
            }

            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {

                hoverBooleans[buttonIndexMap.get(toggleGravityDownButton)] = true;
            }

            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                hoverBooleans[buttonIndexMap.get(toggleGravityDownButton)] = false;
            }
        });

        toggleMinimapButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (checkedButton != null) {
                    checkedButton.setChecked(false);
                }
                if (checkedButton == toggleMinimapButton) {
                    checkedButton = null;
                } else {
                    checkedButton = toggleMinimapButton;
                }
            }

            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {

                hoverBooleans[buttonIndexMap.get(toggleMinimapButton)] = true;
            }

            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                hoverBooleans[buttonIndexMap.get(toggleMinimapButton)] = false;
            }
        });
//        shootGumButton.addListener(new ClickListener() {
//            public void clicked(InputEvent event, float x, float y) {
//
//                if (checkedButton != shootGumButton)
//                    checkedButton = shootGumButton;
//                else {
//                    checkedButton.setText("LEFT CLICK");
//                    values[buttonIndexMap.get(checkedButton)] = Input.Buttons.LEFT;
//                    checkedButton.setChecked(false);
//                    checkedButton = null;
//                }
//
//
//            }
//
//            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
//                hoverBooleans[buttonIndexMap.get(shootGumButton)] = true;
//            }
//
//            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
//                hoverBooleans[buttonIndexMap.get(shootGumButton)] = false;
//            }
//        });
//        unstickGumButton.addListener(new ClickListener() {
//            public void clicked(InputEvent event, float x, float y) {
//
//                if (checkedButton != unstickGumButton)
//                    checkedButton = unstickGumButton;
//                else {
//                    checkedButton.setText("LEFT CLICK");
//                    values[buttonIndexMap.get(checkedButton)] = Input.Buttons.LEFT;
//                    checkedButton.setChecked(false);
//                    checkedButton = null;
//                }
//
//            }
//
//            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
//                hoverBooleans[buttonIndexMap.get(unstickGumButton)] = true;
//            }
//
//            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
//                hoverBooleans[buttonIndexMap.get(unstickGumButton)] = false;
//            }
//        });
        reloadGumButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (checkedButton != null) {
                    checkedButton.setChecked(false);
                }
                if (checkedButton == reloadGumButton) {
                    checkedButton = null;
                } else {
                    checkedButton = reloadGumButton;
                }
            }

            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {

                hoverBooleans[buttonIndexMap.get(reloadGumButton)] = true;
            }

            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                hoverBooleans[buttonIndexMap.get(reloadGumButton)] = false;
            }
        });
        controlsBackButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                stage.clear();
                stage.addActor(settingsTable);
                for (Object o: buttonIndexMap.keySet()){
                    if (o instanceof TextButton){
                        ((TextButton) o).setChecked(false);
                    }
                }
            }

            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {

                controlsBackButton.getLabel().setColor(bubblegumPink);
            }

            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                controlsBackButton.getLabel().setColor(Color.WHITE);
            }
        });

        defaultButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                resetDefaultBindings();
            }

            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {

                defaultButton.getLabel().setColor(bubblegumPink);
            }

            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                defaultButton.getLabel().setColor(Color.WHITE);
            }
        });

    }


    /**
     * Make controls table
     */
    private void makeControlsTable() {
        Label controls = new Label("Controls", headingsStyle);
        Label moveLeft = new Label("Move Left", labelStyle);
        Label moveRight = new Label("Move Right", labelStyle);
        Label toggleGravityUp = new Label("Toggle Gravity Up", labelStyle);
        Label toggleGravityDown = new Label("Toggle Gravity Down", labelStyle);
        Label shootGum = new Label("Shoot Gum", labelStyle);
        Label unstickGum = new Label("Unstick Gum", labelStyle);
        Label toggleMinimap = new Label("Toggle Minimap", labelStyle);
        Label reloadGum = new Label("Reload Gum", labelStyle);
        TextButton.TextButtonStyle backButtonStyle = new TextButton.TextButtonStyle(null, null, null, this.titleFont);

        TextButton.TextButtonStyle controlsButtonStyle = new TextButton.TextButtonStyle(null, null, null, this.labelFont);

        controlsButtonStyle.checkedFontColor = bubblegumPink;


     /*the key bindings are as follows:
        0: left
        1: right
        2: grav up
        3: grav down
        4: minimap
        5: reload (and you can't be moving at the time? why doesn't it just stop you from moving?)
        6: shoot
        7: unstick */

        int[] values = SaveData.getKeyBindings();
        for (int i : values) System.out.println(Input.Keys.toString(i));

        moveLeftButton = new TextButton(Input.Keys.toString(values[0]).toUpperCase(), controlsButtonStyle);
        moveRightButton = new TextButton(Input.Keys.toString(values[1]).toUpperCase(), controlsButtonStyle);
        toggleGravityUpButton = new TextButton(Input.Keys.toString(values[2]).toUpperCase(), controlsButtonStyle);
        toggleGravityDownButton = new TextButton(Input.Keys.toString(values[3]).toUpperCase(), controlsButtonStyle);
        toggleMinimapButton = new TextButton(Input.Keys.toString(values[4]).toUpperCase(), controlsButtonStyle);
        reloadGumButton = new TextButton(Input.Keys.toString(values[5]).toUpperCase(), controlsButtonStyle);

        controlsBackButton = new TextButton("Back", backButtonStyle);
        controlsBackButton.getLabel().setFontScale(.5f);

        defaultButton = new TextButton("Default Settings", backButtonStyle);
        defaultButton.getLabel().setFontScale(.5f);

        HorizontalGroup horizontalButtonGroup = new HorizontalGroup();
        horizontalButtonGroup.space(100);
        horizontalButtonGroup.addActor(controlsBackButton);
        horizontalButtonGroup.addActor(defaultButton);

        addControlButtonListeners();

        Label[] labels = new Label[]{moveLeft, moveRight, toggleGravityUp, toggleGravityDown, toggleMinimap, reloadGum, shootGum, unstickGum};

        List.ListStyle selectBoxListStyle = new List.ListStyle();
        selectBoxListStyle.background = backgroundButton;
        selectBoxListStyle.font = labelFont;
        selectBoxListStyle.fontColorSelected = bubblegumPink;
        selectBoxListStyle.fontColorUnselected = Color.WHITE;
        selectBoxListStyle.selection = backgroundButton;


        SelectBox.SelectBoxStyle selectBoxStyle = new SelectBox.SelectBoxStyle();
        selectBoxStyle.font =labelFont;
        selectBoxStyle.fontColor = Color.WHITE;
        selectBoxStyle.listStyle = selectBoxListStyle;
        selectBoxStyle.scrollStyle = new ScrollPane.ScrollPaneStyle();
        selectBoxStyle.listStyle.selection.setTopHeight(10);
        selectBoxStyle.listStyle.selection.setBottomHeight(10);


        shootGumSelect = new SelectBox(selectBoxStyle);
        shootGumSelect.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {

            }

            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                hoverBooleans[buttonIndexMap.get(shootGumSelect)] = true;
            }

            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                hoverBooleans[buttonIndexMap.get(shootGumSelect)] = false;
            }
        });

        unstickGumSelect = new SelectBox(selectBoxStyle);
        unstickGumSelect.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {

            }

            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                hoverBooleans[buttonIndexMap.get(unstickGumSelect)] = true;
            }

            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                hoverBooleans[buttonIndexMap.get(unstickGumSelect)] = false;
            }
        });



        controlsTable.row();
        controlsTable.add(controls).pad(100, 100, 40, 0);


        buttonIndexMap.put(moveLeftButton, 0);
        buttonIndexMap.put(moveRightButton, 1);
        buttonIndexMap.put(toggleGravityUpButton, 2);
        buttonIndexMap.put(toggleGravityDownButton, 3);
        buttonIndexMap.put(toggleMinimapButton, 4);
        buttonIndexMap.put(reloadGumButton, 5);
        buttonIndexMap.put(shootGumSelect, 6);
        buttonIndexMap.put(unstickGumSelect, 7);

        int rightPadding[] = new int[]{0, 240, 220, 120, 80, 280, 240, 200};
        Object[] objects = new Object[]{moveLeftButton, moveRightButton,
                                                toggleGravityUpButton, toggleGravityDownButton,
                                                toggleMinimapButton, reloadGumButton,
                                                shootGumSelect, unstickGumSelect};
        Table c = new Table();

        scrollPane = new ScrollPane(c);
//        scrollPane.debug();
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setScrollBarPositions(false, true);

        for (int i = 0; i < 8; i++) {
            Object button = objects[i];
            c.row();
            c.add(labels[i]).pad(0, 0, 32, rightPadding[i]);
            c.add((Actor) button).pad(0, 0, 32, 0);
        }
        controlsTable.row();
        controlsTable.add(scrollPane).pad(-10,160,0,0).height(350);



        shootGumSelect.setItems("Left Click", "Right Click", "Shift-Click");
        unstickGumSelect.setItems("Left Click", "Right Click", "Shift-Click");
        controlsTable.row();
        controlsTable.add(horizontalButtonGroup).pad(20, 100, 0, 0);
        controlsTable.row();

        for (Cell cell : c.getCells()) {
            cell.align(Align.left);
        }
        for (Cell cell : controlsTable.getCells()){
            cell.align(Align.left);
        }
        soundEffectsSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundController.getInstance().setEffectsVolume(soundEffectsSlider.getValue());
                if (!soundEffectsSlider.isDragging()) {
                    SoundController.playSound("jump", 1);
                }
            }
        });
    }

    /**
     * If tables have not been initialized then create tables
     */
    public void initialize(BitmapFont labelFont, BitmapFont projectSpace) {
        if (this.labelFont != null) {
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


    /**
     * Draws the settings page
     * Draws stage, background, and hover arrows for controls
     */
    public void draw() {
        stage.getBatch().begin();
        // draw background
        stage.getBatch().draw(background.getRegion(), 0, 0, stage.getViewport().getWorldWidth(), stage.getViewport().getWorldHeight());

        // draw hover arrows
        int spacing = 15;
        for (Map.Entry<Object, Integer> entry : buttonIndexMap.entrySet()) {
            Actor actor = (Actor) entry.getKey();

            int index = entry.getValue();
            if (hoverBooleans[index] || (actor instanceof TextButton && ((TextButton) actor).isChecked()) || (actor instanceof SelectBox && ((SelectBox<?>) actor).getScrollPane().hasParent())) {
                stage.getBatch().draw(arrow,
                        actor.getX() + 160 + actor.getWidth() + spacing,
                        actor.getY() + scrollPane.getScrollY(), arrow.getRegionWidth() / 2,
                        arrow.getRegionHeight() / 2,
                        arrow.getRegionWidth(), arrow.getRegionHeight(), 1, 1, 180);
                stage.getBatch().draw(arrow,
                        actor.getX() + 160 - arrow.getRegionWidth() - spacing,
                        actor.getY() + scrollPane.getScrollY(), arrow.getRegionWidth() / 2,
                        arrow.getRegionHeight() / 2,
                        arrow.getRegionWidth(), arrow.getRegionHeight(), 1, 1, 0);
            }
        }
        stage.getBatch().end();
        stage.draw();
    }

    @Override
    public void show() {
        active = true;
        backButtonClicked = false;
        Gdx.input.setInputProcessor(inputMultiplexer);
    }


    @Override
    public void render(float delta) {
        // TODO clear screen (crashing on mac??)
//        Gdx.gl.glClearColor( 0, 0, 0, 1 );
//        Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT);

        if (active) {
            stage.act();
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
        checkedButton = null;
        for (Object o: buttonIndexMap.keySet()){
            if (o instanceof TextButton){
                ((TextButton) o).setChecked(false);
            }
        }
        SaveData.setKeyBindings(values);
        SoundController.setEffectsVolume(soundEffectsSlider.getValue());
        SoundController.setMusicVolume(musicSlider.getValue());
        SaveData.setSFXVolume(soundEffectsSlider.getValue());
        SaveData.setMusicVolume(musicSlider.getValue());
        PlayerController.changeControls(values);
        //add to save data
    }

    @Override
    public void dispose() {
        internal.dispose();
    }

    /**
     * Custom input processor used to detect when keyboard or mouse is clicked
     * to change user controls
     */
    public class SettingsInputProcessor extends InputAdapter {

        /**
         * If button is checked, change value to key pressed.
         * Unchecks button afterward
         */
        @Override
        public boolean keyUp(int keycode) {

            ArrayList<Integer> indices = new ArrayList<>(2);

            for (int i = 0; i < values.length; i++) {
                if (values[i] == keycode) {
                    indices.add(i);
                    break;
                }
            }

            if (checkedButton != null) {
                int buttonIndex = buttonIndexMap.get(checkedButton);

                int indexGravityUp = buttonIndexMap.get(toggleGravityUpButton);
                int indexGravityDown = buttonIndexMap.get(toggleGravityDownButton);


                // only no duplicates except for gravity
                if (indices.size() == 2 || keycode == Input.Keys.ESCAPE || (indices.size() == 1 && (!(indices.get(0) == indexGravityDown && buttonIndex == indexGravityUp) &&
                        !(indices.get(0) == indexGravityUp && buttonIndex == indexGravityDown)))) {
                    SoundController.playSound("error", 1);
                    return true;
                }

                values[buttonIndex] = keycode;
                checkedButton.setText(Input.Keys.toString(keycode).toUpperCase());
                checkedButton.setChecked(false);
                checkedButton = null;
            }

            // TODO debug remove
            //  }
            //if (keycode == Input.Keys.NUM_1){
            //    controlsTable.setDebug(!controlsTable.getDebug());
            //    settingsTable.setDebug(!settingsTable.getDebug());
            // }
            return true;
        }
    }
}
