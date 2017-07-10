package guichaguri.trackplayer.logic.workers;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat.MediaItem;
import android.support.v4.media.MediaBrowserServiceCompat;
import guichaguri.trackplayer.logic.MediaManager;
import guichaguri.trackplayer.logic.Utils;
import java.util.Collections;
import java.util.List;

/**
 * The main service!
 * @author Guilherme Chaguri
 */
public class PlayerService extends MediaBrowserServiceCompat {

    private MediaManager manager;

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new BrowserRoot("browser-" + getPackageName(), null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaItem>> result) {
        //TODO
        result.sendResult(Collections.<MediaItem>emptyList());
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
