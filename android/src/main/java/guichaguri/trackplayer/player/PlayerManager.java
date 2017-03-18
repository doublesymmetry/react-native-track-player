package guichaguri.trackplayer.player;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import guichaguri.trackplayer.logic.Utils;

/**
 * @author Guilherme Chaguri
 */
public class PlayerManager implements OnAudioFocusChangeListener {

    private final Context context;
    private Player[] players = new Player[0];

    private boolean hasAudioFocus = false;

    public PlayerManager(Context context) {
        this.context = context;
    }

    public boolean isPlaying() {
        for(Player p : players) {
            if(Utils.isPlaying(p.getState())) return true;
        }
        return false;
    }

    public boolean onPlay(Player player) {
        if(player instanceof RemotePlayer) return true;
        if(hasAudioFocus) return true;

        AudioManager manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        int r = manager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        if(r == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            hasAudioFocus = true;
            return true;
        }
        return false;
    }

    public void onStop(Player player) {
        if(!hasAudioFocus) return;

        AudioManager manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        manager.abandonAudioFocus(this);
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
