package edu.cornell.gdiac.bubblegumbandit.helpers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;

public class SaveData {


  private static String prefsName = "save";
  private static int INCOMPLETE = -1;
  private static int LOCKED = -2;
  private static int NOT_FOUND = -3;
  private static int keyCount = 8;

  //any numbers above -1 represent the number of stars collected

  public static boolean saveExists() {
    return Gdx.app.getPreferences(prefsName).getBoolean("save created", false);
  }

  public static void makeData(int levelCount, boolean lockLevels) {
    Preferences prefs =  Gdx.app.getPreferences(prefsName);

    prefs.putFloat("music", .5f);
    prefs.putFloat("sfx", 1f);

    prefs.putInteger("level1", -1);
    for(int i = 2; i<levelCount+1; i++) {
      prefs.putInteger("level"+i, lockLevels? LOCKED : INCOMPLETE);
    }

    /*the key bindings are as follows:
        0: left
        1: right
        2: grav up
        3: grav down
        4: minimap
        5: reload (and you can't be moving at the time? why doesn't it just stop you from moving?)
        6: shoot
        7: unstick */

    //basic controls here
    prefs.putInteger("key0", Input.Keys.A);
    prefs.putInteger("key1", Input.Keys.D);
    prefs.putInteger("key2", Input.Keys.SPACE);
    prefs.putInteger("key3", Input.Keys.SPACE);
    prefs.putInteger("key4", Input.Keys.SHIFT_LEFT);
    prefs.putInteger("key5", Input.Keys.R);
    prefs.putInteger("key6", Input.Buttons.LEFT); //as in click
    prefs.putInteger("key7", Input.Buttons.RIGHT);



    prefs.putBoolean("save created", true);
    prefs.flush();
  }

  private static int getLevelStatus(int level) {
    int status =  Gdx.app.getPreferences(prefsName).getInteger("level"+level, NOT_FOUND);
    if(status == NOT_FOUND) System.err.println("Could not find level "+level+" when retrieving save data. ");
    return status;

  }

  public static boolean completed(int level) {
    return getLevelStatus(level)>-1;
  }

  public static int getStars(int level) {
    int status = getLevelStatus(level);
    if(status<0) System.err.println("Requested star count from incomplete or locked level.");
    return status;
  }

  public static boolean unlocked(int level) {
    int status = getLevelStatus(level);
    return status >LOCKED;
  }

  public static void unlock(int level) {
    Preferences prefs = Gdx.app.getPreferences(prefsName);
    boolean levelExists = prefs.contains("level"+level);
    boolean levelLocked = !unlocked(level);
    if(levelExists&&levelLocked) {
      prefs.putInteger("level"+level, 0);
      prefs.flush();
    }
    if(!levelExists) {
      System.err.println("Level "+level+" does not exist.");
    }

  }


  public static void setStatus(int level, int status) {
    Preferences prefs = Gdx.app.getPreferences(prefsName);
    prefs.putInteger("level"+level, status);
    prefs.flush();

  }

  public static int[] getKeyBindings() {

    int[] keys = new int[keyCount];
    Preferences prefs = Gdx.app.getPreferences(prefsName);
    for(int i = 0; i<keyCount; i++) {
      keys[i] = prefs.getInteger("key"+i);
    }
    return keys;


  }

  public static void setKeyBindings(int[] keyBindings) {
    Preferences prefs = Gdx.app.getPreferences(prefsName);
    assert keyBindings.length == keyCount;
    for(int i = 0; i< keyBindings.length; i++) {
      prefs.putInteger("key"+i, keyBindings[i]);
    }
    prefs.flush();
  }

  public static float getMusicVolume() {
    return  Gdx.app.getPreferences(prefsName).getFloat("music");
  }

  public static float getSFXVolume() {
    return  Gdx.app.getPreferences(prefsName).getFloat("sfx");
  }

  public static void setMusicVolume(float val) {
    Preferences prefs = Gdx.app.getPreferences(prefsName);
    prefs.putFloat("music", val);
    prefs.flush();
  }
  public static void setSFXVolume(float val) {
    Preferences prefs = Gdx.app.getPreferences(prefsName);
    prefs.putFloat("sfx", val);
    prefs.flush();
  }





}
