package com.guichaguri.trackplayer.service;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.annotation.RequiresApi;
import android.util.Log;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.guichaguri.trackplayer.module.MusicEvents;
import com.guichaguri.trackplayer.service.metadata.MetadataManager;
import com.guichaguri.trackplayer.service.models.Track;
import com.guichaguri.trackplayer.service.player.ExoPlayback;
import com.guichaguri.trackplayer.service.player.LocalPlayback;

import static com.google.android.exoplayer2.DefaultLoadControl.*;

/**
 * @author Guichaguri
 */
public class MusicManager implements OnAudioFocusChangeListener {

    private final MusicService service;

    private final WakeLock wakeLock;
    private final WifiLock wifiLock;

    private MetadataManager metadata;
    private ExoPlayback playback;

    @RequiresApi(26)
    private AudioFocusRequest focus = null;
    private boolean hasAudioFocus = false;

    private BroadcastReceiver noisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            service.emit(MusicEvents.BUTTON_PAUSE, null);
        }
    };
    private boolean receivingNoisyEvents = false;

    private boolean stopWithApp = false;

    @SuppressLint("InvalidWakeLockTag")
    public MusicManager(MusicService service) {
        this.service = service;
        this.metadata = new MetadataManager(service, this);

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

    public boolean shouldStopWithApp() {
        return stopWithApp;
    }

    public void setStopWithApp(boolean stopWithApp) {
        this.stopWithApp = stopWithApp;
    }

    public MetadataManager getMetadata() {
        return metadata;
    }

    public void switchPlayback(ExoPlayback playback) {
        if(this.playback != null) {
            this.playback.stop();
            this.playback.destroy();
        }

        this.playback = playback;

        if(this.playback != null) {
            this.playback.initialize();
        }
    }

    public LocalPlayback createLocalPlayback(Bundle options) {
        int minBuffer = (int)Utils.toMillis(options.getDouble("minBuffer", Utils.toSeconds(DEFAULT_MIN_BUFFER_MS)));
        int maxBuffer = (int)Utils.toMillis(options.getDouble("maxBuffer", Utils.toSeconds(DEFAULT_MAX_BUFFER_MS)));
        int playBuffer = (int)Utils.toMillis(options.getDouble("playBuffer", Utils.toSeconds(DEFAULT_BUFFER_FOR_PLAYBACK_MS)));
        int backBuffer = (int)Utils.toMillis(options.getDouble("backBuffer", Utils.toSeconds(DEFAULT_BACK_BUFFER_DURATION_MS)));
        long cacheMaxSize = (long)(options.getDouble("maxCacheSize", 0) * 1024);
        int multiplier = DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS / DEFAULT_BUFFER_FOR_PLAYBACK_MS;

        LoadControl control = new DefaultLoadControl.Builder()
                .setBufferDurationsMs(minBuffer, maxBuffer, playBuffer, playBuffer * multiplier)
                .setBackBuffer(backBuffer, false)
                .createDefaultLoadControl();

        SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(service, new DefaultRenderersFactory(service), new DefaultTrackSelector(), control);

        player.setAudioAttributes(new com.google.android.exoplayer2.audio.AudioAttributes.Builder()
                .setContentType(C.CONTENT_TYPE_MUSIC).setUsage(C.USAGE_MEDIA).build());

        return new LocalPlayback(service, this, player, cacheMaxSize);
    }

    @SuppressLint("WakelockTimeout")
    public void onPlay() {
        Log.d(Utils.LOG, "onPlay");
        if(playback == null) return;

        Track track = playback.getCurrentTrack();
        if(track == null) return;

        if(!playback.isRemote()) {
            requestFocus();

            if(!receivingNoisyEvents) {
                service.registerReceiver(noisyReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
                receivingNoisyEvents = true;
            }

            if(!wakeLock.isHeld()) wakeLock.acquire();

            if(!Utils.isLocal(track.uri)) {
                if(!wifiLock.isHeld()) wifiLock.acquire();
            }
        }

        metadata.setActive(true);
    }

    public void onPause() {
        Log.d(Utils.LOG, "onPause");

        // Unregisters the noisy receiver
        if(receivingNoisyEvents) {
            service.unregisterReceiver(noisyReceiver);
            receivingNoisyEvents = false;
        }

        // Release the wake and the wifi locks
        if(wakeLock.isHeld()) wakeLock.release();
        if(wifiLock.isHeld()) wifiLock.release();

        metadata.setActive(true);
    }

    public void onStop() {
        Log.d(Utils.LOG, "onStop");

        // Release the wake and the wifi locks
        if(wakeLock.isHeld()) wakeLock.release();
        if(wifiLock.isHeld()) wifiLock.release();

        abandonFocus();

        metadata.setActive(false);
    }

    public void onStateChange(int state) {
        Log.d(Utils.LOG, "onStateChange");

        Bundle bundle = new Bundle();
        bundle.putInt("state", state);
        service.emit(MusicEvents.PLAYBACK_STATE, bundle);
        metadata.updatePlayback(playback);
    }

    public void onTrackUpdate(Track previous, long prevPos, Track next) {
        Log.d(Utils.LOG, "onTrackUpdate");

        if(next != null) metadata.updateMetadata(next);

        Bundle bundle = new Bundle();
        bundle.putString("track", previous != null ? previous.id : null);
        bundle.putDouble("position", Utils.toSeconds(prevPos));
        bundle.putString("nextTrack", next != null ? next.id : null);
        service.emit(MusicEvents.PLAYBACK_TRACK_CHANGED, bundle);
    }

    public void onReset() {
        metadata.removeNotifications();
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

    @Override
    public void onAudioFocusChange(int focus) {
        Log.d(Utils.LOG, "onDuck");

        boolean permanent = false;
        boolean paused = false;
        boolean ducking = false;

        switch(focus) {
            case AudioManager.AUDIOFOCUS_LOSS:
                permanent = true;
                abandonFocus();
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                paused = true;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                ducking = true;
                break;
            default:
                break;
        }

        Bundle bundle = new Bundle();
        bundle.putBoolean("permanent", permanent);
        bundle.putBoolean("paused", paused);
        bundle.putBoolean("ducking", ducking);
        service.emit(MusicEvents.BUTTON_DUCK, bundle);
    }

    private void requestFocus() {
        if(hasAudioFocus) return;
        Log.d(Utils.LOG, "Requesting audio focus...");

        AudioManager manager = (AudioManager)service.getSystemService(Context.AUDIO_SERVICE);
        int r;

        if(manager == null) {
            r = AudioManager.AUDIOFOCUS_REQUEST_FAILED;
        } else if(Build.VERSION.SDK_INT >= 26) {
            focus = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setOnAudioFocusChangeListener(this)
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build())
                    .build();

            r = manager.requestAudioFocus(focus);
        } else {
            //noinspection deprecation
            r = manager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }

        hasAudioFocus = r == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private void abandonFocus() {
        if(!hasAudioFocus) return;
        Log.d(Utils.LOG, "Abandoning audio focus...");

        AudioManager manager = (AudioManager)service.getSystemService(Context.AUDIO_SERVICE);
        int r;

        if(manager == null) {
            r = AudioManager.AUDIOFOCUS_REQUEST_FAILED;
        } else if(Build.VERSION.SDK_INT >= 26) {
            r = manager.abandonAudioFocusRequest(focus);
        } else {
            //noinspection deprecation
            r = manager.abandonAudioFocus(this);
        }

        hasAudioFocus = r != AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    public void destroy() {
        Log.d(Utils.LOG, "Releasing service resources...");

        // Disable audio focus
        abandonFocus();

        // Stop receiving audio becoming noisy events
        if(receivingNoisyEvents) {
            service.unregisterReceiver(noisyReceiver);
            receivingNoisyEvents = false;
        }

        // Release the playback resources
        if(playback != null) playback.destroy();

        // Release the metadata resources
        metadata.destroy();

        // Release the locks
        if(wifiLock.isHeld()) wifiLock.release();
        if(wakeLock.isHeld()) wakeLock.release();
    }
}
