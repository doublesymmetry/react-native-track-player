package com.guichaguri.trackplayer.service;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.util.Log;
import android.view.KeyEvent;

import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;
import com.guichaguri.trackplayer.module.MusicEvents;
import com.guichaguri.trackplayer.service.metadata.ButtonEvents;
import com.guichaguri.trackplayer.service.models.Track;
import com.guichaguri.trackplayer.service.player.ExoPlayback;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import static android.view.KeyEvent.KEYCODE_HEADSETHOOK;
import static android.view.KeyEvent.KEYCODE_MEDIA_FAST_FORWARD;
import static android.view.KeyEvent.KEYCODE_MEDIA_NEXT;
import static android.view.KeyEvent.KEYCODE_MEDIA_PLAY;
import static android.view.KeyEvent.KEYCODE_MEDIA_PREVIOUS;
import static android.view.KeyEvent.KEYCODE_MEDIA_REWIND;
import static android.view.KeyEvent.KEYCODE_MEDIA_STOP;
import static com.guichaguri.trackplayer.service.Utils.jsonStringToBundle;
import static com.guichaguri.trackplayer.service.Utils.bundleToJson;

/**
 * @author Guichaguri
 */
public class MusicService extends HeadlessJsTaskService {

    private MusicManager manager;
    private Boolean intentToStop = false;

    @Nullable
    @Override
    protected HeadlessJsTaskConfig getTaskConfig(Intent intent) {
        return new HeadlessJsTaskConfig("TrackPlayer", Arguments.createMap(), 0, true);
    }

    @Override
    public void onHeadlessJsTaskFinish(int taskId) {
        // Overridden to prevent the service from being terminated
    }

    public void emit(String event, Bundle data) {
        Intent intent = new Intent(Utils.EVENT_INTENT);

        intent.putExtra("event", event);
        if(data != null) intent.putExtra("data", data);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if(Utils.CONNECT_INTENT.equals(intent.getAction())) {
            return new MusicBinder(this, manager);
        }

        return super.onBind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null && Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {

            // Interpret event
            KeyEvent intentExtra = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (intentExtra.getKeyCode() == KEYCODE_MEDIA_STOP) {
                intentToStop = true;
            } else {
                intentToStop = false;
            }

            if (manager != null && manager.getMetadata().getSession() != null) {
                MediaButtonReceiver.handleIntent(manager.getMetadata().getSession(), intent);
                return START_NOT_STICKY;
            } else if (manager == null) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                String cachedCurrentTrack = prefs.getString("cachedCurrentTrack", null);
                if (cachedCurrentTrack != null) {
                    manager = new MusicManager(this);
                    recoverLostPlayer(intent);
                    return START_NOT_STICKY;
                }
            }
        }

        if (manager == null) {
            manager = new MusicManager(this);
        }

        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    private void recoverLostPlayer(Intent intent) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // Get intent extra and return if stop (swipe away)
        KeyEvent intentExtra = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        if (intentExtra.getKeyCode() == KEYCODE_MEDIA_STOP) {
            clearCache();
            return;
        }

        // Get current track and return if it is not available
        String cachedCurrentTrack = prefs.getString("cachedCurrentTrack", null);
        if (cachedCurrentTrack == null) return;

        // Get back player options
        String cachedOptionsJsonString = prefs.getString("cachedOptions", null);
        Bundle optionsBundle = jsonStringToBundle(cachedOptionsJsonString);

        // Get current track position
        Integer ratingType = optionsBundle.getInt("ratingType", RatingCompat.RATING_NONE);
        Track currentTrack = new Track(getApplicationContext(), jsonStringToBundle(cachedCurrentTrack), ratingType);
        Long currentPosition = prefs.getLong("cachedPosition", 0);
        Integer resumeAt = 0;

        // Get cached queue
        Set<String> cachedQueueSet = prefs.getStringSet("cachedQueue", null);
        List<Track> cachedQueue = new ArrayList<>();
        Integer index = 0;
        for (String s : cachedQueueSet) {
            Bundle trackBundle = jsonStringToBundle(s);
            Track track = new Track(getApplicationContext(), trackBundle, ratingType);
            if (track.id.equals(currentTrack.id)) {
                resumeAt = index;
            }
            cachedQueue.add(track);
            index ++;
        }

        // Reestablish manager
        ExoPlayback playback = manager.getPlayback();
        if(playback == null) {
            playback = manager.createLocalPlayback(new Bundle());
            manager.switchPlayback(playback);
        }

        playback.add(cachedQueue, resumeAt, null);

        // Set player options
        manager.getMetadata().updateOptions(optionsBundle);

        // Act on event
        ButtonEvents buttonEvents = new ButtonEvents(this, manager);
        if (intentExtra.getKeyCode() == KEYCODE_MEDIA_PLAY || intentExtra.getKeyCode() == KEYCODE_HEADSETHOOK) {
            playback.seekTo(currentPosition);
            manager.getMetadata().updateMetadata(currentTrack);
            buttonEvents.onPlay();
        } else if (intentExtra.getKeyCode() == KEYCODE_MEDIA_NEXT) {
            this.emit(MusicEvents.BUTTON_SKIP_NEXT, null);
            buttonEvents.onSkipToNext();
        } else if (intentExtra.getKeyCode() == KEYCODE_MEDIA_FAST_FORWARD) {
            buttonEvents.onFastForward();
        } else if (intentExtra.getKeyCode() == KEYCODE_MEDIA_REWIND) {
            buttonEvents.onRewind();
        } else if (intentExtra.getKeyCode() == KEYCODE_MEDIA_PREVIOUS) {
            buttonEvents.onSkipToPrevious();
        } else {
            Log.d(Utils.LOG, "keyCode " + intentExtra.getKeyCode() + " is not handled");
        }

        // Clear cache
        clearCache();
    }

    private void clearCache() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("cachedCurrentTrack");
        editor.remove("cachedPosition");
        editor.remove("cachedQueue");
        editor.remove("cachedOptions");
        editor.apply();
    }

    private void cachePlayer(MusicManager manager) {
        if (manager != null) {
            ExoPlayback playback = manager.getPlayback();

            // Make editor
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = prefs.edit();

            // Cache current track
            Track currentTrack = playback.getCurrentTrack();
            if (currentTrack == null) return;
            editor.putString("cachedCurrentTrack", currentTrack.json.toString());

            // Cache current track position
            Long currentPosition = playback.getPosition();
            editor.putLong("cachedPosition", currentPosition);

            // Cache queue
            Set<String> set = new HashSet<>();
            List<Track> tracks = playback.getQueue();
            for(Track track : tracks) {
                set.add(track.json.toString());
            }
            editor.putStringSet("cachedQueue", set);

            // Cache options
            Bundle options = manager.getMetadata().getOptionsBundle();
            editor.putString("cachedOptions", bundleToJson(options).toString());

            editor.apply();
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!intentToStop) {
            cachePlayer(manager);
        }
        manager.destroy(intentToStop);
        manager = null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        if (manager.shouldStopWithApp()) {
            stopSelf();
        }
    }
}
