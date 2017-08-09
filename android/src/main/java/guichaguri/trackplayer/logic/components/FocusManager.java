package guichaguri.trackplayer.logic.components;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.os.Bundle;
import android.util.Log;
import guichaguri.trackplayer.logic.Events;
import guichaguri.trackplayer.logic.Utils;
import guichaguri.trackplayer.metadata.Metadata;

/**
 * @author Guilherme Chaguri
 */
public class FocusManager implements OnAudioFocusChangeListener {

    private final Context context;
    private final Metadata metadata;

    private boolean hasAudioFocus = false;

    private boolean paused = false;
    private boolean ducking = false;

    public FocusManager(Context context, Metadata metadata) {
        this.context = context;
        this.metadata = metadata;
    }

    public boolean enable() {
        if(hasAudioFocus) return true;
        Log.d(Utils.TAG, "Requesting audio focus...");

        AudioManager manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        int r = manager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        if(r == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.d(Utils.TAG, "Audio focus granted!");
            hasAudioFocus = true;
            return true;
        }
        Log.d(Utils.TAG, "Audio focus request failed!");
        return false;
    }

    public void disable() {
        if(!hasAudioFocus) return;
        Log.d(Utils.TAG, "Abandoning audio focus...");

        AudioManager manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        int r = manager.abandonAudioFocus(this);
        hasAudioFocus = r != AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    @Override
    public void onAudioFocusChange(int focus) {
        switch(focus) {
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                Log.d(Utils.TAG, "Audio focus loss, triggering pause");
                paused = true;
                metadata.getControls().pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                Log.d(Utils.TAG, "Audio focus loss, triggering duck");
                ducking = true;
                onDuck();
                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                if(paused) {
                    Log.d(Utils.TAG, "Audio focus gain, triggering play");
                    paused = false;
                    metadata.getControls().play();
                }

                if(ducking) {
                    Log.d(Utils.TAG, "Audio focus gain, triggering duck");
                    ducking = false;
                    onDuck();
                }
                break;
        }
    }

    private void onDuck() {
        Bundle bundle = new Bundle();
        bundle.putBoolean("ducking", ducking);
        Events.dispatchEvent(context, Events.BUTTON_DUCK, bundle);
    }
}
