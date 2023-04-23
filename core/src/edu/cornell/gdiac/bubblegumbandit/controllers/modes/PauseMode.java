package edu.cornell.gdiac.bubblegumbandit.controllers.modes;

import com.badlogic.gdx.*;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.viewport.Viewport;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;
import edu.cornell.gdiac.util.ScreenListener;

import java.util.HashMap;
import java.util.Map;

public class PauseMode implements Screen, InputProcessor, ControllerListener {
    /** Internal assets for the pause scrren*/
    private AssetDirectory directory;

    /** The JSON for game constants*/
    private JsonValue constantsJson;

    /** The stage that contains all the pause elements */
    private Stage stage;

    /** Background texture*/
    private TextureRegionDrawable background;

    /** Resume button that takes player back to the game*/
    private TextButton resumeButton;

    /** Reset Button that takes player back to the beginning of the game*/
    private TextButton retryButton;

    /** LevelSelectButton that takes player to the level select screen*/
    private TextButton levelSelectButton;

    /** Settings Button, takes player to the settings menu*/
    private TextButton settingsButton;

    /**Quit button, that exits the game*/
    private TextButton quitButton;

    /**Whether resume button was clicked*/
    private boolean resumeClicked;

    /**Whether retry button was clicked*/
    private boolean retryClicked;

    /**Whether level select button was clicked*/
    private boolean levelSelectClicked;

    /**Whether settings button was clicked*/
    private boolean settingsClicked;

    /**Whether quit button was clicked*/
    private boolean quitClicked;

    /** Pointer to what is being hovered. */
    private TextureRegion hoverPointer;

    /** Whether this player mode is still active*/
    private boolean active;

    /** The container for laying out the pause menu elements*/
    private Table pauseTable;

    /** Listener that updates the player mode*/
    private ScreenListener listener;

    /** Font used*/
    private BitmapFont font;

    /**
     * Style used for labels
     */
    private Label.LabelStyle style;

    /** The color used for hovering*/
    public final Color bubblegumPink = new Color(1, 149/255f, 138/255f, 1);

    /** Current button*/
    private TextButton currentButton;

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
    private boolean[] hoverBooleans = new boolean[]{false, false, false, false, false};

    /**
     * Values representing the keys and buttons pressed for each user control
     */
    private int[] values;


    /** Reference to the GameCanvas*/
    private GameCanvas canvas;


    /**
     * 0 = nothing pressed
     * 1 = resume down
     * 2 = rest down
     * 3 = level select down
     * 4 = setting down
     * 5 = save and quit down
     * 6 = resume up
     * 7 = reset up
     * 8 = level select up
     * 9 = settings up
     * 10 = save and quit up
     * */
    private int pressState;

    /** True if the player is ready to go after clicking resume*/
    public boolean isReady() {
        return pressState == 5;
    }

    /** True if the player wants to quit*/
    public boolean quit() {
        return pressState == 8;
    }

    private BitmapFont displayFont;

    /** The box2D world*/
    private World world;

    /**
     * Creates a PauseMode with the default size and position
     *
     * @param canvas The game canvas to draw to
     * @oaram file The asset directory
     */
    public PauseMode(GameCanvas canvas) {
        AssetDirectory directory = new AssetDirectory("jsons/pause.json");
        directory.loadAssets();
        directory.finishLoading();

        background = new TextureRegionDrawable(directory.getEntry("background", Texture.class));
        hoverPointer = new TextureRegion(directory.getEntry("arrow", Texture.class));
        active = true;
        this.canvas = canvas;
        canvas.getCamera().setFixedX(false);
        canvas.getCamera().setFixedY(false);
        canvas.getCamera().setZoom(1);

        stage = new Stage();
        pauseTable = new Table();
        pauseTable.align(Align.topLeft);
        pauseTable.setFillParent(true);
        stage.addActor(pauseTable);
        values = new int[] {Input.Keys.A,
                            };
    }

    /**
     * Set viewport
     */
    public void setViewport(Viewport view) {
        stage.setViewport(view);
        view.apply(true);
    }


    public void makePauseTable() {
        //style = new Label.LabelStyle(this.font, Color.WHITE);
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle(null, null, null, this.font);

        resumeButton = new TextButton("Resume", style);
        resumeButton.getLabel().setFontScale(.5f);
        retryButton = new TextButton("Retry", style);
        retryButton.getLabel().setFontScale(.5f);
        levelSelectButton = new TextButton("Level Select", style);
        levelSelectButton.getLabel().setFontScale(.5f);
        settingsButton = new TextButton("Settings", style);
        settingsButton.getLabel().setFontScale(.5f);
        quitButton = new TextButton("Quit", style);
        quitButton.getLabel().setFontScale(.5f);

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
        pauseTable.add(resumeButton).pad(0, 100, 0, 0);
        pauseTable.row();
        pauseTable.add(retryButton).pad(0, 100, 0, 0);
        pauseTable.row();
        pauseTable.add(levelSelectButton).pad(0, 100, 0, 0);
        pauseTable.row();
        pauseTable.add(settingsButton).pad(0, 100, 0, 0);
        pauseTable.row();
        pauseTable.add(quitButton).pad(0, 100, 0, 0);
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
        makePauseTable();
    }

    /**
     * Sets the ScreenListener for this mode
     * <p>
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }


    /** Draws the status of this mode*/
    private void draw() {
        stage.getBatch().begin();
        // draw background
        stage.getBatch().draw(background.getRegion(), 0, 0, stage.getViewport().getScreenWidth(), stage.getViewport().getScreenHeight());

        int spacing = 15;

        for (Map.Entry<TextButton, Integer> entry : buttonIndexMap.entrySet()) {
            TextButton button = entry.getKey();
            int index = entry.getValue();
            if (hoverBooleans[index] || button.isChecked()) {
                stage.getBatch().draw(hoverPointer,
                        button.getX() + button.getWidth() + spacing,
                        button.getY(), hoverPointer.getRegionWidth() / 2,
                        hoverPointer.getRegionHeight() / 2,
                        hoverPointer.getRegionWidth(), hoverPointer.getRegionHeight(), 1, 1, 180);
                stage.getBatch().draw(hoverPointer,
                        button.getX() - hoverPointer.getRegionWidth() - spacing,
                        button.getY(), hoverPointer.getRegionWidth() / 2,
                        hoverPointer.getRegionHeight() / 2,
                        hoverPointer.getRegionWidth(), hoverPointer.getRegionHeight(), 1, 1, 0);
            }
        }
        stage.getBatch().end();
        stage.draw();
    }

//    /** Called when the screen should render itself*/
//    public void render(float delta) {
//        if (active) {
//            update(delta);
//            draw();
//
//            if (isReady() && listener != null) {
//                listener.exitScreen(this, 0);
//            }
//        }
//    }

    @Override
    public void show() {
        active = true;
        Gdx.input.setInputProcessor(this);
    }


    @Override
    public void render(float delta) {
        if (active) {
            stage.act();
            draw();

            if (resumeClicked && listener != null) {
                listener.exitScreen(this, 1);
            }
            if (retryClicked && listener != null) {
                listener.exitScreen(this, 1);
            }
            if (levelSelectClicked && listener != null) {
                listener.exitScreen(this, 1);
            }
            if (settingsClicked && listener != null) {
                listener.exitScreen(this, 1);
            }
            if (quitClicked && listener != null) {
                listener.exitScreen(this, 1);
            }
        }
    }
    /** Called when the screen is resized*/
    public void resize(int width, int height) {
    }

    /**
            * Called when the Screen is paused.
            *
            * This is usually when it's not active or visible on screen. An Application is
            * also paused before it is destroyed.
            */
    public void pause() {

    }

    /**
     * Called when the Screen is resumed from a paused state.
     *
     * This is usually when it regains focus.
     */
    public void resume() {
    }


    /**
     * Called when this screen is no longer the current screen for a Game.
     */
    public void hide() {
        active = false;
    }

    public void dispose() {
        directory.dispose();
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }

    @Override
    public void connected(Controller controller) {

    }

    @Override
    public void disconnected(Controller controller) {

    }

    @Override
    public boolean buttonDown(Controller controller, int buttonCode) {
        return false;
    }

    @Override
    public boolean buttonUp(Controller controller, int buttonCode) {
        return false;
    }

    @Override
    public boolean axisMoved(Controller controller, int axisCode, float value) {
        return false;
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
            if (currentButton != null) {
                values[buttonIndexMap.get(currentButton)] = keycode;
                currentButton.setText(Input.Keys.toString(keycode).toUpperCase());
                currentButton.setChecked(false);
                currentButton = null;
            }
            if (keycode == Input.Keys.NUM_1){
                pauseTable.setDebug(!pauseTable.getDebug());
            }
            return true;
        }

        /**
         * If button is checked, change value to button pressed.
         * Unchecks button afterward
         */
        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {

            if (currentButton != null) {
                if (button == Input.Buttons.LEFT) {
                    currentButton.setText("LEFT CLICK");
                    values[buttonIndexMap.get(currentButton)] = Input.Buttons.LEFT;
                } else if (button == Input.Buttons.RIGHT) {
                    currentButton.setText("RIGHT CLICK");
                    values[buttonIndexMap.get(currentButton)] = Input.Buttons.RIGHT;
                }
                currentButton.setChecked(false);
                currentButton = null;
            }
            return true;
        }
    }
}