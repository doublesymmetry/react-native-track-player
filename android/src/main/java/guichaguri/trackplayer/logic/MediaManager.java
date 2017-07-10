package guichaguri.trackplayer.logic;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import guichaguri.trackplayer.logic.components.FocusManager;
import guichaguri.trackplayer.logic.track.Track;
import guichaguri.trackplayer.logic.workers.PlayerService;
import guichaguri.trackplayer.metadata.Metadata;
import guichaguri.trackplayer.metadata.components.MediaNotification;
import guichaguri.trackplayer.player.Playback;
import guichaguri.trackplayer.player.players.AndroidPlayback;
import guichaguri.trackplayer.player.players.ExoPlayback;
import guichaguri.trackplayer.remote.Remote;

/**
 * @author Guilherme Chaguri
 */
public class MediaManager {

    private final PlayerService service;
    private final FocusManager focus;
    private final Metadata metadata;
    private final Remote remote;

    private final WakeLock wakeLock;
    private final WifiLock wifiLock;

    private Playback playback;
    private boolean serviceStarted = false;

    public MediaManager(PlayerService service) {
        this.service = service;
        this.metadata = new Metadata(service, this);
        this.remote = new Remote(service.getApplicationContext(), this);
        this.focus = new FocusManager(service, metadata);

        PowerManager powerManager = (PowerManager)service.getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "track-playback-wake-lock");
        wakeLock.setReferenceCounted(false);

        WifiManager wifiManager = (WifiManager)service.getSystemService(Context.WIFI_SERVICE);
        wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "track-playback-wifi-lock");
        wifiLock.setReferenceCounted(false);
    }

    public void updateOptions(ReadableMap data) {
        remote.updateOptions(data);
        metadata.updateOptions(data);
        metadata.updatePlayback(playback);
    }

    public void setupPlayer(ReadableMap options) {
        if(LibHelper.isExoPlayerAvailable()) {
            Utils.log("Creating an ExoPlayer instance...");
            playback = new ExoPlayback(service, this, options);
        } else {
            Utils.log("Creating a MediaPlayer instance...");
            playback = new AndroidPlayback(service, this, options);
        }
    }

    public void destroyPlayer() {
        playback.destroy();
        if(!Utils.isStopped(playback.getState())) onStop();

        if(serviceStarted && !remote.isScanning()) {
            Utils.log("Marking the service as stopped, as there's nothing playing");
            service.stopSelf();
            serviceStarted = false;
        }
    }

    public Remote getRemote() {
        return remote;
    }

    public int getRatingType() {
        return metadata.getRatingType();
    }

    public void switchPlayback(Playback pb) {
        // Same playback?
        if(pb == playback) return;

        // Set the new playback
        playback = pb;

        // Update the metadata
        metadata.updatePlayback(playback);
        metadata.updateMetadata(playback);
    }

    public Playback getPlayback() {
        return playback;
    }

    public void onPlay() {
        MediaNotification notification = metadata.getNotification();

        // Set the service as foreground, updating and showing the notification
        service.startForeground(MediaNotification.NOTIFICATION_ID, notification.build());
        notification.setShowing(true);

        // Activate the session
        metadata.setEnabled(true);

        if(!playback.isRemote()) {
            focus.enable();
            if(!wakeLock.isHeld()) wakeLock.acquire();

            if(!playback.getCurrentTrack().urlLocal) {
                // Acquire wifi lock when the track needs network
                if(!wifiLock.isHeld()) wifiLock.acquire();
            }
        }

        if(!serviceStarted) {
            Utils.log("Marking the service as started, as there is now playback");
            service.startService(new Intent(service, PlayerService.class));
            serviceStarted = true;
        }

        Events.dispatchEvent(service, Events.PLAYBACK_PLAY, null);
    }

    public void onPause() {
        // Set the service as background, keeping the notification
        service.stopForeground(false);

        if(!playback.isRemote()) {
            if(wakeLock.isHeld()) wakeLock.release();

            if(wifiLock.isHeld()) {
                // Release the wifi lock if the track was using network
                wifiLock.release();
            }

            // We'll disable the audio focus as we don't need it anymore
            focus.disable();
        }

        Events.dispatchEvent(service, Events.PLAYBACK_PAUSE, null);
    }

    public void onStop() {
        // Set the service as background, removing the notification
        metadata.getNotification().setShowing(false);
        service.stopForeground(true);

        // Deactivate the session
        metadata.setEnabled(false);

        if(!playback.isRemote()) {
            if(wakeLock.isHeld()) wakeLock.release();

            if(wifiLock.isHeld()) {
                // Release the wifi lock if the track was using network
                wifiLock.release();
            }

            // We'll disable the audio focus as we don't need it anymore
            focus.disable();
        }

        Events.dispatchEvent(service, Events.PLAYBACK_STOP, null);
    }

    public void onLoad(Track track) {
        WritableMap data = Arguments.createMap();
        data.putString("track", track.id);
        Events.dispatchEvent(service, Events.PLAYBACK_LOAD, data);
    }

    public void onEnd() {
        Events.dispatchEvent(service, Events.PLAYBACK_ENDED, null);
    }

    public void onStateChange(int state) {
        WritableMap data = Arguments.createMap();
        data.putInt("state", state);
        Events.dispatchEvent(service, Events.PLAYBACK_STATE, data);
    }

    public void onTrackUpdate() {
        metadata.updateMetadata(playback);
    }

    public void onPlaybackUpdate() {
        metadata.updatePlayback(playback);
    }

    public void onError(Throwable error) {
        WritableMap data = Arguments.createMap();
        data.putString("error", error.getMessage());
        Events.dispatchEvent(service, Events.PLAYBACK_ERROR, data);
    }

    public void onCommand(Intent intent) {
        metadata.handleIntent(intent);
    }

    public void onServiceDestroy() {
        Utils.log("Destroying resources");

        // Destroy the playback
        playback.destroy();

        // Remove the audio focus
        focus.disable();

        // Destroy the metadata resources
        metadata.destroy();

        // Release the wifi lock
        if(wifiLock.isHeld()) {
            wifiLock.release();
        }

        // Release the wake lock
        if(wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    public void onScanningStart() {
        if(!serviceStarted) {
            Utils.log("Marking the service as started, as we are now searching for remote devices");
            service.startService(new Intent(service, PlayerService.class));
            serviceStarted = true;
        }
    }

    public void onScanningStop() {
        if(serviceStarted && playback == null && !remote.isScanning()) {
            Utils.log("Marking the service as stopped, as we are not searching for remote devices anymore");
            service.stopSelf();
            serviceStarted = false;
        }
    }

}
