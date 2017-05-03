package guichaguri.trackplayer.logic.workers;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import guichaguri.trackplayer.logic.MediaManager;
import guichaguri.trackplayer.logic.Utils;
import guichaguri.trackplayer.logic.components.BrowserWrapper;
import guichaguri.trackplayer.logic.components.MediaWrapper;
import guichaguri.trackplayer.logic.components.VideoWrapper;

/**
 * @author Guilherme Chaguri
 */
public class PlayerService extends Service {

    public static final String ACTION_MEDIA = "track-player-media";
    public static final String ACTION_VIDEO = "track-player-video";
    public static final String ACTION_BROWSER = "track-player-browser";

    private MediaManager manager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        String action = intent.getAction();

        Utils.log("Service bound (%s)", action);

        if(action.equals(ACTION_MEDIA)) {
            return new MediaWrapper(manager);
        } else if(action.equals(ACTION_VIDEO)) {
            return new VideoWrapper(manager);
        } else if(action.equals(ACTION_BROWSER)) {
            return new BrowserWrapper(this, manager);
        }
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Utils.log("Service command (%s)", intent != null ? intent.getAction() : null);

        manager.onCommand(intent);

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Utils.log("Service init");

        manager = new MediaManager(this);
    }

    @Override
    public void onDestroy() {
        Utils.log("Service destroy");

        manager.onServiceDestroy();
        manager = null;
    }

}
