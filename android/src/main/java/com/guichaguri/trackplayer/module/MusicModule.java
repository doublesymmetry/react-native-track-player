package com.guichaguri.trackplayer.module;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.facebook.react.bridge.*;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Player;
import com.guichaguri.trackplayer.service.MusicBinder;
import com.guichaguri.trackplayer.service.MusicService;
import com.guichaguri.trackplayer.service.Utils;
import com.guichaguri.trackplayer.service.models.NowPlayingMetadata;
import com.guichaguri.trackplayer.service.models.Track;
import com.guichaguri.trackplayer.service.player.ExoPlayback;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * @author Guichaguri
 */
public class MusicModule extends ReactContextBaseJavaModule implements ServiceConnection {

    private MusicBinder binder;
    private MusicEvents eventHandler;
    private ArrayDeque<Runnable> initCallbacks = new ArrayDeque<>();
    private boolean connecting = false;
    private Bundle options;

    public MusicModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    @Nonnull
    public String getName() {
        return "TrackPlayerModule";
    }

    @Override
    public void initialize() {
        ReactContext context = getReactApplicationContext();
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);

        eventHandler = new MusicEvents(context);
        manager.registerReceiver(eventHandler, new IntentFilter(Utils.EVENT_INTENT));
    }

    @Override
    public void onCatalystInstanceDestroy() {
        ReactContext context = getReactApplicationContext();

        if(eventHandler != null) {
            LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);

            manager.unregisterReceiver(eventHandler);
            eventHandler = null;
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        binder = (MusicBinder)service;
        connecting = false;

        // Reapply options that user set before with updateOptions
        if (options != null) {
            binder.updateOptions(options);
        }

        // Triggers all callbacks
        while(!initCallbacks.isEmpty()) {
            binder.post(initCallbacks.remove());
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        binder = null;
        connecting = false;
    }

    /**
     * Waits for a connection to the service and/or runs the {@link Runnable} in the player thread
     */
    private void waitForConnection(Runnable r) {
        if(binder != null) {
            binder.post(r);
            return;
        } else {
            initCallbacks.add(r);
        }

        if(connecting) return;

        ReactApplicationContext context = getReactApplicationContext();

        // Binds the service to get a MediaWrapper instance
        Intent intent = new Intent(context, MusicService.class);
        context.startService(intent);
        intent.setAction(Utils.CONNECT_INTENT);
        context.bindService(intent, this, 0);

        connecting = true;
    }

    /* ****************************** API ****************************** */

    @Nullable
    @Override
    public Map<String, Object> getConstants() {
        Map<String, Object> constants = new HashMap<>();

        // Capabilities
        constants.put("CAPABILITY_PLAY", PlaybackStateCompat.ACTION_PLAY);
        constants.put("CAPABILITY_PLAY_FROM_ID", PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID);
        constants.put("CAPABILITY_PLAY_FROM_SEARCH", PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH);
        constants.put("CAPABILITY_PAUSE", PlaybackStateCompat.ACTION_PAUSE);
        constants.put("CAPABILITY_STOP", PlaybackStateCompat.ACTION_STOP);
        constants.put("CAPABILITY_SEEK_TO", PlaybackStateCompat.ACTION_SEEK_TO);
        constants.put("CAPABILITY_SKIP", PlaybackStateCompat.ACTION_SKIP_TO_QUEUE_ITEM);
        constants.put("CAPABILITY_SKIP_TO_NEXT", PlaybackStateCompat.ACTION_SKIP_TO_NEXT);
        constants.put("CAPABILITY_SKIP_TO_PREVIOUS", PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);
        constants.put("CAPABILITY_SET_RATING", PlaybackStateCompat.ACTION_SET_RATING);
        constants.put("CAPABILITY_JUMP_FORWARD", PlaybackStateCompat.ACTION_FAST_FORWARD);
        constants.put("CAPABILITY_JUMP_BACKWARD", PlaybackStateCompat.ACTION_REWIND);

        // States
        constants.put("STATE_NONE", PlaybackStateCompat.STATE_NONE);
        constants.put("STATE_READY", PlaybackStateCompat.STATE_PAUSED);
        constants.put("STATE_PLAYING", PlaybackStateCompat.STATE_PLAYING);
        constants.put("STATE_PAUSED", PlaybackStateCompat.STATE_PAUSED);
        constants.put("STATE_STOPPED", PlaybackStateCompat.STATE_STOPPED);
        constants.put("STATE_BUFFERING", PlaybackStateCompat.STATE_BUFFERING);
        constants.put("STATE_CONNECTING", PlaybackStateCompat.STATE_CONNECTING);

        // Rating Types
        constants.put("RATING_HEART", RatingCompat.RATING_HEART);
        constants.put("RATING_THUMBS_UP_DOWN", RatingCompat.RATING_THUMB_UP_DOWN);
        constants.put("RATING_3_STARS", RatingCompat.RATING_3_STARS);
        constants.put("RATING_4_STARS", RatingCompat.RATING_4_STARS);
        constants.put("RATING_5_STARS", RatingCompat.RATING_5_STARS);
        constants.put("RATING_PERCENTAGE", RatingCompat.RATING_PERCENTAGE);

        // Repeat Modes
        constants.put("REPEAT_OFF", Player.REPEAT_MODE_OFF);
        constants.put("REPEAT_TRACK", Player.REPEAT_MODE_ONE);
        constants.put("REPEAT_QUEUE", Player.REPEAT_MODE_ALL);

        return constants;
    }

    @ReactMethod
    public void setupPlayer(ReadableMap data, final Promise promise) {
        final Bundle options = Arguments.toBundle(data);

        waitForConnection(() -> binder.setupPlayer(options, promise));
    }

    @ReactMethod
    public void isServiceRunning(final Promise promise) {
        promise.resolve(binder != null);
    }

    @ReactMethod
    public void destroy() {
        // Ignore if it was already destroyed
        if (binder == null && !connecting) return;

        try {
            if(binder != null) {
                binder.destroy();
                binder = null;
            }

            ReactContext context = getReactApplicationContext();
            if(context != null) context.unbindService(this);
        } catch(Exception ex) {
            // This method shouldn't be throwing unhandled errors even if something goes wrong.
            Log.e(Utils.LOG, "An error occurred while destroying the service", ex);
        }
    }

    @ReactMethod
    public void updateOptions(ReadableMap data, final Promise callback) {
        // keep options as we may need them for correct MetadataManager reinitialization later
        options = Arguments.toBundle(data);

        waitForConnection(() -> {
            binder.updateOptions(options);
            callback.resolve(null);
        });
    }

    @ReactMethod
    public void add(ReadableArray tracks, final Integer insertBeforeIndex, final Promise callback) {
        final ArrayList bundleList = Arguments.toList(tracks);

        waitForConnection(() -> {
            List<Track> trackList;

            try {
                trackList = Track.createTracks(getReactApplicationContext(), bundleList, binder.getRatingType());
            } catch(Exception ex) {
                callback.reject("invalid_track_object", ex);
                return;
            }

            List<Track> queue = binder.getPlayback().getQueue();
            // -1 means no index was passed and therefore should be inserted at the end.
            int index = insertBeforeIndex != -1 ? insertBeforeIndex : queue.size();

            if(index < 0 || index > queue.size()) {
                callback.reject("index_out_of_bounds", "The track index is out of bounds");
            } else if(trackList == null || trackList.isEmpty()) {
                callback.reject("invalid_track_object", "Track is missing a required key");
            } else if(trackList.size() == 1) {
                binder.getPlayback().add(trackList.get(0), index, callback);
            } else {
                binder.getPlayback().add(trackList, index, callback);
            }
        });
    }

    @ReactMethod
    public void remove(ReadableArray tracks, final Promise callback) {
        final ArrayList trackList = Arguments.toList(tracks);

        waitForConnection(() -> {
            List<Track> queue = binder.getPlayback().getQueue();
            List<Integer> indexes = new ArrayList<>();

            for(Object o : trackList) {
                int index = o instanceof Integer ? (int)o : Integer.parseInt(o.toString());

                // we do not allow removal of the current item
                int currentIndex = binder.getPlayback().getCurrentTrackIndex();
                if (index == currentIndex) continue;

                if (index >= 0 && index < queue.size()) {
                    indexes.add(index);
                }
            }

            if (!indexes.isEmpty()) {
                binder.getPlayback().remove(indexes, callback);
            } else {
                callback.resolve(null);
            }
        });
    }

    @ReactMethod
    public void updateMetadataForTrack(int index, ReadableMap map, final Promise callback) {
        waitForConnection(() -> {
            ExoPlayback playback = binder.getPlayback();
            List<Track> queue = playback.getQueue();

            if(index < 0 || index >= queue.size()) {
                callback.reject("index_out_of_bounds", "The index is out of bounds");
            } else {
                Track track = queue.get(index);
                track.setMetadata(getReactApplicationContext(), Arguments.toBundle(map), binder.getRatingType());
                playback.updateTrack(index, track);
                callback.resolve(null);
            }
        });
    }

    @ReactMethod
    public void updateNowPlayingMetadata(ReadableMap map, final Promise callback) {
        final Bundle data = Arguments.toBundle(map);

        waitForConnection(() -> {
            NowPlayingMetadata metadata = new NowPlayingMetadata(getReactApplicationContext(), data, binder.getRatingType());
            binder.updateNowPlayingMetadata(metadata);
            callback.resolve(null);
        });
    }

    @ReactMethod
    public void clearNowPlayingMetadata(final Promise callback) {
        waitForConnection(() -> {
            binder.clearNowPlayingMetadata();
            callback.resolve(null);
        });
    }

    @ReactMethod
    public void removeUpcomingTracks(final Promise callback) {
        waitForConnection(() -> {
            binder.getPlayback().removeUpcomingTracks();
            callback.resolve(null);
        });
    }

    @ReactMethod
    public void skip(final int index, final Promise callback) {
        waitForConnection(() -> binder.getPlayback().skip(index, callback));
    }

    @ReactMethod
    public void skipToNext(final Promise callback) {
        waitForConnection(() -> binder.getPlayback().skipToNext(callback));
    }

    @ReactMethod
    public void skipToPrevious(final Promise callback) {
        waitForConnection(() -> binder.getPlayback().skipToPrevious(callback));
    }

    @ReactMethod
    public void reset(final Promise callback) {
        waitForConnection(() -> {
            binder.getPlayback().reset();
            callback.resolve(null);
        });
    }

    @ReactMethod
    public void play(final Promise callback) {
        waitForConnection(() -> {
            binder.getPlayback().play();
            callback.resolve(null);
        });
    }

    @ReactMethod
    public void pause(final Promise callback) {
        waitForConnection(() -> {
            binder.getPlayback().pause();
            callback.resolve(null);
        });
    }

    @ReactMethod
    public void stop(final Promise callback) {
        waitForConnection(() -> {
            binder.getPlayback().stop();
            callback.resolve(null);
        });
    }

    @ReactMethod
    public void seekTo(final float seconds, final Promise callback) {
        waitForConnection(() -> {
            long secondsToSkip = Utils.toMillis(seconds);
            binder.getPlayback().seekTo(secondsToSkip);
            callback.resolve(null);
        });
    }

    @ReactMethod
    public void setVolume(final float volume, final Promise callback) {
        waitForConnection(() -> {
            binder.getPlayback().setVolume(volume);
            callback.resolve(null);
        });
    }

    @ReactMethod
    public void getVolume(final Promise callback) {
        waitForConnection(() -> callback.resolve(binder.getPlayback().getVolume()));
    }

    @ReactMethod
    public void setRate(final float rate, final Promise callback) {
        waitForConnection(() -> {
            binder.getPlayback().setRate(rate);
            callback.resolve(null);
        });
    }

    @ReactMethod
    public void getRate(final Promise callback) {
        waitForConnection(() -> callback.resolve(binder.getPlayback().getRate()));
    }

    @ReactMethod
    public void setRepeatMode(int mode, final Promise callback) {
        waitForConnection(() -> {
            binder.getPlayback().setRepeatMode(mode);
            callback.resolve(null);
        });
    }

    @ReactMethod
    public void getRepeatMode(final Promise callback) {
        waitForConnection(() -> callback.resolve(binder.getPlayback().getRepeatMode()));
    }

    @ReactMethod
    public void getTrack(final int index, final Promise callback) {
        waitForConnection(() -> {
            List<Track> tracks = binder.getPlayback().getQueue();

            if (index >= 0 && index < tracks.size()) {
                callback.resolve(Arguments.fromBundle(tracks.get(index).originalItem));
            } else {
                callback.resolve(null);
            }
        });
    }

    @ReactMethod
    public void getQueue(Promise callback) {
        waitForConnection(() -> {
            List queue = new ArrayList();
            List<Track> tracks = binder.getPlayback().getQueue();

            for(Track track : tracks) {
                queue.add(track.originalItem);
            }

            callback.resolve(Arguments.fromList(queue));
        });
    }

    @ReactMethod
    public void getCurrentTrack(final Promise callback) {
        waitForConnection(() -> callback.resolve(binder.getPlayback().getCurrentTrackIndex()));
    }

    @ReactMethod
    public void getDuration(final Promise callback) {
        waitForConnection(() -> {
            long duration = binder.getPlayback().getDuration();

            if(duration == C.TIME_UNSET) {
                callback.resolve(Utils.toSeconds(0));
            } else {
                callback.resolve(Utils.toSeconds(duration));
            }
        });
    }

    @ReactMethod
    public void getBufferedPosition(final Promise callback) {
        waitForConnection(() -> {
            long position = binder.getPlayback().getBufferedPosition();

            if(position == C.POSITION_UNSET) {
                callback.resolve(Utils.toSeconds(0));
            } else {
                callback.resolve(Utils.toSeconds(position));
            }
        });
    }

    @ReactMethod
    public void getPosition(final Promise callback) {
        waitForConnection(() -> {
            long position = binder.getPlayback().getPosition();

            if(position == C.POSITION_UNSET) {
                callback.reject("unknown", "Unknown position");
            } else {
                callback.resolve(Utils.toSeconds(position));
            }
        });
    }

    @ReactMethod
    public void getState(final Promise callback) {
        if (binder == null) {
            callback.resolve(PlaybackStateCompat.STATE_NONE);
        } else {
            waitForConnection(() -> callback.resolve(binder.getPlayback().getState()));
        }
    }
}
