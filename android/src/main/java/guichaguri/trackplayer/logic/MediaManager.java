package guichaguri.trackplayer.logic;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import com.facebook.react.bridge.Promise;
import guichaguri.trackplayer.cast.GoogleCast;
import guichaguri.trackplayer.logic.components.FocusManager;
import guichaguri.trackplayer.logic.services.PlayerService;
import guichaguri.trackplayer.logic.track.Track;
import guichaguri.trackplayer.metadata.Metadata;
import guichaguri.trackplayer.metadata.components.MediaNotification;
import guichaguri.trackplayer.player.Playback;
import guichaguri.trackplayer.player.players.AndroidPlayback;
import guichaguri.trackplayer.player.players.ExoPlayback;

/**
 * @author Guilherme Chaguri
 */
public class MediaManager {

    private final PlayerService service;
    private final FocusManager focus;
    private final Metadata metadata;
    private final GoogleCast cast;

    private final WakeLock wakeLock;
    private final WifiLock wifiLock;

    private Playback playback;
    private Bundle playbackOptions;
    private boolean serviceStarted = false;

    public MediaManager(PlayerService service) {
        this.service = service;
        this.metadata = new Metadata(service, this);

        if(LibHelper.isChromecastAvailable(service)) {
            this.cast = new GoogleCast(service.getApplicationContext(), this);
        } else {
            this.cast = null;
        }

        this.focus = new FocusManager(service, metadata);

        service.setSessionToken(metadata.getToken());

        PowerManager powerManager = (PowerManager)service.getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "track-playback-wake-lock");
        wakeLock.setReferenceCounted(false);

        WifiManager wifiManager = (WifiManager)service.getSystemService(Context.WIFI_SERVICE);
        wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "track-playback-wifi-lock");
        wifiLock.setReferenceCounted(false);
    }

    public void updateOptions(Bundle data) {
        metadata.updateOptions(data);
        metadata.updatePlayback(playback);
    }

    public Playback createLocalPlayback() {
        if(LibHelper.isExoPlayerAvailable()) {
            Log.i(Utils.TAG, "Creating an ExoPlayer instance...");
            return new ExoPlayback(service, this, playbackOptions);
        } else {
            Log.i(Utils.TAG, "Creating a MediaPlayer instance...");
            return new AndroidPlayback(service, this, playbackOptions);
        }
    }

    public void setupPlayer(Bundle options, Promise promise) {
        if(playback != null) {
            Utils.rejectCallback(promise, "setupPlayer", "The playback is already initialized");
            return;
        }

        playbackOptions = options;
        playback = createLocalPlayback();

        Utils.resolveCallback(promise);
    }

    public void destroyPlayer() {
        playback.destroy();
        if(!Utils.isStopped(playback.getState())) onStop();
        playback = null;

        if(serviceStarted) {
            Log.i(Utils.TAG, "Marking the service as stopped, as we don't need it anymore");
            service.stopSelf();
            serviceStarted = false;
        }
    }

    public int getRatingType() {
        return metadata.getRatingType();
    }

    public void switchPlayback(Playback pb) {
        // Same playback?
        if(pb == playback) return;

        // Copy everything to the new playback
        pb.copyPlayback(playback);
        playback.destroy();

        // Set the new playback
        playback = pb;

        // Update the metadata
        metadata.updateQueue(playback);
        metadata.updateMetadata(playback);
        metadata.updatePlayback(playback);
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
            // Enable the audio focus so the device knows we are playing music
            focus.enable();

            // Acquire the wake lock so the device doesn't sleeps stopping the music
            if(!wakeLock.isHeld()) wakeLock.acquire();

            if(!playback.getCurrentTrack().urlLocal) {
                // Acquire wifi lock when the track needs network
                if(!wifiLock.isHeld()) wifiLock.acquire();
            }
        }

        if(!serviceStarted) {
            Log.i(Utils.TAG, "Marking the service as started, as there is now playback");
            service.startService(new Intent(service, PlayerService.class));
            serviceStarted = true;
        }

        Events.dispatchEvent(service, Events.PLAYBACK_PLAY, null);
    }

    public void onPause() {
        // Set the service as background, keeping the notification
        service.stopForeground(false);

        if(!playback.isRemote()) {
            // Release the wake lock
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
            // Release the wake lock
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
        Bundle bundle = new Bundle();
        bundle.putString("track", track.id);
        Events.dispatchEvent(service, Events.PLAYBACK_LOAD, bundle);
    }

    public void onEnd() {
        Events.dispatchEvent(service, Events.PLAYBACK_ENDED, null);
    }

    public void onStateChange(int state) {
        Bundle bundle = new Bundle();
        bundle.putInt("state", state);
        Events.dispatchEvent(service, Events.PLAYBACK_STATE, bundle);
    }

    public void onTrackUpdate() {
        metadata.updateMetadata(playback);
    }

    public void onPlaybackUpdate() {
        metadata.updatePlayback(playback);
    }

    public void onQueueUpdate() {
        metadata.updateQueue(playback);
    }

    public void onError(Throwable error) {
        Bundle bundle = new Bundle();
        bundle.putString("error", error.getMessage());
        Events.dispatchEvent(service, Events.PLAYBACK_ERROR, bundle);
    }

    public void onCommand(Intent intent) {
        metadata.handleIntent(intent);
    }

    public void onServiceDestroy() {
        Log.i(Utils.TAG, "Destroying resources");

        // Destroy the playback
        if(playback != null) playback.destroy();

        // Remove the audio focus
        focus.disable();

        // Destroy the metadata resources
        metadata.destroy();

        // Destroy the cast resources
        if(cast != null) cast.destroy();

        // Release the wifi lock
        if(wifiLock.isHeld()) {
            wifiLock.release();
        }

        // Release the wake lock
        if(wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

}
