package com.guichaguri.trackplayer.service;

import android.os.Binder;
import android.os.Bundle;
import com.facebook.react.bridge.Promise;
import com.guichaguri.trackplayer.service.player.ExoPlayback;
import android.util.Log;

/**
 * @author Guichaguri
 */
public class MusicBinder extends Binder {

    private final MusicService service;
    private final MusicManager manager;

    public MusicBinder(MusicService service, MusicManager manager) {
	Log.d(Utils.LOG, "Binder constructing with service " + service + " and manager " + manager, new Throwable());
        this.service = service;
        this.manager = manager;
    }

    public void post(Runnable r) {
	Log.d(Utils.LOG, "Service = " + service.handler);
	Log.d(Utils.LOG, "Service handler = " + service.handler);
        service.handler.post(r);
    }

    public ExoPlayback getPlayback() {
        return manager.getPlayback();
    }

    public void setupPlayer(Bundle bundle, Promise promise) {
        manager.switchPlayback(manager.createLocalPlayback(bundle));
        promise.resolve(null);
    }

    public void updateOptions(Bundle bundle) {
        manager.setStopWithApp(bundle.getBoolean("stopWithApp", false));
        manager.setAlwaysPauseOnInterruption(bundle.getBoolean("alwaysPauseOnInterruption", false));
        manager.getMetadata().updateOptions(bundle);
    }

    public int getRatingType() {
        return manager.getMetadata().getRatingType();
    }

    public void destroy() {
        service.handler.post(() -> {
                Log.d(Utils.LOG, "Destroying called from " + Thread.currentThread(), new Throwable());
                service.destroy();
                service.stopSelf();
	});
    }
}
