package edu.cornell.gdiac.bubblegumbandit.helpers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import edu.cornell.gdiac.bubblegumbandit.controllers.modes.SettingsMode;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;

public class SaveData {


  /** The access key for save data in a computer's preferences. Should never be changed */
  private final static String prefsName = "save";

  /** Constants relating to level status */
  private static int INCOMPLETE = -1;
  private static int LOCKED = -2;
  private static int NOT_FOUND = -3;

  /** The number of key bindings in-game */
  private static int keyCount = 9;

  //any numbers above -1 represent the number of stars collected

  /** Returns whether valid save data can be found on this device */
  public static boolean saveExists() {
   return false; //uncomment to reset save data
   // return Gdx.app.getPreferences(prefsName).getBoolean("save created", false);
  }

  /** Makes a new save with defaults
   * @param lockLevels whether this save should start with levels initially locked or not
   * */

  public static void makeData(boolean lockLevels, AssetDirectory directory) {
    Preferences prefs =  Gdx.app.getPreferences(prefsName);

    prefs.putFloat("music", .5f);
    prefs.putFloat("sfx", 1f);
    prefs.putInteger("lastFinished", 1);

    prefs.putInteger("level1", INCOMPLETE);
    JsonValue level;
    int i = 1;
    while (true) {
      level = directory.getEntry("level" + i, JsonValue.class);
      if (level != null) {
        JsonValue prop = level.get("properties").child;
        int count = 0;
        while (prop != null) {
          String propName = prop.get("name").asString();
          if( propName.equals("captives")) {
            count = prop.getInt("value");
          }
          prop = prop.next();
        }

        prefs.putInteger("level"+i+"Captives", count);
        if(i>1) prefs.putInteger("level" + i, lockLevels ? LOCKED : INCOMPLETE);
        i++;
      } else break;

    }

    /*the key bindings are as follows:
        0: left
        1: right
        2: grav up
        3: grav down
        4: shoot
        5: unstick
        6: reload
        7: minimap
        8: pause  */

    int[] defaultKeys = SettingsMode.defaultVals;
    boolean[] defaultBindings = SettingsMode.defaultBindings;

    for (int j = 0; i < keyCount; j++){
      prefs.putInteger("key" + j, defaultKeys[j]);
      prefs.putBoolean("key"+j+"bool", defaultBindings[j]);
    }

    prefs.putBoolean("save created", true);
    prefs.flush();
  }

  /** Returns the status of a given level, given by the above constants
   * Note: this should stay private
   * @param level the level number
   */
  private static int getLevelStatus(int level) {
    int status =  Gdx.app.getPreferences(prefsName).
        getInteger("level"+level, NOT_FOUND);
    if(status == NOT_FOUND) System.err.println("Could not find level" +
        " "+level+" when retrieving save data. ");
    return status;

  }

  public static int getCaptiveCount(int level) {
    int count =  Gdx.app.getPreferences(prefsName).
        getInteger("level"+level+"Captives", NOT_FOUND);
    if(count == NOT_FOUND) System.err.println("Could not find level" +
        " "+level+" when retrieving save data. ");
    return count;
  }

  /** Returns whether a level has been completed
   * @param level the level number
   */
  public static boolean completed(int level) {
    return getLevelStatus(level)>-1;
  }

  /** Returns the number of stars collected from a completed level
   * Requires: The level has been completed (the status is >=0)
   * @param level the level number
   */
  public static int getStars(int level) {
    int status = getLevelStatus(level);
    if(status<0) System.err.println("Requested star count from incomplete or locked level.");
    return status;
  }

  /** Returns whether a level has been unlocked
   * @param level the level number
   */
  public static boolean unlocked(int level) {
    int status = getLevelStatus(level);
    return status >LOCKED;
  }

  /** Unlocks a level
   * @param level the level number
   */
  public static void unlock(int level) {
    Preferences prefs = Gdx.app.getPreferences(prefsName);
    boolean levelExists = prefs.contains("level"+level);
    boolean levelLocked = !unlocked(level);
    if(levelExists&&levelLocked) {
      prefs.putInteger("level"+level, -1);
      prefs.flush();
    }
    if(!levelExists) {
      System.err.println("Level "+level+" does not exist.");
    }

  }

  /** Sets the status of a level
   * @param level the level number
   */
  public static void setStatus(int level, int status) {
    Preferences prefs = Gdx.app.getPreferences(prefsName);
    prefs.putInteger("level"+level, status);
    prefs.flush();

  }

  /** Returns an array of the current key bindings
   */
  public static int[] getKeyBindings() {

    int[] keys = new int[keyCount];
    Preferences prefs = Gdx.app.getPreferences(prefsName);
    for(int i = 0; i<keyCount; i++) {
      keys[i] = prefs.getInteger("key"+i);
    }
    return keys;

  }

  public static boolean[] getKeyButtons() {
    boolean[] keys = new boolean[keyCount];
    Preferences prefs = Gdx.app.getPreferences(prefsName);
    for(int i = 0; i<keyCount; i++) {
      keys[i] = prefs.getBoolean("key"+i +"bool");
    }
    return keys;
  }

  /** Sets the current key bindings
   * @param keyBindings the new bindings
   */
  public static void setKeyBindings(int[] keyBindings, boolean[] buttons) {
    Preferences prefs = Gdx.app.getPreferences(prefsName);
    assert keyBindings.length == keyCount;
    for(int i = 0; i< keyBindings.length; i++) {
      prefs.putInteger("key"+i, keyBindings[i]);
      prefs.putBoolean("key"+i+"bool", buttons[i]);
    }
    prefs.flush();
  }

  /** Returns the current music volume
   */
  public static float getMusicVolume() {
    return  Gdx.app.getPreferences(prefsName).getFloat("music");
  }

  /** Returns the current sound effects volume
   */
  public static float getSFXVolume() {
    return  Gdx.app.getPreferences(prefsName).getFloat("sfx");
  }

  /** Sets the music volume
   * @param val the new volume (between 0 and 1)
   */
  public static void setMusicVolume(float val) {
    Preferences prefs = Gdx.app.getPreferences(prefsName);
    prefs.putFloat("music", val);
    prefs.flush();
  }


  /** Sets the sound effects volume
   * @param val the new volume (between 0 and 1)
   */
  public static void setSFXVolume(float val) {
    Preferences prefs = Gdx.app.getPreferences(prefsName);
    prefs.putFloat("sfx", val);
    prefs.flush();
  }

  public static int getContinueLevel() {

    Preferences prefs = Gdx.app.getPreferences(prefsName);
    int levels = 1;
    while(true) {
      int status = prefs.getInteger(("level"+levels), -10);
      if(status == -10) break;
      levels++;
    }
    int current = prefs.getInteger("lastFinished", -10);
    if(current==-1) return 1;
    if(current==-10) {
      prefs.putInteger("lastFinished", levels);
      prefs.flush();
      return 1;
    }
    if(current==levels) return 1;
    else return current;

  }

  public static void setLevel(int level) {
    Preferences prefs = Gdx.app.getPreferences(prefsName);
    prefs.putInteger("lastFinished", level);
    prefs.flush();
  }

}
