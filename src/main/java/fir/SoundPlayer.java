package fir;

import com.google.common.eventbus.Subscribe;
import com.google.common.io.Resources;
import fir.evt.PlaySoundEvent;

import java.applet.Applet;
import java.applet.AudioClip;

/**
 * Created by kongzheng on 16/9/2.
 */
public class SoundPlayer {

    private String fileName = "sound.wav";
    private AudioClip clip;

    public SoundPlayer() {
        try {
            clip = Applet.newAudioClip(Resources.getResource(fileName));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void onEvent(PlaySoundEvent e) {
        clip.play();
    }
}
