package guichaguri.trackplayer.logic;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.media.session.MediaButtonReceiver;
import guichaguri.trackplayer.metadata.Metadata;
import guichaguri.trackplayer.player.Player;

/**
 * @author Guilherme Chaguri
 */
public class PlayerService extends Service {

    public static final String MEDIA_BUTTON = "track_player_media_button";

    private Metadata metadata;
    private Player player;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.hasExtra(MEDIA_BUTTON)) {

        } else if(intent.hasExtra(Intent.EXTRA_KEY_EVENT)) {
            MediaButtonReceiver.handleIntent(null, intent);//TODO
        }
        return START_REDELIVER_INTENT;
    }

    public void onPlay() {
        startForeground(0, null);//TODO show notification
    }

    public void onPause() {
        stopForeground(false);
    }

    public void onStop() {
        stopForeground(true);
    }

    @Override
    public void onDestroy() {

    }
}
