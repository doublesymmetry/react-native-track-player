package com.guichaguri.trackplayer.service;

import android.content.Context;
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
import com.facebook.react.jstasks.HeadlessJsTaskConfig;
import com.guichaguri.trackplayer.service.models.Track;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.annotation.Nullable;

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
            try {
                MediaButtonReceiver.handleIntent(manager.getMetadata().getSession(), intent);
            } catch (NullPointerException e) {
                Log.d(Utils.LOG, "MusicService trying to handle intent with manager that does not exist");
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                Set<String> cachedQueueSet = prefs.getStringSet("cachedQueue", null);
                List<Track> cachedQueue = Collections.emptyList();
                for (String s : cachedQueueSet) {
                    Bundle trackBundle = jsonStringToBundle(s);
                    Track track = new Track(getApplicationContext(), trackBundle, RatingCompat.RATING_NONE); // Temp rating none;
                    cachedQueue.add(track);
                }
            }
            return START_NOT_STICKY;
        }

        manager = new MusicManager(this);
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    public static Bundle jsonStringToBundle(String jsonString){
        try {
            JSONObject jsonObject = toJsonObject(jsonString);
            return jsonToBundle(jsonObject);
        } catch (JSONException ignored) {

        }
        return null;
    }
    public static JSONObject toJsonObject(String jsonString) throws JSONException {
        return new JSONObject(jsonString);
    }
    public static Bundle jsonToBundle(JSONObject jsonObject) throws JSONException {
        Bundle bundle = new Bundle();
        Iterator iter = jsonObject.keys();
        while(iter.hasNext()){
            String key = (String)iter.next();
            String value = jsonObject.getString(key);
            bundle.putString(key,value);
        }
        return bundle;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (manager != null) {

            Set<String> set = new HashSet<>();

            List<Track> tracks = manager.getPlayback().getQueue();

            for(Track track : tracks) {
                set.add(track.json.toString());
            }

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = prefs.edit();
            editor.putStringSet("cachedQueue", set);
            editor.apply();

            manager.destroy();
            manager = null;
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        if (manager.shouldStopWithApp()) {
            stopSelf();
        }
    }
}
