package com.guichaguri.trackplayer.service;

import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import com.facebook.react.bridge.Promise;
import com.guichaguri.trackplayer.service.player.ExoPlayback;

/**
 * @author Guichaguri
 */
public class MusicBinder extends Binder {

    private final MusicManager manager;
    private final Handler handler;

    public MusicBinder(MusicManager manager) {
        this.manager = manager;
        this.handler = new Handler();
    }

    public void post(Runnable r) {
        this.handler.post(r);
    }

    public ExoPlayback getPlayback() {
        ExoPlayback playback = manager.getPlayback();

        // TODO remove?
        if(playback == null) {
            playback = manager.createLocalPlayback(new Bundle());
            manager.switchPlayback(playback);
        }

        return playback;
    }

    public void setupPlayer(Bundle bundle, Promise promise) {
        manager.switchPlayback(manager.createLocalPlayback(bundle));
        promise.resolve(null);
    }

    public void updateOptions(Bundle bundle) {
        manager.getMetadata().updateOptions(bundle);
    }

    public int getRatingType() {
        return manager.getMetadata().getRatingType();
    }

    public void destroy() {
        handler.removeMessages(0);
        manager.destroy();
    }

}
