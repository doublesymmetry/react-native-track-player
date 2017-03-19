package guichaguri.trackplayer.player.components;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;

/**
 * @author Guilherme Chaguri
 */
public class FocusManager implements OnAudioFocusChangeListener {

    private final Context context;
    private boolean hasAudioFocus = false;

    public FocusManager(Context context) {
        this.context = context;
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
                // TODO pause everything
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // TODO duck everything
                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                // TODO play and "unduck" everything
                break;
        }
    }
}
