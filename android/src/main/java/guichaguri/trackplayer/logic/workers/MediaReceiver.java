package guichaguri.trackplayer.logic.workers;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import guichaguri.trackplayer.logic.Utils;

/**
 * Receives media buttons
 * @author Guilherme Chaguri
 */
public class MediaReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if(Intent.ACTION_MEDIA_BUTTON.equals(action)) {

            Utils.log("Media button received, sending it to the service...");

            // Sends media keys to the service
            intent.setComponent(new ComponentName(context, PlayerService.class));
            context.startService(intent);

        }
    }

}