package guichaguri.trackplayer.metadata.components;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.support.v4.media.session.MediaSessionCompat;

/**
 * @author Guilherme Chaguri
 */
public class NoisyReceiver extends BroadcastReceiver {

    private final Context context;
    private final MediaSessionCompat session;

    private boolean registered = false;

    public NoisyReceiver(Context context, MediaSessionCompat session) {
        this.context = context;
        this.session = session;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // Trigger a pause event
        session.getController().getTransportControls().pause();
    }

    public void setEnabled(boolean enabled) {
        // Ignore if the receiver is already registered/unregistered
        if(enabled == registered) return;

        if(enabled) {
            // Register the receiver to only receive the noisy intent
            IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
            context.registerReceiver(this, filter);
        } else {
            // Unregister the receiver
            context.unregisterReceiver(this);
        }
        registered = enabled;
    }

}
