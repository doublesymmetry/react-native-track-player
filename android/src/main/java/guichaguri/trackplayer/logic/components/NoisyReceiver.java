package guichaguri.trackplayer.logic.components;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaControllerCompat.TransportControls;

/**
 * This receiver is registered when the audio starts playing and unregistered when the audio stops playing
 * @author Guilherme Chaguri
 */
public class NoisyReceiver extends BroadcastReceiver {

    private final MediaControllerCompat.TransportControls controls;

    public NoisyReceiver(TransportControls controls) {
        this.controls = controls;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if(AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {

            // Sends a pause key to the service
            controls.pause();

        }

    }
}
