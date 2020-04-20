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
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import androidx.annotation.RequiresApi;
import android.util.Log;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.guichaguri.trackplayer.module.MusicEvents;
import com.guichaguri.trackplayer.module.MusicModule;
import com.guichaguri.trackplayer.service.metadata.MetadataManager;
import com.guichaguri.trackplayer.service.models.Track;
import com.guichaguri.trackplayer.service.player.ExoPlayback;
import com.guichaguri.trackplayer.service.player.LocalPlayback;

import static com.google.android.exoplayer2.DefaultLoadControl.*;

/**
 * @author Guichaguri
 */
public class MusicManager implements OnAudioFocusChangeListener {

    private final ReactApplicationContext context;

    private final WakeLock wakeLock;
    private final WifiLock wifiLock;

    private final Handler handler = new Handler();

    private MetadataManager metadata;
    private ExoPlayback playback;

    @RequiresApi(26)
    private AudioFocusRequest focus = null;
    private boolean hasAudioFocus = false;
    private boolean wasDucking = false;

    private BroadcastReceiver noisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            emitEvent(MusicEvents.BUTTON_PAUSE, null);
        }
    };
    private boolean receivingNoisyEvents = false;

    private boolean stopWithApp = false;
    private boolean alwaysPauseOnInterruption = false;

    @SuppressLint("InvalidWakeLockTag")
    public MusicManager(ReactApplicationContext context) {
        this.context = context;
        this.metadata = new MetadataManager(context, this);

        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "track-player-wake-lock");
        wakeLock.setReferenceCounted(false);

        // Android 7: Use the application context here to prevent any memory leaks
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
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

    public void setAlwaysPauseOnInterruption(boolean alwaysPauseOnInterruption) {
        this.alwaysPauseOnInterruption = alwaysPauseOnInterruption;
    }

    public MetadataManager getMetadata() {
        return metadata;
    }

    public Handler getHandler() {
        return handler;
    }

    public void emitEvent(String event, WritableMap data) {
        context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(event, data);
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

    public LocalPlayback createLocalPlayback(ReadableMap options) {
        boolean autoUpdateMetadata = Utils.getBoolean(options, "autoUpdateMetadata", true);
        int minBuffer = (int)Utils.toMillis(Utils.getDouble(options, "minBuffer", Utils.toSeconds(DEFAULT_MIN_BUFFER_MS)));
        int maxBuffer = (int)Utils.toMillis(Utils.getDouble(options, "maxBuffer", Utils.toSeconds(DEFAULT_MAX_BUFFER_MS)));
        int playBuffer = (int)Utils.toMillis(Utils.getDouble(options, "playBuffer", Utils.toSeconds(DEFAULT_BUFFER_FOR_PLAYBACK_MS)));
        int backBuffer = (int)Utils.toMillis(Utils.getDouble(options, "backBuffer", Utils.toSeconds(DEFAULT_BACK_BUFFER_DURATION_MS)));
        long cacheMaxSize = (long)(Utils.getDouble(options, "maxCacheSize", 0) * 1024);
        int multiplier = DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS / DEFAULT_BUFFER_FOR_PLAYBACK_MS;

        LoadControl control = new DefaultLoadControl.Builder()
                .setBufferDurationsMs(minBuffer, maxBuffer, playBuffer, playBuffer * multiplier)
                .setBackBuffer(backBuffer, false)
                .createDefaultLoadControl();

        SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(context, new DefaultRenderersFactory(context), new DefaultTrackSelector(), control);

        player.setAudioAttributes(new com.google.android.exoplayer2.audio.AudioAttributes.Builder()
                .setContentType(C.CONTENT_TYPE_MUSIC).setUsage(C.USAGE_MEDIA).build());

        return new LocalPlayback(context, this, player, cacheMaxSize, autoUpdateMetadata);
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
                receivingNoisyEvents = true;
                context.registerReceiver(noisyReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
            }

            if(!wakeLock.isHeld()) wakeLock.acquire();

            if(!Utils.isLocal(track.uri)) {
                if(!wifiLock.isHeld()) wifiLock.acquire();
            }
        }

        if (playback.shouldAutoUpdateMetadata())
            metadata.setActive(true);
    }

    public void onPause() {
        Log.d(Utils.LOG, "onPause");

        // Unregisters the noisy receiver
        if(receivingNoisyEvents) {
            context.unregisterReceiver(noisyReceiver);
            receivingNoisyEvents = false;
        }

        // Release the wake and the wifi locks
        if(wakeLock.isHeld()) wakeLock.release();
        if(wifiLock.isHeld()) wifiLock.release();

        if (playback.shouldAutoUpdateMetadata())
            metadata.setActive(true);
    }

    public void onStop() {
        Log.d(Utils.LOG, "onStop");

        // Unregisters the noisy receiver
        if(receivingNoisyEvents) {
            context.unregisterReceiver(noisyReceiver);
            receivingNoisyEvents = false;
        }

        // Release the wake and the wifi locks
        if(wakeLock.isHeld()) wakeLock.release();
        if(wifiLock.isHeld()) wifiLock.release();

        abandonFocus();

        if (playback.shouldAutoUpdateMetadata())
            metadata.setActive(false);
    }

    public void onStateChange(int state) {
        Log.d(Utils.LOG, "onStateChange");

        WritableMap map = Arguments.createMap();
        map.putInt("state", state);
        emitEvent(MusicEvents.PLAYBACK_STATE, map);

        if (playback.shouldAutoUpdateMetadata())
            metadata.updatePlayback(playback);
    }

    public void onTrackUpdate(Track previous, long prevPos, Track next) {
        Log.d(Utils.LOG, "onTrackUpdate");

        if(playback.shouldAutoUpdateMetadata() && next != null)
            metadata.updateMetadata(next);

        WritableMap map = Arguments.createMap();
        map.putString("track", previous != null ? previous.id : null);
        map.putDouble("position", Utils.toSeconds(prevPos));
        map.putString("nextTrack", next != null ? next.id : null);
        emitEvent(MusicEvents.PLAYBACK_TRACK_CHANGED, map);
    }

    public void onReset() {
        metadata.removeNotifications();
    }

    public void onEnd(Track previous, long prevPos) {
        Log.d(Utils.LOG, "onEnd");

        WritableMap map = Arguments.createMap();
        map.putString("track", previous != null ? previous.id : null);
        map.putDouble("position", Utils.toSeconds(prevPos));
        emitEvent(MusicEvents.PLAYBACK_QUEUE_ENDED, map);
    }

    public void onMetadataReceived(String source, String title, String url, String artist, String album, String date, String genre) {
        Log.d(Utils.LOG, "onMetadataReceived: " + source);

        WritableMap map = Arguments.createMap();
        map.putString("source", source);
        map.putString("title", title);
        map.putString("url", url);
        map.putString("artist", artist);
        map.putString("album", album);
        map.putString("date", date);
        map.putString("genre", genre);
        emitEvent(MusicEvents.PLAYBACK_METADATA, map);
    }

    public void onError(String code, String error) {
        Log.d(Utils.LOG, "onError");
        Log.e(Utils.LOG, "Playback error: " + code + " - " + error);

        WritableMap map = Arguments.createMap();
        map.putString("code", code);
        map.putString("message", error);
        emitEvent(MusicEvents.PLAYBACK_ERROR, map);
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
                if (alwaysPauseOnInterruption)
                    paused = true;
                else
                    ducking = true;
                break;
            default:
                break;
        }

        if (ducking) {
            playback.setVolumeMultiplier(0.5F);
            wasDucking = true;
        } else if (wasDucking) {
            playback.setVolumeMultiplier(1.0F);
            wasDucking = false;
        }

        WritableMap map = Arguments.createMap();
        map.putBoolean("permanent", permanent);
        map.putBoolean("paused", paused);
        emitEvent(MusicEvents.BUTTON_DUCK, map);
    }

    private void requestFocus() {
        if(hasAudioFocus) return;
        Log.d(Utils.LOG, "Requesting audio focus...");

        AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
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
                    .setWillPauseWhenDucked(alwaysPauseOnInterruption)
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

        AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
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
            context.unregisterReceiver(noisyReceiver);
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
