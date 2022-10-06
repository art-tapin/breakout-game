/* File: Music.java
 * This is a simple class that adds the music files support
 * It has simple interface:
 * - play audio-file
 * - stop audio-file
 * - play audio-file in a loop of 100 (significant amount of time)
 */
package com.shpp.p2p.cs.atiupin.assignment4;

import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;


public class Music {

    Clip clip;

    public void setFile(String soundFileName){
        try{
            File file = new File(soundFileName);
            AudioInputStream sound = AudioSystem.getAudioInputStream(file);
            clip = AudioSystem.getClip();
            clip.open(sound);
        }
        catch(Exception e){
            System.out.println("File wasn't open properly. Check the path name or existence of the file");
        }
    }

    /**
     * Play audio clip until .stop() method is called (or if the file will be played for 100 times)
     */
    public void playUntilStop() {

        clip.setFramePosition(0);
        clip.start();
        clip.loop(100);
    }

    /**
     * Play audio clip
     */
    public void play() {

        clip.setFramePosition(0);
        clip.start();

    }

    /**
     * Stop playing the audio clip
     */
    public void stop() {
        clip.stop();
    }

}