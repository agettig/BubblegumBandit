package edu.cornell.gdiac.bubblegumbandit.controllers;

import com.badlogic.gdx.Gdx;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.audio.*;

import java.util.HashMap;
import java.util.Map;

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
     * The sound when robot is hit with gume.  We only want to play once.
     */
    private static SoundEffect robotSplatSound;

    /**
     * The sound when an item is collected.  We only want to play once.
     */
    private static SoundEffect collectItemSound;


    /**Hashmap holding sounds and corresponding Id*/
    private static HashMap<SoundEffect, Integer> soundIds;

    /**Hashmap holding sounds and corresponding string */
    private static HashMap<String, SoundEffect> sounds ;


    // music

    /** menu music: SpaceCruising */
   private static AudioSource menu;

    /** in-game music: BubbleGumBallad */
    private static AudioSource game;

    /** alarm music: escape! */
    private static AudioSource escape;

   /** engine*/
   private static AudioEngine engine;

    /** A queue to play music */
    private static MusicQueue musicPlayer;

    /**Hashmap holding sounds and corresponding string */
    private static HashMap<String, AudioSource> music ;
    public SoundController() {}

    public static void initialize(AssetDirectory directory) {
        jumpSound = directory.getEntry("jump", SoundEffect.class);
        smallEnemyShootingSound = directory.getEntry("smallEnemyShooting", SoundEffect.class);
        gumSplatSound = directory.getEntry("gumSplat", SoundEffect.class);
        robotSplatSound = directory.getEntry("robotSplat", SoundEffect.class);
        collectItemSound = directory.getEntry("collectItem", SoundEffect.class);

        soundIds = new HashMap<SoundEffect, Integer>() {{
            put(jumpSound, -1);
            put(smallEnemyShootingSound, -2);
            put(gumSplatSound, -3);
            put(robotSplatSound, -4);
            put(collectItemSound, -5);
        }};

        sounds = new HashMap<String, SoundEffect>() {{
            put("jump", jumpSound);
            put("smallEnemyShooting", smallEnemyShootingSound);
            put("gumSplat", gumSplatSound);
            put("robotSplat", robotSplatSound);
            put("collectItem", collectItemSound);
        }};

       menu = directory.getEntry( "menu", AudioSource.class );
       game = directory.getEntry( "inGame", AudioSource.class );
       escape = directory.getEntry( "escape", AudioSource.class );

        music = new HashMap<String, AudioSource>() {{
            put("menu", menu);
            put("game", game);
            put("escape", escape);
        }};
       engine = (AudioEngine) Gdx.audio;
       musicPlayer = engine.newMusicBuffer( false, 44100 );
    }

    public static void playMusic(String sound){
        musicPlayer.clearSources();
        AudioSource sample = music.get(sound);
        musicPlayer.setLooping(true);
        musicPlayer.addSource(sample);
        musicPlayer.play();
    }

    public static long playSound(String sound, float volume) {
        SoundEffect s = sounds.get(sound);
        return playSound(s, soundIds.get(s), volume);
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
        return playSound(sound, soundId, 1.0f);
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
        if (soundId != -1 && sound.isPlaying(soundId)) {
            sound.stop(soundId);
        }
        return sound.play(volume);
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

}
