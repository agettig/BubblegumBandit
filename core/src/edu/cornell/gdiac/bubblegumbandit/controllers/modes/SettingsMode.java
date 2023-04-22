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
import edu.cornell.gdiac.util.ScreenListener;

import java.util.HashMap;
import java.util.Map;

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

    /**
     * Unstick gum button
     */
    private TextButton unstickGumButton;

    /**
     * Shoot gum button
     */
    private TextButton shootGumButton;

    /**
     * Controls button
     * Takes the player to controls settings screen
     */
    private TextButton controlsButton;

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
     * Whether or not back butotn to main menu was clicked
     */
    private boolean backButtonClicked;


    /**
     * Whether or not this player mode is still active
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
    private HashMap<TextButton, Integer> buttonIndexMap = new HashMap<>();

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

        background = new TextureRegionDrawable(internal.getEntry("background", Texture.class));
        sliderBeforeKnob = new TextureRegionDrawable(new TextureRegion(slider));
        sliderKnob = new TextureRegionDrawable(new TextureRegion(knob));
        sliderTexture = new TextureRegionDrawable(sliderBeforeKnobTexture);
        arrow = new TextureRegion(internal.getEntry("arrow", Texture.class));


        stage = new Stage();
        settingsTable = new Table();
        settingsTable.align(Align.center);
        controlsTable = new Table();
        controlsTable.align(Align.center);
        settingsTable.setFillParent(true);
        controlsTable.setFillParent(true);
        stage.addActor(settingsTable);
        settingsTable.debug();
        values = new int[]{Input.Keys.A,
                           Input.Keys.D,
                           Input.Keys.SPACE,
                           Input.Keys.SPACE,
                           Input.Keys.SHIFT_LEFT,
                           Input.Keys.R,
                           Input.Buttons.LEFT,
                           Input.Buttons.RIGHT};

        SettingsInputProcessor settingsInputProcessor = new SettingsInputProcessor();
        inputMultiplexer = new InputMultiplexer(stage, settingsInputProcessor);
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
        musicSlider.setValue(.5f);

        soundEffectsSlider = new Slider(0, 1, .05f, false, sliderStyle);
        soundEffectsSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                SoundController.getInstance().setEffectsVolume(soundEffectsSlider.getValue());
            }
        });
        soundEffectsSlider.setValue(.5f);
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

        TextButton.TextButtonStyle backButtonStyle = new TextButton.TextButtonStyle(null, null, null, this.titleFont);

        backButtonSettings = new TextButton("Back", backButtonStyle);
        backButtonSettings.getLabel().setFontScale(.5f);

        backButtonSettings.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
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

        for (Cell cell : settingsTable.getCells()) {
            cell.align(Align.left);
            if (cell.getColumn() == 0) {
                cell.pad(10, 10, 10, 100);
            } else {
                cell.pad(10);
            }
        }
        settingsTable.columnDefaults(1).setActorWidth(400);
        settingsTable.columnDefaults(1).fillX();
    }

    /**
     * Add listeners to controls buttons
     */
    private void addControlButtonListners() {
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
        shootGumButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (checkedButton != null) {
                    checkedButton.setChecked(false);
                }
                if (checkedButton == shootGumButton) {
                    checkedButton = null;
                } else {
                    checkedButton = shootGumButton;
                }
            }

            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                hoverBooleans[buttonIndexMap.get(shootGumButton)] = true;
            }

            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                hoverBooleans[buttonIndexMap.get(shootGumButton)] = false;
            }
        });
        unstickGumButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (checkedButton != null) {
                    checkedButton.setChecked(false);
                }
                if (checkedButton == unstickGumButton) {
                    checkedButton = null;
                } else {
                    checkedButton = unstickGumButton;
                }
            }

            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {

                hoverBooleans[buttonIndexMap.get(unstickGumButton)] = true;
            }

            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                hoverBooleans[buttonIndexMap.get(unstickGumButton)] = false;
            }
        });
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
            }

            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {

                controlsBackButton.getLabel().setColor(bubblegumPink);
            }

            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                controlsBackButton.getLabel().setColor(Color.WHITE);
            }
        });

    }

    /**
     * Make controls table
     */
    private void makeControlsTable() {
        Label controls = new Label("Controls", headingsStyle);
        Label moveLeft = new Label("Strafe Left", labelStyle);
        Label moveRight = new Label("Strafe Right", labelStyle);
        Label toggleGravityUp = new Label("Toggle Gravity Up", labelStyle);
        Label toggleGravityDown = new Label("Toggle Gravity Down", labelStyle);
        Label shootGum = new Label("Shoot Gum", labelStyle);
        Label unstickGum = new Label("Unstick Gum", labelStyle);
        Label toggleMinimap = new Label("Toggle Minimap", labelStyle);
        Label reloadGum = new Label("Reload Gum", labelStyle);
        TextButton.TextButtonStyle backButtonStyle = new TextButton.TextButtonStyle(null, null, null, this.titleFont);

        TextButton.TextButtonStyle controlsButtonStyle = new TextButton.TextButtonStyle(null, null, null, this.labelFont);
        controlsButtonStyle.checkedFontColor = bubblegumPink;


        moveLeftButton = new TextButton("A", controlsButtonStyle);
        moveRightButton = new TextButton("D", controlsButtonStyle);
        toggleGravityUpButton = new TextButton("SPACE", controlsButtonStyle);
        toggleGravityDownButton = new TextButton("SPACE", controlsButtonStyle);
        toggleMinimapButton = new TextButton("L-SHIFT", controlsButtonStyle);
        shootGumButton = new TextButton("LEFT CLICK", controlsButtonStyle);
        unstickGumButton = new TextButton("RIGHT CLICK", controlsButtonStyle);
        reloadGumButton = new TextButton("R", controlsButtonStyle);
        controlsBackButton = new TextButton("Back", backButtonStyle);
        controlsBackButton.getLabel().setFontScale(.5f);

        addControlButtonListners();

        Label[] labels = new Label[]{moveLeft, moveRight, toggleGravityUp, toggleGravityDown, toggleMinimap, reloadGum, shootGum, unstickGum};


        controlsTable.row();
        controlsTable.add(controls);

        buttonIndexMap.put(moveLeftButton, 0);
        buttonIndexMap.put(moveRightButton, 1);
        buttonIndexMap.put(toggleGravityUpButton, 2);
        buttonIndexMap.put(toggleGravityDownButton, 3);
        buttonIndexMap.put(toggleMinimapButton, 4);
        buttonIndexMap.put(reloadGumButton, 5);
        buttonIndexMap.put(shootGumButton, 6);
        buttonIndexMap.put(unstickGumButton, 7);

        for (Map.Entry<TextButton, Integer> entry : buttonIndexMap.entrySet()) {
            TextButton button = entry.getKey();
            int index = entry.getValue();
            controlsTable.row();
            controlsTable.add(labels[index]);
            controlsTable.add(button);
        }

        for (Cell cell : controlsTable.getCells()) {
            cell.align(Align.left);
            if (cell.getColumn() == 0) {
                cell.pad(10, 10, 10, 100);
            } else {
                cell.pad(10);
            }
        }
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
        stage.getBatch().draw(background.getRegion(), 0, 0, stage.getViewport().getScreenWidth(), stage.getViewport().getScreenHeight());

        // draw hover arrows
        int spacing = 15;
        for (Map.Entry<TextButton, Integer> entry : buttonIndexMap.entrySet()) {
            TextButton button = entry.getKey();
            int index = entry.getValue();
            if (hoverBooleans[index] || button.isChecked()) {
                stage.getBatch().draw(arrow,
                        button.getX() + button.getWidth() + spacing,
                        button.getY(), arrow.getRegionWidth() / 2,
                        arrow.getRegionHeight() / 2,
                        arrow.getRegionWidth(), arrow.getRegionHeight(), 1, 1, 180);
                stage.getBatch().draw(arrow,
                        button.getX() - arrow.getRegionWidth() - spacing,
                        button.getY(), arrow.getRegionWidth() / 2,
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
        PlayerController.changeControls(values);
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
            if (checkedButton != null && checkedButton != shootGumButton && checkedButton != unstickGumButton) {
                values[buttonIndexMap.get(checkedButton)] = keycode;
                checkedButton.setText(Input.Keys.toString(keycode).toUpperCase());
                checkedButton.setChecked(false);
                checkedButton = null;
            }
            return true;
        }

        /**
         * If button is checked, change value to button pressed.
         * Unchecks button afterward
         */
        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {

            if (checkedButton != null && (checkedButton == shootGumButton || checkedButton == unstickGumButton)) {
                if (button == Input.Buttons.LEFT) {
                    checkedButton.setText("LEFT CLICK");
                    values[buttonIndexMap.get(checkedButton)] = Input.Buttons.LEFT;
                } else if (button == Input.Buttons.RIGHT) {
                    checkedButton.setText("RIGHT CLICK");
                    values[buttonIndexMap.get(checkedButton)] = Input.Buttons.RIGHT;
                }
                checkedButton.setChecked(false);
                checkedButton = null;
            }
            return true;
        }
    }
}
