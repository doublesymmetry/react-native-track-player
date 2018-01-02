package guichaguri.trackplayer.metadata.components;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import guichaguri.trackplayer.logic.Events;
import android.os.Bundle;

/**
 * @author Guilherme Chaguri
 */
public class MusicIntentReceiver extends BroadcastReceiver {

    private final Context context;
    private final MediaSessionCompat session;
    private static final String ACTION_HEADSET_PLUG = AudioManager.ACTION_HEADSET_PLUG;

    private boolean registered = false;

    public MusicIntentReceiver(Context context, MediaSessionCompat session) {
        this.context = context;
        this.session = session;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // Trigger a pause event

        Bundle bundle = new Bundle();

        if (ACTION_HEADSET_PLUG.equals(intent.getAction())) {
            int state = intent.getIntExtra("state", -1);
            switch (state) {
            case 0:
                Events.dispatchEvent(context, Events.HEADSET_PLUGGED_OUT, bundle);
                break;
            case 1:
                Events.dispatchEvent(context, Events.HEADSET_PLUGGED_IN, bundle);
                break;
            default:
                Log.d("####", "I have no idea what the headset state is" + state);
            }
        }
    }

    public void setEnabled(boolean enabled) {
        // Ignore if the receiver is already registered/unregistered

        if (enabled == registered)
            return;

        if (enabled) {

            IntentFilter filter = new IntentFilter(AudioManager.ACTION_HEADSET_PLUG);
            context.registerReceiver(this, filter);
        } else {
            // Unregister the receiver
            context.unregisterReceiver(this);
        }
        registered = enabled;
    }

}
