package edu.cornell.gdiac.bubblegumbandit.helpers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class SaveData {


  private static String prefsName = "save";
  private static int INCOMPLETE = -1;
  private static int LOCKED = -2;
  private static int NOT_FOUND = -3;
  //any numbers above -1 represent the number of stars collected

  public static boolean saveExists() {
    return Gdx.app.getPreferences(prefsName).getBoolean("save created", false);
  }

  public static void makeData(int levelCount, boolean lockLevels) {
    Preferences prefs =  Gdx.app.getPreferences(prefsName);
    prefs.putInteger("1", -1);
    for(int i = 2; i<levelCount+1; i++) {
      prefs.putInteger(""+i, lockLevels? LOCKED : INCOMPLETE);
    }
    prefs.flush();
  }

  private static int getLevelStatus(int level) {
    int status =  Gdx.app.getPreferences(prefsName).getInteger(""+level, NOT_FOUND);
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
    boolean levelExists = prefs.contains(""+level);
    boolean levelLocked = !unlocked(level);
    if(levelExists&&levelLocked) {
      prefs.putInteger(""+level, 0);
      prefs.flush();
    }
    if(!levelExists) {
      System.err.println("Level "+level+" does not exist.");
    }

  }


  public static void setStatus(int level, int status) {
    Preferences prefs = Gdx.app.getPreferences(prefsName);
    prefs.putInteger(""+level, status);
    prefs.flush();

  }


}
