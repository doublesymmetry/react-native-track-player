package com.guichaguri.trackplayer.module;

import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import com.facebook.react.bridge.*;
import com.google.android.exoplayer2.C;
import com.guichaguri.trackplayer.service.MusicManager;
import com.guichaguri.trackplayer.service.Utils;
import com.guichaguri.trackplayer.service.metadata.MetadataManager;
import com.guichaguri.trackplayer.service.models.NowPlayingMetadata;
import com.guichaguri.trackplayer.service.models.Track;
import com.guichaguri.trackplayer.service.player.ExoPlayback;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * @author Guichaguri
 */
public class MusicModule extends ReactContextBaseJavaModule {

    private MusicManager manager;

    public MusicModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    @Nonnull
    public String getName() {
        return "TrackPlayerModule";
    }

    @Override
    public void onCatalystInstanceDestroy() {
        if(manager != null) {
            manager.destroy();
            manager = null;
        }
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

        return constants;
    }

    @ReactMethod
    public void setupPlayer(ReadableMap data, Promise promise) {
        if (manager == null) manager = new MusicManager(getReactApplicationContext());
        manager.switchPlayback(manager.createLocalPlayback(data));
        promise.resolve(null);
    }

    @ReactMethod
    public void destroy() {
        // Ignore if it was already destroyed
        if (manager == null) return;

        try {
            manager.destroy();
            manager = null;
        } catch(Exception ex) {
            // This method shouldn't be throwing unhandled errors even if something goes wrong.
            Log.e(Utils.LOG, "An error occurred while destroying the service", ex);
        }
    }

    @ReactMethod
    public void updateOptions(ReadableMap data, Promise callback) {
        if (manager == null) manager = new MusicManager(getReactApplicationContext());

        manager.setStopWithApp(Utils.getBoolean(data, "stopWithApp", false));
        manager.setAlwaysPauseOnInterruption(Utils.getBoolean(data, "alwaysPauseOnInterruption", false));
        manager.getMetadata().updateOptions(data);
        callback.resolve(null);
    }

    @ReactMethod
    public void add(ReadableArray tracks, String insertBeforeId, Promise callback) {

        List<Track> trackList;

        try {
            trackList = Track.createTracks(getReactApplicationContext(), tracks, manager.getMetadata().getRatingType());
        } catch(Exception ex) {
            callback.reject("invalid_track_object", ex);
            return;
        }

        List<Track> queue = manager.getPlayback().getQueue();
        int index = -1;

        if(insertBeforeId != null) {
            for(int i = 0; i < queue.size(); i++) {
                if(queue.get(i).id.equals(insertBeforeId)) {
                    index = i;
                    break;
                }
            }
        } else {
            index = queue.size();
        }

        if(index == -1) {
            callback.reject("track_not_in_queue", "Given track ID was not found in queue");
        } else if(trackList.isEmpty()) {
            callback.reject("invalid_track_object", "Track is missing a required key");
        } else if(trackList.size() == 1) {
            manager.getPlayback().add(trackList.get(0), index, callback);
        } else {
            manager.getPlayback().add(trackList, index, callback);
        }
    }

    @ReactMethod
    public void remove(ReadableArray tracks, final Promise callback) {
        final ArrayList trackList = Arguments.toList(tracks);

        List<Track> queue = manager.getPlayback().getQueue();
        List<Integer> indexes = new ArrayList<>();

        for(Object o : trackList) {
            String id = o.toString();

            for(int i = 0; i < queue.size(); i++) {
                if(queue.get(i).id.equals(id)) {
                    indexes.add(i);
                    break;
                }
            }
        }

        if (!indexes.isEmpty()) {
            manager.getPlayback().remove(indexes, callback);
        } else {
            callback.resolve(null);
        }
    }

    @ReactMethod
    public void updateMetadataForTrack(String id, ReadableMap map, Promise callback) {
        ExoPlayback playback = manager.getPlayback();
        List<Track> queue = playback.getQueue();
        Track track = null;
        int index = -1;

        for(int i = 0; i < queue.size(); i++) {
            track = queue.get(i);

            if(track.id.equals(id)) {
                index = i;
                break;
            }
        }

        if(index == -1) {
            callback.reject("track_not_in_queue", "No track found");
        } else {
            track.setMetadata(getReactApplicationContext(), map, manager.getMetadata().getRatingType());
            playback.updateTrack(index, track);
            callback.resolve(null);
        }
    }

    @ReactMethod
    public void updateNowPlayingMetadata(ReadableMap data, Promise callback) {
        MetadataManager md = manager.getMetadata();

        // TODO elapsedTime
        md.updateMetadata(new NowPlayingMetadata(getReactApplicationContext(), data, manager.getMetadata().getRatingType()));
        md.setActive(true);

        callback.resolve(null);
    }

    @ReactMethod
    public void clearNowPlayingMetadata(Promise callback) {
        manager.getMetadata().setActive(false);
        callback.resolve(null);
    }

    @ReactMethod
    public void removeUpcomingTracks(Promise callback) {
        manager.getPlayback().removeUpcomingTracks();
        callback.resolve(null);
    }

    @ReactMethod
    public void skip(String track, Promise callback) {
        manager.getPlayback().skip(track, callback);
    }

    @ReactMethod
    public void skipToNext(Promise callback) {
        manager.getPlayback().skipToNext(callback);
    }

    @ReactMethod
    public void skipToPrevious(Promise callback) {
        manager.getPlayback().skipToPrevious(callback);
    }

    @ReactMethod
    public void reset(Promise callback) {
        manager.getPlayback().reset();
        callback.resolve(null);
    }

    @ReactMethod
    public void play(Promise callback) {
        manager.getPlayback().play();
        callback.resolve(null);
    }

    @ReactMethod
    public void pause(Promise callback) {
        manager.getPlayback().pause();
        callback.resolve(null);
    }

    @ReactMethod
    public void stop(Promise callback) {
        manager.getPlayback().stop();
        callback.resolve(null);
    }

    @ReactMethod
    public void seekTo(float seconds, Promise callback) {
        manager.getPlayback().seekTo(Utils.toMillis(seconds));
        callback.resolve(null);
    }

    @ReactMethod
    public void setVolume(float volume, Promise callback) {
        manager.getPlayback().setVolume(volume);
        callback.resolve(null);
    }

    @ReactMethod
    public void getVolume(Promise callback) {
        callback.resolve(manager.getPlayback().getVolume());
    }

    @ReactMethod
    public void setRate(float rate, Promise callback) {
        manager.getPlayback().setRate(rate);
        callback.resolve(null);
    }

    @ReactMethod
    public void getRate(final Promise callback) {
        callback.resolve(manager.getPlayback().getRate());
    }

    @ReactMethod
    public void getTrack(String id, Promise callback) {
        List<Track> tracks = manager.getPlayback().getQueue();

        for(Track track : tracks) {
            if(track.id.equals(id)) {
                callback.resolve(track.originalItem);
                return;
            }
        }

        callback.resolve(null);
    }

    @ReactMethod
    public void getQueue(Promise callback) {
        WritableArray queue = Arguments.createArray();
        List<Track> tracks = manager.getPlayback().getQueue();

        for(Track track : tracks) {
            queue.pushMap(track.originalItem);
        }

        callback.resolve(queue);
    }

    @ReactMethod
    public void getCurrentTrack(Promise callback) {
        Track track = manager.getPlayback().getCurrentTrack();
        callback.resolve(track == null ? null : track.id);
    }

    @ReactMethod
    public void getDuration(final Promise callback) {
        long duration = manager.getPlayback().getDuration();
        callback.resolve(duration == C.TIME_UNSET ? 0 : Utils.toSeconds(duration));
    }

    @ReactMethod
    public void getBufferedPosition(final Promise callback) {
        long position = manager.getPlayback().getBufferedPosition();
        callback.resolve(position == C.POSITION_UNSET ? 0 : Utils.toSeconds(position));
    }

    @ReactMethod
    public void getPosition(final Promise callback) {
        long position = manager.getPlayback().getPosition();

        if(position == C.POSITION_UNSET) {
            callback.reject("unknown", "Unknown position");
        } else {
            callback.resolve(Utils.toSeconds(position));
        }
    }

    @ReactMethod
    public void getState(Promise callback) {
        callback.resolve(manager.getPlayback().getState());
    }
}
