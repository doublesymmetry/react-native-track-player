package com.guichaguri.trackplayer.service;

import android.content.Context;
import android.media.AudioFocusRequest;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import com.guichaguri.trackplayer.module.MusicEvents;
import com.guichaguri.trackplayer.service.metadata.MetadataManager;
import com.guichaguri.trackplayer.service.models.Track;
import com.guichaguri.trackplayer.service.player.ExoPlayback;

/**
 * @author Guichaguri
 */
public class MusicManager {

    private final MusicService service;

    private final WakeLock wakeLock;
    private final WifiLock wifiLock;

    private MetadataManager metadata;
    private ExoPlayback playback;
    private AudioFocusRequest focus;

    public MusicManager(MusicService service) {
        this.service = service;

        PowerManager powerManager = (PowerManager)service.getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "track-player-wake-lock");
        wakeLock.setReferenceCounted(false);

        // Android 7: Use the application context here to prevent any memory leaks
        WifiManager wifiManager = (WifiManager)service.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "track-player-wifi-lock");
        wifiLock.setReferenceCounted(false);
    }

    public ExoPlayback getPlayback() {
        return playback;
    }

    public void onPlay() {
        Log.d(Utils.LOG, "onPlay");

        if(!playback.isRemote()) {
            //TODO audio focus

            if(!wakeLock.isHeld()) wakeLock.acquire();

            if(!Utils.isLocal(playback.getCurrentTrack().uri)) {
                if(!wifiLock.isHeld()) wifiLock.acquire();
            }
        }

        metadata.setForeground(true, true);
    }

    public void onPause() {
        Log.d(Utils.LOG, "onPause");

        // Release the wake and the wifi locks
        if(wakeLock.isHeld()) wakeLock.release();
        if(wifiLock.isHeld()) wifiLock.release();

        // TODO disable focus

        metadata.setForeground(false, true);
    }

    public void onStop() {
        Log.d(Utils.LOG, "onStop");

        // Release the wake and the wifi locks
        if(wakeLock.isHeld()) wakeLock.release();
        if(wifiLock.isHeld()) wifiLock.release();

        // TODO disable focus

        metadata.setForeground(false, false);
    }

    public void onStateChange(int state) {
        Log.d(Utils.LOG, "onStateChange");

        Bundle bundle = new Bundle();
        bundle.putInt("state", state);
        service.emit(MusicEvents.PLAYBACK_STATE, bundle);
    }

    public void onTrackUpdate(Track previous, long prevPos, Track next) {
        Log.d(Utils.LOG, "onTrackUpdate");

        metadata.updateMetadata(next);

        Bundle bundle = new Bundle();
        bundle.putString("track", previous != null ? previous.id : null);
        bundle.putDouble("position", Utils.toSeconds(prevPos));
        bundle.putString("nextTrack", next != null ? next.id : null);
        service.emit(MusicEvents.PLAYBACK_TRACK_CHANGED, bundle);
    }

    public void onEnd(Track previous, long prevPos) {
        Log.d(Utils.LOG, "onEnd");

        Bundle bundle = new Bundle();
        bundle.putString("track", previous != null ? previous.id : null);
        bundle.putDouble("position", Utils.toSeconds(prevPos));
        service.emit(MusicEvents.PLAYBACK_QUEUE_ENDED, bundle);
    }

    public void onError(String code, String error) {
        Log.d(Utils.LOG, "onError");
        Log.e(Utils.LOG, "Playback error: " + code + " - " + error);

        Bundle bundle = new Bundle();
        bundle.putString("code", code);
        bundle.putString("message", error);
        service.emit(MusicEvents.PLAYBACK_ERROR, bundle);
    }

    public void destroy() {
        playback.destroy();
        metadata.destroy();
    }
}
