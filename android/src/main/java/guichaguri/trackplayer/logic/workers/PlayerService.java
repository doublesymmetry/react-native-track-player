package guichaguri.trackplayer.logic.workers;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import guichaguri.trackplayer.logic.MediaManager;
import guichaguri.trackplayer.logic.components.MediaWrapper;
import guichaguri.trackplayer.logic.components.VideoWrapper;

/**
 * @author Guilherme Chaguri
 */
public class PlayerService extends Service {

    public static final String ACTION_MEDIA = "track-player-media";
    public static final String ACTION_VIDEO = "track-player-video";

    private MediaManager manager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        String action = intent.getAction();

        if(action.equals(ACTION_MEDIA)) {
            return new MediaWrapper(manager);
        } else if(action.equals(ACTION_VIDEO)) {
            return new VideoWrapper(manager);
        }
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        manager.onCommand(intent);

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onCreate() {
        manager = new MediaManager(this);
    }

    @Override
    public void onDestroy() {
        manager.onServiceDestroy();
        manager = null;
    }

}
