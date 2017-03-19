package guichaguri.trackplayer.metadata;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import guichaguri.trackplayer.logic.PlayerService;
import guichaguri.trackplayer.metadata.components.MediaNotification;

/**
 * @author Guilherme Chaguri
 */
public class MetadataManager {

    private final PlayerService service;
    private final NoisyReceiver noisyReceiver = new NoisyReceiver();

    private Metadata metadata; //TODO

    public MetadataManager(PlayerService service) {
        this.service = service;
    }

    public void onPlay() {
        MediaNotification notification = metadata.getNotification();

        // Set the service as foreground, updating and showing the notification
        service.startForeground(MediaNotification.NOTIFICATION_ID, notification.build());
        notification.setShowing(true);

        // Activate the session
        metadata.setEnabled(true);

        // Register the noisy receiver for receiving a speaker event
        IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        service.registerReceiver(noisyReceiver, filter);
    }

    public void onPause() {
        // Set the service as background, keeping the notification
        service.stopForeground(false);

        // Unregister the noisy receiver
        service.unregisterReceiver(noisyReceiver);
    }

    public void onStop() {
        // Set the service as background, removing the notification
        metadata.getNotification().setShowing(false);
        service.stopForeground(true);

        // Deactivate the session
        metadata.setEnabled(false);

        // Unregister the noisy receiver
        service.unregisterReceiver(noisyReceiver);
    }

    public void onCommand(Intent intent) {
        if(intent.hasExtra(Intent.EXTRA_KEY_EVENT)) {
            metadata.handleIntent(intent);
        }
    }

    public void onDestroy() {
        metadata.destroy();
    }


    private class NoisyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            metadata.getControls().pause();
        }
    }

}
