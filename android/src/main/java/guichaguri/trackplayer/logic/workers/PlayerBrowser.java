package guichaguri.trackplayer.logic.workers;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat.MediaItem;
import android.support.v4.media.MediaBrowserServiceCompat;
import guichaguri.trackplayer.logic.components.BrowserWrapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Guilherme Chaguri
 */
public class PlayerBrowser extends MediaBrowserServiceCompat implements ServiceConnection {

    private boolean serviceEnabled = false;

    private BrowserWrapper browser;
    private List<Result<List<MediaItem>>> toProcess = null;

    @Override
    public void onDestroy() {
        enableService(false);

        super.onDestroy();
    }

    private void enableService(boolean enable) {
        if(serviceEnabled == enable) return;

        if(enable) {
            Intent intent = new Intent(this, PlayerService.class);
            intent.setAction(PlayerService.ACTION_BROWSER);
            bindService(intent, this, Service.BIND_AUTO_CREATE);
        } else {
            unbindService(this);
        }
        serviceEnabled = enable;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        browser = (BrowserWrapper)service;

        if(toProcess != null) {
            browser.sendQueue(toProcess);
            toProcess = null;
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        browser = null;

        if(toProcess != null) {
            for(Result<List<MediaItem>> result : toProcess) {
                result.sendResult(Collections.<MediaItem>emptyList());
            }
            toProcess = null;
        }
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new BrowserRoot("browser-" + getPackageName(), null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaItem>> result) {
        if(browser == null) {
            result.detach();

            if(toProcess == null) toProcess = new ArrayList<>();
            toProcess.add(result);

            enableService(true);
        } else {
            browser.sendQueue(result);
        }
    }
}
