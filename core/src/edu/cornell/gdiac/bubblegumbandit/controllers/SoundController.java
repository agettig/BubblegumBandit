package edu.cornell.gdiac.bubblegumbandit.controllers;

import com.badlogic.gdx.Gdx;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.audio.*;

import edu.cornell.gdiac.bubblegumbandit.helpers.SaveData;
import java.util.HashMap;

import static edu.cornell.gdiac.backend.Effect.engine;

public class SoundController {

    /**
     * The jump sound.  We only want to play once.
     */
    private static SoundEffect jumpSound;
    /**
     * The small enemy shooting sound.  We only want to play once.
     */
    private static SoundEffect smallEnemyShootingSound;

    /**
     * The gum splat sound.  We only want to play once.
     */
    private static SoundEffect gumSplatSound;

    /**
     * The sound used when an illegal key binding was inputted
     */
    private static SoundEffect errorSound;

    /**
     * The sound when enemy is hit with gume.  We only want to play once.
     */
    private static SoundEffect enemySplatSound;

    /**
     * The sound when an item is collected.  We only want to play once.
     */
    private static SoundEffect collectItemSound;

    private static SoundEffect laserFiring;
    private static SoundEffect laserCharging;
    private static SoundEffect laserLocking;
    private static SoundEffect laserThud;

    /**The sound the robot makes when letting out the shock*/
    private static SoundEffect shockAttack;

    /**sound for when the bandit is shocked*/
    private static SoundEffect banditShock;

    private static SoundEffect failure;
    private static SoundEffect noGum;
    private static SoundEffect victory;
    private static SoundEffect rolling;
    private static SoundEffect clockTick;
    private static SoundEffect lowStressAlarm;
    private static SoundEffect reloadingGum;
    private static SoundEffect banditLanding;
    private static SoundEffect doorSound;
    private static SoundEffect keyClick;
    private static SoundEffect knockback;
    private static SoundEffect glassSmash;
    private static SoundEffect smash;
    private static SoundEffect shipExplosion;
    private static SoundEffect shipExhaust;
    private static SoundEffect banditHurt;
    private static SoundEffect shipDoor;
    private static SoundEffect banditJingle;
    private static SoundEffect pageTurn;
    private static SoundEffect unlockDoor;
    private static SoundEffect hubbaVictory;

    /**Hashmap holding sounds and corresponding Id*/
    private static HashMap<SoundEffect, Integer> soundIds;

    /**Hashmap holding sounds and corresponding string */
    private static HashMap<String, SoundEffect> sounds ;

    private static SoundController controller;

    private static float musicVolume;

    private static float soundEffectsVolume;


    // music

    /** menu music: SpaceCruising */
   private static AudioSource menu;

   /** intro to menu music */
   private static AudioSource menuIntro;

    /** in-game music: BubbleGumBallad */
    private static AudioSource game;

    /** intro to in-game music */
    private static AudioSource gameIntro;

    /** alarm music: escape! */
    private static AudioSource escape;

   /** engine*/
   private static AudioEngine engine;

    /** A queue to play music */
    private static MusicQueue musicPlayer;

    /**Hashmap holding sounds and corresponding string */
    private static HashMap<String, AudioSource> music ;

    /**last played soundId, for tracking */
    private static int lastPlayed;

    public SoundController() {}


    /**
     * Return the singleton instance of the input controller
     *
     * @return the singleton instance of the input controller
     */
    public static SoundController getInstance() {
        if (controller == null) {
            controller = new SoundController();
        }
        return controller;
    }

    public void initialize(AssetDirectory directory){

        if (SaveData.saveExistsSettings()){
            musicVolume = SaveData.getMusicVolume();
            soundEffectsVolume = SaveData.getSFXVolume();
        }
        else{
             musicVolume = .5f;
             soundEffectsVolume = 1f;
        }
        //get from save data
        jumpSound = directory.getEntry("jump", SoundEffect.class);
        smallEnemyShootingSound = directory.getEntry("smallEnemyShooting", SoundEffect.class);
        gumSplatSound = directory.getEntry("gumSplat", SoundEffect.class);
        enemySplatSound = directory.getEntry("enemySplat", SoundEffect.class);
        collectItemSound = directory.getEntry("collectItem", SoundEffect.class);
        errorSound = directory.getEntry("error", SoundEffect.class);
        laserFiring = directory.getEntry("laserFiring", SoundEffect.class);
        laserCharging = directory.getEntry("laserCharging", SoundEffect.class);
        laserLocking = directory.getEntry("laserLocking", SoundEffect.class);
        laserThud = directory.getEntry("laserThud", SoundEffect.class);
        shockAttack = directory.getEntry("shockAttack", SoundEffect.class);
        banditShock = directory.getEntry("electricShock", SoundEffect.class);
        failure = directory.getEntry("failure", SoundEffect.class);
        victory = directory.getEntry("victory", SoundEffect.class);
        noGum = directory.getEntry("noGum", SoundEffect.class);
        rolling = directory.getEntry("rolling", SoundEffect.class);
        clockTick = directory.getEntry("clockTick", SoundEffect.class);
        lowStressAlarm = directory.getEntry("lowStressAlarm", SoundEffect.class);
        reloadingGum = directory.getEntry("reloadingGum", SoundEffect.class);
        banditLanding = directory.getEntry("banditLanding", SoundEffect.class);
        doorSound = directory.getEntry("doorSound", SoundEffect.class);
        keyClick = directory.getEntry("keyClick", SoundEffect.class);
        knockback = directory.getEntry("knockback", SoundEffect.class);
        glassSmash = directory.getEntry("glassSmash", SoundEffect.class);
        smash = directory.getEntry("smash", SoundEffect.class);
        shipExplosion = directory.getEntry("shipExplosion", SoundEffect.class);
        shipExhaust = directory.getEntry("shipExhaust", SoundEffect.class);
        banditHurt = directory.getEntry("banditHurt", SoundEffect.class);
        shipDoor = directory.getEntry("shipDoor", SoundEffect.class);
        banditJingle = directory.getEntry("banditJingle", SoundEffect.class);
        pageTurn = directory.getEntry("pageTurn", SoundEffect.class);
        unlockDoor = directory.getEntry("unlockDoor", SoundEffect.class);
        hubbaVictory = directory.getEntry("hubbaVictory", SoundEffect.class);

        soundIds = new HashMap<SoundEffect, Integer>() {{
            put(jumpSound, -1);
            put(smallEnemyShootingSound, -2);
            put(gumSplatSound, -3);
            put(enemySplatSound, -4);
            put(collectItemSound, -5);
            put(errorSound, -6);
            put(laserFiring, -7);
            put(laserCharging, -8);
            put(laserLocking, -9);
            put(laserThud, -10);
            put(shockAttack, -11);
            put(banditShock, -12);
            put(failure, -13);
            put(victory, -14);
            put(noGum, -15);
            put(rolling, -16);
            put(clockTick, -17);
            put(lowStressAlarm, -18);
            put(reloadingGum, -19);
            put(banditLanding, -20);
            put(doorSound, -21);
            put(keyClick, -22);
            put(knockback, -23);
            put(glassSmash, -24);
            put(smash, -25);
            put(shipExplosion, -26);
            put(shipExhaust, -27);
            put(banditHurt, -28);
            put(shipDoor, -29);
            put(banditJingle, -30);
            put(unlockDoor, -31);
            put(pageTurn, -32);
            put(hubbaVictory, -33);
        }};

        sounds = new HashMap<String, SoundEffect>() {{
            put("pageTurn", pageTurn);
            put("jump", jumpSound);
            put("smallEnemyShooting", smallEnemyShootingSound);
            put("gumSplat", gumSplatSound);
            put("enemySplat", enemySplatSound);
            put("collectItem", collectItemSound);
            put("error", errorSound);
            put("laserFiring", laserFiring);
            put("laserCharging", laserCharging);
            put("laserLocking", laserLocking);
            put("laserThud", laserThud);
            put("shockAttack", shockAttack);
            put("banditShock", banditShock);
            put("failure", failure);
            put("victory", victory);
            put("noGum", noGum);
            put("rolling", rolling);
            put("clockTick", clockTick);
            put("lowStressAlarm", lowStressAlarm);
            put("reloadingGum", reloadingGum);
            put("banditLanding", banditLanding);
            put("doorSound", doorSound);
            put("keyClick", keyClick);
            put("knockback", knockback);
            put("glassSmash", glassSmash);
            put("smash", smash);
            put("shipExplosion", shipExplosion);
            put("shipExhaust", shipExhaust);
            put("banditHurt", banditHurt);
            put("shipDoor", shipDoor);
            put("banditJingle", banditJingle);
            put("unlockDoor", unlockDoor);
            put("hubbaVictory", hubbaVictory);
        }};

       menu = directory.getEntry( "menu", AudioSource.class );
       menuIntro = directory.getEntry( "menuIntro", AudioSource.class );
       game = directory.getEntry( "inGame", AudioSource.class );
       gameIntro = directory.getEntry( "inGameIntro", AudioSource.class );
       escape = directory.getEntry( "escape", AudioSource.class );

        music = new HashMap<String, AudioSource>() {{
            put("menu", menu);
            put("menuIntro", menu);
            put("game", game);
            put("gameIntro", game);
            put("escape", escape);
        }};
       engine = (AudioEngine) Gdx.audio;
       musicPlayer = engine.newMusicBuffer( false, 44100 );
    }

    public static void playMusic(String sound){
        AudioSource sample = music.get(sound);
        if (musicPlayer.getCurrent() == sample) {
            musicPlayer.play();
            return;
        }
        musicPlayer.clearSources();
        musicPlayer.setLooping(true);
        musicPlayer.addSource(sample);
        musicPlayer.setVolume(musicVolume);
        musicPlayer.play();

    }

    public static void pauseMusic() {
        musicPlayer.stop();
    }

    public static void loopSound(String sound, int soundId) {
        SoundEffect s = sounds.get(sound);
        s.setLooping(soundId, true);
    }

    public static long playSound(String sound, float volume) {
        SoundEffect s = sounds.get(sound);
        int soundId = soundIds.get(s);
        if (!(soundId == lastPlayed)) {
            return playSound(s,soundId, volume * soundEffectsVolume);
        }
        return 0;
    }

    /**mark what the most last played sound was, used to ensure sounds do not get played over each other*/
    public static void lastPlayed(int soundId) {
        lastPlayed = soundId;
    }

    public static int lastPlayed() {
        return lastPlayed;
    }


    /**
     * Method to ensure that a sound asset is only played once.
     * <p>
     * Every time you play a sound asset, it makes a new instance of that sound.
     * If you play the sounds to close together, you will have overlapping copies.
     * To prevent that, you must stop the sound before you play it again.  That
     * is the purpose of this method.  It stops the current instance playing (if
     * any) and then returns the id of the new instance for tracking.
     *
     * @param sound   The sound asset to play
     * @param soundId The previously playing sound instance
     * @return the new sound instance for this asset.
     */
    public static long playSound(SoundEffect sound, long soundId) {
        return playSound(sound, soundId, soundEffectsVolume);
    }

    /**
     * Method to ensure that a sound asset is only played once.
     * <p>
     * Every time you play a sound asset, it makes a new instance of that sound.
     * If you play the sounds to close together, you will have overlapping copies.
     * To prevent that, you must stop the sound before you play it again.  That
     * is the purpose of this method.  It stops the current instance playing (if
     * any) and then returns the id of the new instance for tracking.
     *
     * @param sound   The sound asset to play
     * @param soundId The previously playing sound instance
     * @param volume  The sound volume
     * @return the new sound instance for this asset.
     */
    public static long playSound(SoundEffect sound, long soundId, float volume) {
        if (sound.isPlaying(soundId)) {
            sound.stop(soundId);
        }
        return sound.play(volume);
    }

    /** Stop sound that is playing */
    public static void stopSound(String sound) {
        SoundEffect effect = sounds.get(sound);
        effect.stop();
    }


    /**
     * Called when the Screen is paused.
     * <p>
     * This is usually when it's not active or visible on screen. An Application is
     * also paused before it is destroyed.
     */
    public static void pause() {
        // We need this method to stop all sounds when we pause.
        for (SoundEffect key : soundIds.keySet()) {
            if (key.isPlaying(soundIds.get(key))) {
                key.stop(soundIds.get(key));
            }
        }
    }

    public static void setMusicVolume(float volume)
    {
        musicVolume = volume;
        musicPlayer.setVolume(volume);
    }

    public static void setEffectsVolume(float volume){
      soundEffectsVolume = volume;
    }

}
