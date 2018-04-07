package guichaguri.trackplayer.logic.services;

import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;
import guichaguri.trackplayer.logic.MediaManager;
import guichaguri.trackplayer.logic.Utils;
import guichaguri.trackplayer.logic.components.MediaWrapper;
import javax.annotation.Nullable;

/**
 * The main service!
 * @author Guilherme Chaguri
 */
public class PlayerService extends HeadlessJsTaskService {

    public static final String ACTION_CONNECT = "trackplayer.connect";

    private MediaManager manager;

    @Nullable
    @Override
    protected HeadlessJsTaskConfig getTaskConfig(Intent intent) {
        return new HeadlessJsTaskConfig("TrackPlayer", Arguments.createMap(), 0, true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        String action = intent.getAction();

        if(ACTION_CONNECT.equals(action)) {
            Log.d(Utils.TAG, "onBind");
            return new MediaWrapper(this, manager);
        } else {
            return super.onBind(intent);
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(Utils.TAG, "onUnbind");
        super.onUnbind(intent);
        manager.onServiceUnbounded();
        return true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(Utils.TAG, "Service command (" + (intent != null ? intent.getAction() : "Unknown") + ")");

        manager.onCommand(intent);

        if(intent == null) {
            return super.onStartCommand(intent, flags, startId);
        }

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.d(Utils.TAG, "Service init");

        super.onCreate();

        manager = new MediaManager(this);
    }

    @Override
    public void onDestroy() {
        Log.d(Utils.TAG, "Service destroy");

        manager.onServiceDestroy();
        manager = null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(Utils.TAG, "Task removed");

        if(manager.shouldStopWithApp()) {
            stopSelf();
        }
    }
}
