package com.guichaguri.trackplayer.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import com.facebook.react.bridge.*;
import com.facebook.react.views.imagehelper.ResourceDrawableIdHelper;
import com.google.android.exoplayer2.upstream.RawResourceDataSource;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Guichaguri
 */
public class Utils {

    public static final String NOTIFICATION_CHANNEL = "com.guichaguri.trackplayer";
    public static final int NOTIFICATION_ID = 103;
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
                scheme.equals(RawResourceDataSource.RAW_RESOURCE_SCHEME) ||
                scheme.equals("res") ||
                host == null ||
                host.equals("localhost") ||
                host.equals("127.0.0.1") ||
                host.equals("[::1]");
    }

    public static Uri getUri(Context context, ReadableMap data, String key) {
        if (!data.hasKey(key)) return null;
        ReadableType type = data.getType(key);

        if (type == ReadableType.String) {
            // Remote or Local Uri

            String uri = data.getString(key);

            if(uri.trim().isEmpty())
                throw new RuntimeException("The URL cannot be empty");

            return Uri.parse(uri);

        } else if (type == ReadableType.Map) {
            // require/import

            String uri = data.getMap(key).getString("uri");

            ResourceDrawableIdHelper helper = ResourceDrawableIdHelper.getInstance();
            int id = helper.getResourceDrawableId(context, uri);

            if(id > 0) {
                // In production, we can obtain the resource uri
                Resources res = context.getResources();

                return new Uri.Builder()
                        .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                        .authority(res.getResourcePackageName(id))
                        .appendPath(res.getResourceTypeName(id))
                        .appendPath(res.getResourceEntryName(id))
                        .build();
            } else {
                // During development, the resources might come directly from the metro server
                return Uri.parse(uri);
            }

        }

        return null;
    }

    public static int getRawResourceId(Context context, ReadableMap data, String key) {
        if(!data.hasKey(key)) return 0;

        if(data.getType(key) != ReadableType.Map) return 0;
        String name = data.getMap(key).getString("uri");

        if(name == null || name.isEmpty()) return 0;
        name = name.toLowerCase().replace("-", "_");

        try {
            return Integer.parseInt(name);
        } catch (NumberFormatException ex) {
            return context.getResources().getIdentifier(name, "raw", context.getPackageName());
        }
    }

    public static boolean isPlaying(int state) {
        return state == PlaybackStateCompat.STATE_PLAYING || state == PlaybackStateCompat.STATE_BUFFERING;
    }

    public static boolean isPaused(int state) {
        return state == PlaybackStateCompat.STATE_PAUSED || state == PlaybackStateCompat.STATE_CONNECTING;
    }

    public static boolean isStopped(int state) {
        return state == PlaybackStateCompat.STATE_NONE || state == PlaybackStateCompat.STATE_STOPPED;
    }

    public static RatingCompat getRating(ReadableMap data, String key, int ratingType) {
        if (!data.hasKey(key) || ratingType == RatingCompat.RATING_NONE) {
            return RatingCompat.newUnratedRating(ratingType);
        } else if (ratingType == RatingCompat.RATING_HEART) {
            return RatingCompat.newHeartRating(getBoolean(data, key, true));
        } else if (ratingType == RatingCompat.RATING_THUMB_UP_DOWN) {
            return RatingCompat.newThumbRating(getBoolean(data, key, true));
        } else if (ratingType == RatingCompat.RATING_PERCENTAGE) {
            return RatingCompat.newPercentageRating((float) getDouble(data, key, 0));
        } else {
            return RatingCompat.newStarRating(ratingType, (float) getDouble(data, key, 0));
        }
    }

    public static void setRating(WritableMap data, String key, RatingCompat rating) {
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

    public static int getInt(ReadableMap data, String key, int defaultValue) {
        if (!data.hasKey(key) || data.getType(key) != ReadableType.Number) {
            return defaultValue;
        } else {
            return data.getInt(key);
        }
    }

    public static double getDouble(ReadableMap data, String key, double defaultValue) {
        if (!data.hasKey(key) || data.getType(key) != ReadableType.Number) {
            return defaultValue;
        } else {
            return data.getDouble(key);
        }
    }

    public static boolean getBoolean(ReadableMap data, String key, boolean defaultValue) {
        if (!data.hasKey(key) || data.getType(key) != ReadableType.Boolean) {
            return defaultValue;
        } else {
            return data.getBoolean(key);
        }
    }

    public static String getString(ReadableMap data, String key, String defaultValue) {
        if (!data.hasKey(key) || data.getType(key) != ReadableType.String) {
            return defaultValue;
        } else {
            return data.getString(key);
        }
    }

    public static List<Integer> getIntegerList(ReadableMap data, String key, List<Integer> defaultValue) {
        if (!data.hasKey(key) || data.getType(key) != ReadableType.Array) {
            return defaultValue;
        }

        ReadableArray array = data.getArray(key);
        List<Integer> list = new ArrayList<>();

        for(int i = 0; i < array.size(); i++) {
            if (array.getType(i) == ReadableType.Number)
                list.add(array.getInt(i));
        }

        return list;
    }
    
}
