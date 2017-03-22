package guichaguri.trackplayer.logic.components;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
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

        AudioManager manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        int r = manager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        if(r == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            hasAudioFocus = true;
            return true;
        }
        return false;
    }

    public void disable() {
        if(!hasAudioFocus) return;

        AudioManager manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        manager.abandonAudioFocus(this);
        hasAudioFocus = false;
    }

    @Override
    public void onAudioFocusChange(int focus) {
        switch(focus) {
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                paused = true;
                metadata.getControls().pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                ducking = true;
                // TODO duck
                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                if(paused) {
                    paused = false;
                    metadata.getControls().play();
                }

                if(ducking) {
                    ducking = false;
                    // TODO unduck
                }
                break;
        }
    }
}
