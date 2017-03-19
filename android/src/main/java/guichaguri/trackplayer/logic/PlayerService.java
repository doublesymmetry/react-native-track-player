package guichaguri.trackplayer.logic;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import guichaguri.trackplayer.metadata.MetadataManager;
import guichaguri.trackplayer.player.PlayerManager;

/**
 * @author Guilherme Chaguri
 */
public class PlayerService extends Service {

    private MetadataManager metadata; //TODO
    private PlayerManager player; //TODO

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        metadata.onCommand(intent);

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        metadata.onDestroy();
    }

}
