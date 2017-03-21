package guichaguri.trackplayer.logic;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * @author Guilherme Chaguri
 */
public class PlayerService extends Service {

    private MediaManager manager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return manager;
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
