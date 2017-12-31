package guichaguri.trackplayer.logic.components;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.util.Log;
import guichaguri.trackplayer.logic.MediaManager;
import guichaguri.trackplayer.logic.Utils;
import guichaguri.trackplayer.player.Playback;

/**
 * @author Guilherme Chaguri
 */
public class FocusManager implements OnAudioFocusChangeListener {

    private final Context context;
    private final MediaManager manager;

    private boolean hasAudioFocus = false;

    private boolean paused = false;
    private boolean ducking = false;
    private float originalVolume = 1;

    public FocusManager(Context context, MediaManager manager) {
        this.context = context;
        this.manager = manager;
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
        Playback pb = manager.getPlayback();
        if(pb == null) return;

        switch(focus) {
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                Log.d(Utils.TAG, "Audio focus loss, triggering pause");
                paused = true;
                pb.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                Log.d(Utils.TAG, "Audio focus loss, triggering duck");
                ducking = true;
                originalVolume = pb.getVolume();
                pb.setVolume(originalVolume / 2);
                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                if(paused) {
                    Log.d(Utils.TAG, "Audio focus gain, triggering play");
                    paused = false;
                    pb.play();
                }

                if(ducking) {
                    Log.d(Utils.TAG, "Audio focus gain, triggering duck");
                    ducking = false;
                    pb.setVolume(originalVolume);
                }
                break;
        }
    }
}
