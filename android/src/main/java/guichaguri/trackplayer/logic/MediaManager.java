package guichaguri.trackplayer.logic;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
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

    private final WakeLock wakeLock;
    private final WifiLock wifiLock;

    private Playback playback;
    private Bundle playbackOptions;
    private boolean serviceStarted = false;
    private boolean stopWithApp = false;

    public MediaManager(PlayerService service) {
        this.service = service;
        this.metadata = new Metadata(service, this);

        this.focus = new FocusManager(service, metadata);

        service.setSessionToken(metadata.getToken());

        PowerManager powerManager = (PowerManager)service.getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "track-playback-wake-lock");
        wakeLock.setReferenceCounted(false);

        // Android 7: Use the application context here to prevent any memory leaks
        WifiManager wifiManager = (WifiManager)service.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "track-playback-wifi-lock");
        wifiLock.setReferenceCounted(false);
    }

    public void updateOptions(Bundle data) {
        stopWithApp = data.getBoolean("stopWithApp", stopWithApp);

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

    public void setupPlayer(Bundle options) {
        if(playback != null) return;

        Log.d(Utils.TAG, "Setting up the player");

        playbackOptions = options == null ? new Bundle() : options;
        playback = createLocalPlayback();
    }

    public void destroyPlayer() {
        Log.d(Utils.TAG, "Destroying the player");

        playback.destroy();
        if(!Utils.isStopped(playback.getState())) onStop();
        playback = null;

        if(serviceStarted) {
            Log.i(Utils.TAG, "Marking the service as stopped, as we don't need it anymore");
            service.stopSelf();
            serviceStarted = false;
        }
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public boolean shouldStopWithApp() {
        return stopWithApp;
    }

    public void switchPlayback(Playback pb) {
        Log.d(Utils.TAG, "Switching playback");

        // Same playback?
        if(pb == playback) return;

        // Copy everything to the new playback
        pb.copyPlayback(playback);
        playback.destroy();

        // Set the new playback
        playback = pb;

        // Update the metadata
        metadata.updateQueue(playback);
        metadata.updateMetadata(playback, playback.getCurrentTrack());
        metadata.updatePlayback(playback);
    }

    public Playback getPlayback() {
        return playback;
    }

    public void onPlay() {
        Log.d(Utils.TAG, "onPlay: The service is now on foreground, audio focus, wake and wifi locks have been acquired");

        if (VERSION.SDK_INT >= 23) {
            MediaNotification notification = metadata.getNotification();

            // Set the service as foreground, updating and showing the notification
            service.startForeground(MediaNotification.NOTIFICATION_ID, notification.build());
            notification.setShowing(true);
        }

        // Activate the session
        metadata.setEnabled(true);

        if(!playback.isRemote()) {
            // Enable the audio focus so the device knows we are playing music
            focus.enable();

            // Acquire the wake lock so the device doesn't sleeps stopping the music
            if(!wakeLock.isHeld()) wakeLock.acquire();

            Track currentTrack = playback.getCurrentTrack();
            if(currentTrack != null && !currentTrack.urlLocal) {
                // Acquire wifi lock when the track needs network
                if(!wifiLock.isHeld()) wifiLock.acquire();
            }
        }

        if(!serviceStarted) {
            Log.d(Utils.TAG, "Marking the service as started, as there is now playback");
            if (VERSION.SDK_INT >= 26) {
                service.startForegroundService(new Intent(service, PlayerService.class));
            } else {
                service.startService(new Intent(service, PlayerService.class));
            }
            serviceStarted = true;
        }
    }

    public void onPause() {
        Log.d(Utils.TAG, "onPause: The service is now in background again, audio focus, wake and wifi locks have been released");

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
    }

    public void onStop() {
        Log.d(Utils.TAG, "onStop: The service is now in background, audio focus, wake and wifi locks have been released");

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
    }

    public void onEnd(Track previous, long prevPos) {
        Log.d(Utils.TAG, "onEnd");

        Bundle bundle = new Bundle();
        bundle.putString("track", previous != null ? previous.id : null);
        bundle.putDouble("position", Utils.toSeconds(prevPos));
        Events.dispatchEvent(service, Events.PLAYBACK_QUEUE_ENDED, bundle);
    }

    public void onStateChange(int state) {
        Log.d(Utils.TAG, "onStateChange");

        Bundle bundle = new Bundle();
        bundle.putInt("state", state);
        Events.dispatchEvent(service, Events.PLAYBACK_STATE, bundle);
    }

    public void onTrackUpdate(Track previous, long prevPos, Track next, boolean changed) {
        Log.d(Utils.TAG, "onTrackUpdate");

        metadata.updateMetadata(playback, next);

        if(changed) {
            Bundle bundle = new Bundle();
            bundle.putString("track", previous != null ? previous.id : null);
            bundle.putDouble("position", Utils.toSeconds(prevPos));
            bundle.putString("nextTrack", next != null ? next.id : null);
            Events.dispatchEvent(service, Events.PLAYBACK_TRACK_CHANGED, bundle);
        }
    }

    public void onPlaybackUpdate() {
        Log.d(Utils.TAG, "onPlaybackUpdate");

        metadata.updatePlayback(playback);
    }

    public void onQueueUpdate() {
        Log.d(Utils.TAG, "onQueueUpdate");

        metadata.updateQueue(playback);
    }

    public void onError(Throwable error) {
        Log.d(Utils.TAG, "onError: " + error.getMessage());

        Bundle bundle = new Bundle();
        bundle.putString("error", error.getMessage());
        Events.dispatchEvent(service, Events.PLAYBACK_ERROR, bundle);
    }

    public void onCommand(Intent intent) {
        if(intent == null) {
            Log.d(Utils.TAG, "The service is probably restarting. The player is being safely destroyed");

            // Fail-safely
            if(playback != null) {
                destroyPlayer();
            }

            service.stopForeground(true);
        } else {
            metadata.handleIntent(intent);
        }
    }

    public void onServiceUnbounded() {
        Bundle bundle = new Bundle();
        Events.dispatchEvent(service, Events.PLAYBACK_UNBIND, bundle);
    }
    
    public void onServiceDestroy() {
        Log.i(Utils.TAG, "Destroying resources");

        // Destroy the playback
        if(playback != null) {
            playback.destroy();
            playback = null;
        }

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

}
