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
import edu.cornell.gdiac.bubblegumbandit.controllers.InputController;
import edu.cornell.gdiac.bubblegumbandit.controllers.PlayerController;
import edu.cornell.gdiac.bubblegumbandit.controllers.SoundController;
import edu.cornell.gdiac.util.ScreenListener;
import org.w3c.dom.Text;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashMap;

public class SettingsMode implements Screen {

    /**
     * The stage, contains all HUD elements
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

    private TextButton reloadGumButton;

    private boolean backButtonClicked;


    /**
     * Whether or not this player mode is still active
     */
    private boolean active;

    /**
     * The container for laying out all HUD elements
     */
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

    public final Color bubblegumPink = new Color(1, 149 / 255f, 138 / 255f, 1);

    public final Color spaceBlue = new Color(55 / 255f, 226 / 255f, 226 / 255f, 1);

    private TextButton checkedButton;

    private HashMap<TextButton, Integer> buttonIndexMap = new HashMap<>();

    private int[] values;

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
    }

    public void setViewport(Viewport view) {
        stage.setViewport(view);
        view.apply(true);
    }

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


    public void makeSettingsTable() {
        settingsStyle = new Label.LabelStyle(this.titleFont, spaceBlue);
        labelStyle = new Label.LabelStyle(this.labelFont, Color.WHITE);
        Label settings = new Label("Settings", settingsStyle);
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

    private void makeControlsTable() {
        Label controls = new Label("Controls", settingsStyle);
        Label strafeLeft = new Label("Strafe Left", labelStyle);
        Label strafeRight = new Label("Strafe Right", labelStyle);
        Label toggleGravityUp = new Label("Toggle Gravity Up", labelStyle);
        Label toggleGravityDown = new Label("Toggle Gravity Down", labelStyle);
        Label shootGum = new Label("Shoot Gum", labelStyle);
        Label unstickGum = new Label("Unstick Gum", labelStyle);
        Label toggleMinimap = new Label("Toggle Minimap", labelStyle);
        Label reloadGum = new Label("Reload Gum", labelStyle);
        TextButton.TextButtonStyle backButtonStyle = new TextButton.TextButtonStyle(null, null, null, this.titleFont);

        TextButton.TextButtonStyle controlsButtonStyle = new TextButton.TextButtonStyle(null, null, null, this.labelFont);
        controlsButtonStyle.checkedFontColor = bubblegumPink;


        strafeLeftButton = new TextButton("A", controlsButtonStyle);
        strafeLeftButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (checkedButton != null) {
                    checkedButton.setChecked(false);
                }
                if (checkedButton == strafeLeftButton) {
                    checkedButton = null;
                } else {
                    checkedButton = strafeLeftButton;
                }
            }

            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {

                strafeLeftButton.getLabel().setColor(bubblegumPink);
            }

            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                strafeLeftButton.getLabel().setColor(Color.WHITE);
            }
        });

        strafeRightButton = new TextButton("D", controlsButtonStyle);
        strafeRightButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (checkedButton != null) {
                    checkedButton.setChecked(false);
                }
                if (checkedButton == strafeRightButton) {
                    checkedButton = null;
                } else {
                    checkedButton = strafeRightButton;
                }
            }

            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {

                strafeRightButton.getLabel().setColor(bubblegumPink);
            }

            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                strafeRightButton.getLabel().setColor(Color.WHITE);
            }
        });

        toggleGravityUpButton = new TextButton("SPACE", controlsButtonStyle);
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

                toggleGravityUpButton.getLabel().setColor(bubblegumPink);
            }

            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                toggleGravityUpButton.getLabel().setColor(Color.WHITE);
            }
        });

        toggleGravityDownButton = new TextButton("SPACE", controlsButtonStyle);
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

                toggleGravityDownButton.getLabel().setColor(bubblegumPink);
            }

            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                toggleGravityDownButton.getLabel().setColor(Color.WHITE);
            }
        });

        toggleMinimapButton = new TextButton("L-SHIFT", controlsButtonStyle);
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

                toggleMinimapButton.getLabel().setColor(bubblegumPink);
            }

            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                toggleMinimapButton.getLabel().setColor(Color.WHITE);
            }
        });

        shootGumButton = new TextButton("LEFT CLICK", controlsButtonStyle);

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

//                shootGumButton.getLabel().setColor(bubblegumPink);
            }

            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
//                shootGumButton.getLabel().setColor(Color.WHITE);
            }
        });
        unstickGumButton = new TextButton("RIGHT CLICK", controlsButtonStyle);
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
        });

        reloadGumButton = new TextButton("R", controlsButtonStyle);
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
        });

        controlsBackButton = new TextButton("Back", backButtonStyle);
        controlsBackButton.getLabel().setFontScale(.5f);

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
        controlsTable.add(reloadGum);
        controlsTable.add(reloadGumButton);
        controlsTable.row();
        controlsTable.add(shootGum);
        controlsTable.add(shootGumButton);
        controlsTable.row();
        controlsTable.add(unstickGum);
        controlsTable.add(unstickGumButton);
        controlsTable.row();
        controlsTable.add(controlsBackButton);

        for (Cell cell : controlsTable.getCells()) {
            cell.align(Align.left);
            if (cell.getColumn() == 0) {
                cell.pad(10, 10, 10, 100);
            } else {
                cell.pad(10);
            }
        }

        buttonIndexMap.put(strafeLeftButton, 0);
        buttonIndexMap.put(strafeRightButton, 1);
        buttonIndexMap.put(toggleGravityUpButton, 2);
        buttonIndexMap.put(toggleGravityDownButton, 3);
        buttonIndexMap.put(toggleMinimapButton, 4);
        buttonIndexMap.put(reloadGumButton, 5);
        buttonIndexMap.put(shootGumButton, 6);
        buttonIndexMap.put(unstickGumButton, 7);

    }

    public void initialize(BitmapFont labelFont, BitmapFont projectSpace) {
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
        stage.draw();
    }

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
        PlayerController.changeControls(values);
    }

    @Override
    public void dispose() {
        internal.dispose();
    }

    public class SettingsInputProcessor extends InputAdapter {
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
