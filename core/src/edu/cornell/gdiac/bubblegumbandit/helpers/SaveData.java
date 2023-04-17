package edu.cornell.gdiac.bubblegumbandit.helpers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class SaveData {

  public static int LOCKED = -2;
  public static int NOT_FOUND = -3;
  public static int INCOMPLETE = -1;
  private static String prefsName = "save";


  public static boolean saveExists() {

  }
  public static void makeSave(int levelCount) {

  }


  public static boolean unlocked(int levelName) {

  }

  public static int stars(int level) {

  }

  public static boolean levelExists(int level) {

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
