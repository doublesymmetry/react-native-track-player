package guichaguri.trackplayer.logic.workers;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

/**
 * Receives media buttons
 * @author Guilherme Chaguri
 */
public class MediaReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if(Intent.ACTION_MEDIA_BUTTON.equals(action)) {

            // Sends media keys to the service
            intent.setComponent(new ComponentName(context, PlayerService.class));
            context.startService(intent);

        }
    }

}