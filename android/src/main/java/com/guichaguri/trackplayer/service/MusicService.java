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

import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;
import com.guichaguri.trackplayer.service.models.Track;
import com.guichaguri.trackplayer.service.player.ExoPlayback;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import static com.guichaguri.trackplayer.service.Utils.jsonStringToBundle;
import static com.guichaguri.trackplayer.service.Utils.bundleToJson;

/**
 * @author Guichaguri
 */
public class MusicService extends HeadlessJsTaskService {

    private MusicManager manager;

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

            if (manager != null && manager.getMetadata().getSession() != null) {
                Log.d(Utils.LOG, "Manager not null and session returned");
                MediaButtonReceiver.handleIntent(manager.getMetadata().getSession(), intent);
                return START_NOT_STICKY;
            } else if (manager != null) {
                Log.d(Utils.LOG, "MusicService trying to recover lost player");
            } else {
                Log.d(Utils.LOG, "Manager null");
                manager = new MusicManager(this, getApplicationContext());
                recoverLostPlayer();
            }

        }

        if (manager == null) {
            manager = new MusicManager(this, getApplicationContext());
        }

        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    private void recoverLostPlayer() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        Integer resumeAt = 0;

        // Get current track
        String cachedCurrentTrack = prefs.getString("cachedCurrentTrack", null);
        Track currentTrack = new Track(getApplicationContext(), jsonStringToBundle(cachedCurrentTrack), RatingCompat.RATING_NONE); // Temp rating none, because we use none;

        // Get current track position
        Long currentPosition = prefs.getLong("cachedPosition", 0);

        // Get cached queue
        Set<String> cachedQueueSet = prefs.getStringSet("cachedQueue", null);
        List<Track> cachedQueue = new ArrayList<>();
        Integer index = 0;
        for (String s : cachedQueueSet) {
            Bundle trackBundle = jsonStringToBundle(s);
            Track track = new Track(getApplicationContext(), trackBundle, RatingCompat.RATING_NONE); // Temp rating none, because we use none;
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
        playback.seekTo(currentPosition);
        playback.play();

        // Get back player options
        String cachedOptionsJsonString = prefs.getString("cachedOptions", null);
        Bundle optionsBundle = jsonStringToBundle(cachedOptionsJsonString);
        manager.getMetadata().updateOptions(optionsBundle);
    }

    private void cachePlayer(MusicManager manager) {
        if (manager != null) {
            ExoPlayback playback = manager.getPlayback();

            // Make editor
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = prefs.edit();

            // Cache current track
            Track currentTrack = playback.getCurrentTrack();
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
        cachePlayer(manager);
        manager.destroy();
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
