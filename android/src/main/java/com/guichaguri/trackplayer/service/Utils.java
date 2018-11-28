package com.guichaguri.trackplayer.service;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.facebook.react.views.imagehelper.ResourceDrawableIdHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Guichaguri
 */
public class Utils {

    public static final String EVENT_INTENT = "com.guichaguri.trackplayer.event";
    public static final String CONNECT_INTENT = "com.guichaguri.trackplayer.connect";
    public static final String NOTIFICATION_CHANNEL = "com.guichaguri.trackplayer";
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

    public static Uri getUri(Context context, Bundle data, String key) {
        if(!data.containsKey(key)) return null;
        Object obj = data.get(key);

        if(obj instanceof String) {
            // Remote or Local Uri

            return Uri.parse((String)obj);

        } else if(obj instanceof Bundle) {
            // require/import

            String uri = ((Bundle)obj).getString("uri");

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
        if(!data.containsKey(key) || ratingType == RatingCompat.RATING_NONE) {
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

    public static JSONObject bundleToJson(Bundle bundle) {
        JSONObject json = new JSONObject();
        Set<String> keys = bundle.keySet();
        for (String key : keys) {
            try {
                // json.put(key, bundle.get(key)); see edit below
                json.put(key, JSONObject.wrap(bundle.get(key)));
            } catch(JSONException e) {
                //Handle exception here
                Log.d(Utils.LOG, "bundleToJson: Something went wrong, creating json");
            }
        }

        return json;
    }

    public static Bundle jsonStringToBundle(String jsonString){
        try {
            JSONObject jsonObject = toJsonObject(jsonString);
            return jsonToBundle(jsonObject);
        } catch (JSONException ignored) {

        }
        return null;
    }
    private static JSONObject toJsonObject(String jsonString) throws JSONException {
        return new JSONObject(jsonString);
    }
    private static Bundle jsonToBundle(JSONObject jsonObject) throws JSONException {
        Bundle bundle = new Bundle();
        Iterator iter = jsonObject.keys();
        while(iter.hasNext()) {
            String key = (String)iter.next();
            if (key.equals("capabilities") || key.equals("compactCapabilities")) {
                // this check should be more generic but is functional for now.
                ArrayList<Integer> value = new ArrayList<Integer>();
                JSONArray jsonArray = jsonObject.getJSONArray(key);
                int len = jsonArray.length();
                for (int i=0;i<len;i++){
                    value.add(jsonArray.getInt(i));
                }
                bundle.putIntegerArrayList(key,value);
            } else if (key.equals("duration")) {
                // this check should be in Track
                Double value = jsonObject.getDouble(key);
                bundle.putDouble(key, value);
            } else {
                String value = jsonObject.getString(key);
                bundle.putString(key,value);
            }
        }
        return bundle;
    }
    
}
