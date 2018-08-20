package com.guichaguri.trackplayer.service;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import com.facebook.react.bridge.Promise;

/**
 * @author Guichaguri
 */
public class Utils {

    public static final String EVENT_INTENT = "com.guichaguri.trackplayer.event";
    public static final String CONNECT_INTENT = "com.guichaguri.trackplayer.connect";
    public static final String LOG = "RNTrackPlayer";

    public static Runnable toRunnable(Promise promise) {
        return () -> promise.resolve(null);
    }

    public static long toMillis(double seconds) {
        return (long)(seconds * 1000);
    }

    public static double toSeconds(long millis) {
        return millis / 1000D;
    }

    public static boolean isLocal(Uri uri) {
        if(uri == null) return false;

        String scheme = uri.getScheme();
        String host = uri.getHost();

        return scheme == null ||
                scheme.equals(ContentResolver.SCHEME_FILE) ||
                scheme.equals(ContentResolver.SCHEME_ANDROID_RESOURCE) ||
                scheme.equals(ContentResolver.SCHEME_CONTENT) ||
                scheme.equals("res") ||
                host == null ||
                host.equals("localhost") ||
                host.equals("127.0.0.1") ||
                host.equals("[::1]");
    }

    public static boolean isPlaying(int state) {
        return state == PlaybackStateCompat.STATE_PLAYING || state == PlaybackStateCompat.STATE_BUFFERING;
    }

    public static boolean isPaused(int state) {
        return state == PlaybackStateCompat.STATE_PAUSED;
    }

    public static boolean isStopped(int state) {
        return state == PlaybackStateCompat.STATE_NONE || state == PlaybackStateCompat.STATE_STOPPED;
    }

    public static RatingCompat getRating(Bundle data, String key, int ratingType) {
        if(!data.containsKey(key)) {
            return RatingCompat.newUnratedRating(ratingType);
        } else if(ratingType == RatingCompat.RATING_HEART) {
            return RatingCompat.newHeartRating(data.getBoolean(key, true));
        } else if(ratingType == RatingCompat.RATING_THUMB_UP_DOWN) {
            return RatingCompat.newThumbRating(data.getBoolean(key, true));
        } else if(ratingType == RatingCompat.RATING_PERCENTAGE) {
            return RatingCompat.newPercentageRating(data.getFloat(key, 0));
        } else {
            return RatingCompat.newStarRating(ratingType, data.getFloat(key, 0));
        }
    }

    public static void setRating(Bundle data, String key, RatingCompat rating) {
        if(!rating.isRated()) return;
        int ratingType = rating.getRatingStyle();

        if(ratingType == RatingCompat.RATING_HEART) {
            data.putBoolean(key, rating.hasHeart());
        } else if(ratingType == RatingCompat.RATING_THUMB_UP_DOWN) {
            data.putBoolean(key, rating.isThumbUp());
        } else if(ratingType == RatingCompat.RATING_PERCENTAGE) {
            data.putDouble(key, rating.getPercentRating());
        } else {
            data.putDouble(key, rating.getStarRating());
        }
    }

}
