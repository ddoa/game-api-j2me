/*
 * File: 	GameSound.java
 * Created: Jan 26, 2005
 */
package phonegame;

import java.io.*;

/**
 * This class features a number of static methods that can be used to add sound to your game. Please notice that <u>not
 * </u> all phones support the use of audio in Java.
 * <p>
 * <i>Only MIDP 2.0 phones and phones that implement the MMAPI optional package (JSR 135) support the playback of
 * audiofiles / tones. If you use this class on phones that do not meet these requirements you simply won't hear any
 * sound. </i>
 * 
 * @author Tim Prijn, Richard Kettelerij & Paul Bergervoet
 * @version 2.0, October 11, 2005
 * @version 2.1, April 24, 2006
 * @version 3.0, November 17, 2006
 */
public final class GameSound
{
    /**
     * required by the Singleton pattern
     */ 
    private static GameSound instance;

    /**
     * The actual mediaplayer
     */
    private static AudioPlayer player;
    
    /**
     * is there a background sound being played indefinitively?
     */
    private static boolean playsBackgroundSound = false;
    
    /**
     * Has the background sound been paused?
     */
    private static boolean backgroundSoundPaused = false;
    
    /**
     * The name of the file containing the background sound
     */
    private static String backgroundSound = "";

    /**
     * Constructs an instance of this class
     */
    private GameSound()
    {
        // do nothing
    }

    /**
     * Play the given MIDI soundfile.
     * 
     * @param midiFile
     *                the file that needs to be played
     * @param stopPrevious
     *                true, if you want the previous sound(s) to be stopped (this maximizes compatibility with <i>older </i>
     *                phones), false if not
     * @param playIndefinitely
     *                true if the sound should play continuesly, false if not
     * @throws GameException
     *                 if the midifile is invalid or if the device can't handle it
     */
    public static final void playSound(String midiFile, boolean stopPrevious, boolean playIndefinitely)
            throws GameException
    {
        // check file extension & device support
        if (midiFile.endsWith(".mid") || midiFile.endsWith(".midi"))
        {
            if (canPlaySounds())
            {
                if ( playIndefinitely )
                {
                    playsBackgroundSound = true;
                    backgroundSound = midiFile;
                }
                getPlayer().playSound(midiFile, stopPrevious, playIndefinitely);
            }
        } else
        {
            throw new GameException("Invalid MIDI file found, soundfiles must end " + "with .mid or .midi extension");
        }
    }

    /**
     * Playback a tone as specified by a note and its duration. A note is given in the range of 0 to 127 inclusive. A
     * list of MIDI note numbers can be found <a href="http://www.harmony-central.com/MIDI/Doc/table2.html">here </a>.
     * <p>
     * Notice that this method may utilize CPU resources significantly on devices that don't have hardware support for
     * tone generation.
     * 
     * @param note
     *                defines the tone of the note
     * @param duration
     *                the duration of the tone in milli-seconds (must be positive)
     * @param volume
     *                audio volume range from 0 to 100. 100 represents the maximum volume at the current hardware level.
     *                Setting the volume to a value less than 0 will set the volume to 0. Setting the volume to greater than
     *                100 will set the volume to 100.
     * @throws GameException
     *                 if note/duration is incorrent or if there's a device related problem
     */
    public static final void playTone(int note, int duration, int volume) throws GameException
    {
        if (canPlaySounds())
        {
            getPlayer().playTone(note, duration, volume);
        }
    }

    /**
     * Stop playing sounds, especially background music. You can specify if you want
     * to resume the sound later on with resumeBackgroundSound(). (The GameEngine
     * does this at an incoming call). <br/>
     * Does nothing if no background sound is playing.
     *  
     * @see phonegame.GameSound#resumeBackgroundSound()
     * 
     * @param temporarily true if you want to resume the sound later on, false if you really stop it
     */
    public static final void stopBackgroundSound(boolean temporarily)
    {
        if ( player != null && playsBackgroundSound)
        {	
            player.stopPlaying(temporarily);
            backgroundSoundPaused = true;
        }
    }

    /**
     * Resume playing a background sound or music, that has been stopped temorarily. <br/>
     * Does nothing if there is no background sound or if it is already playing, 
     * 
     * @see phonegame.GameSound#stopBackgroundSound(boolean temporarily)
     */
    public static final void resumeBackgroundSound()
    {
        if ( playsBackgroundSound && backgroundSoundPaused )
        { 
            playSound(backgroundSound, false, true);
            backgroundSoundPaused = false;
    	}
    }

    /**
     * Checks if a soundfile/tone is currently being played.
     * 
     * @return true if sound is being played, false otherwise
     */
    public static final boolean isPlaying()
    {
        if ( player == null )
            return false;
        return getPlayer().isPlaying();
    }

    /**
     * Returns an instance of the private AudioPlayer class.
     * <br/>  Note: the GameSound class must 
     * also be instantiated (in this method) in order to create an instance of AudioPlayer.
     * 
     * @return an instance of the AudioPlayer class
     */
    private static final AudioPlayer getPlayer()
    {
        if ( player == null)
        {
            if ( instance == null )
            {    instance = new GameSound(); // required to create AudioPlayer
            }
            player = instance.new AudioPlayer();
        }

        return player;
    }

    /**
     * Check if the current device is capable of playing audio.
     * 
     * @return true if the device is capable of playing audio, false if otherwise
     */
    private static final boolean canPlaySounds()
    {
        // quick 'n dirty hack - required to check if MMAPI is available because
        // some early Nokia Series40 phones can't deal with the Class.forName test
        if (System.getProperty("supports.mixing") != null)
        {
            try
            {
                // test if the Manager class (which is a vital part of MMAPI) exists
                Class.forName("javax.microedition.media.Manager");
                return true;
            } catch (Exception e)
            {
                return false;
            }
        } else
        {
            return false;
        }
    }

    /**
     * The AudioPlayer contains the MMAPI code and performs the actual playback of audiofiles.
     * 
     * Note: All MMAPI code is put in this separate class because some non-MMAPI phones (early Nokia Series40 that I
     * know of) can't deal with embedded MMAPI code.
     * 
     * @author Tim Prijn & Richard Kettelerij
     * @version 1.0
     */
    private final class AudioPlayer
    {
        private static final String MIDI_CONTENTTYPE = "audio/midi";

        private javax.microedition.media.Player player;

        private Class thisClass;

        /**
         * Constructs an instance of this class
         */
        public AudioPlayer()
        {
            thisClass = getClass();
        }
        
        private void stopPlaying(boolean temporarily)
        {
            playsBackgroundSound = temporarily;
            try
            {	
                player.stop();
                player.close();
                player = null; 		// to ensure that a fresh call to playSound creates a new one
            } catch ( javax.microedition.media.MediaException e)
            {}
        }

        private boolean isPlaying()
        {
            return (player.getState() == javax.microedition.media.Player.STARTED);
        }

        /**
         * @see nl.han.ica.propedeuse.gameapi.GameSound#playTone(int, int, int)
         */
        private final void playTone(int note, int duration, int volume) throws GameException
        {
            try
            {
                javax.microedition.media.Manager.playTone(note, duration, volume);
            } catch (IllegalArgumentException iae)
            {
                throw new GameException("Invalid note and/or duration parameter");
            } catch (javax.microedition.media.MediaException e)
            {
                throw new GameException("Tone cannot be played due a device related problem "
                        + " (i.e insufficient hardware support)");
            }
        }

        /**
         * @see nl.han.ica.propedeuse.gameapi.GameSound#playSound(String, boolean, boolean)
         */
        private final void playSound(String midiFile, boolean stopPrevious, boolean playIndefinitely)
                throws GameException
        {
            try
            {
                // stop any previous files if needed
                if (player != null && stopPrevious)
                    player.stop();

                // load (new) file from JAR
                InputStream stream = thisClass.getResourceAsStream(midiFile);
                player = javax.microedition.media.Manager.createPlayer(stream, MIDI_CONTENTTYPE);

                if (playIndefinitely)
                    player.setLoopCount(-1); // repeat

                // play it!
                player.start();
            } catch (IOException ioe)
            {
                throw new GameException("MIDI file not found at the given location");
            } catch (javax.microedition.media.MediaException e)
            {
                throw new GameException("Tone cannot be played due a device related problem "
                        + " (i.e insufficient hardware support)");
            }
        }

    }
}