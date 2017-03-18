package guichaguri.trackplayer.logic;

import android.content.BroadcastReceiver;
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
            Intent i = new Intent(context, PlayerService.class);
            i.putExtra(Intent.EXTRA_KEY_EVENT, intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT));
            context.startService(i);

        }
    }

}
