package guichaguri.trackplayer.logic;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.KeyEvent;
import com.facebook.react.bridge.Promise;
import com.facebook.react.views.imagehelper.ResourceDrawableIdHelper;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Guilherme Chaguri
 */
public class Utils {

    public static final String TAG = "ReactNativeTrackPlayer";

    public static String getString(JSONObject object, String key, String def) {
        try {
            return object != null ? object.getString(key) : def;
        } catch(JSONException ex) {
            return def;
        }
    }

    public static void setString(JSONObject object, String key, String value) {
        try {
            if(object != null) object.put(key, value);
        } catch(JSONException ex) {
            // Ignored
        }
    }

    public static long getTime(Bundle map, String key, long def) {
        if(!map.containsKey(key)) return def;
        Object obj = map.get(key);
        if(!(obj instanceof Number)) return def;
        return toMillis(((Number)obj).doubleValue());
    }

    public static void setTime(Bundle bundle, String key, long time) {
        bundle.putDouble(key, toSeconds(time));
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

    public static Uri getResourceUri(Resources res, int id) {
        return new Uri.Builder()
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(res.getResourcePackageName(id))
                .appendPath(res.getResourceTypeName(id))
                .appendPath(res.getResourceEntryName(id))
                .build();
    }

    public static Uri getUri(Context context, Bundle data, String key, Uri def) {
        if(!data.containsKey(key)) return def;

        Object obj = data.get(key);
        if(obj instanceof String) {
            return Uri.parse((String)obj);
        } else if(obj instanceof Bundle) {
            String uri = ((Bundle)obj).getString("uri");

            ResourceDrawableIdHelper helper = ResourceDrawableIdHelper.getInstance();
            int id = helper.getResourceDrawableId(context, uri);

            if(id > 0) {
                return getResourceUri(context.getResources(), id);
            } else {
                // During development, the resources might come directly from the packager server
                return Uri.parse(uri);
            }
        }
        return def;
    }

    public static int getResourceId(Context context, Bundle bundle) {
        ResourceDrawableIdHelper helper = ResourceDrawableIdHelper.getInstance();
        return helper.getResourceDrawableId(context, bundle.getString("uri"));
    }

    public static long toMillis(double seconds) {
        return (long)(seconds * 1000);
    }

    public static double toSeconds(long millis) {
        return millis / 1000D;
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

    public static void resolveCallback(Promise promise) {
        if(promise != null) promise.resolve(null);
    }

    public static void resolveCallback(Promise promise, Object data) {
        if(promise != null) promise.resolve(data);
    }

    public static void rejectCallback(Promise promise, Throwable crash) {
        if(promise != null) promise.reject(crash);
    }

    public static void rejectCallback(Promise promise, String code, String error) {
        if(promise != null) promise.reject(code, error);
    }

    /**
     * Code taken from an updated version of the support library located in PlaybackStateCompat.toKeyCode
     * Replace this to PlaybackStateCompat.toKeyCode when React Native updates the support library
     */
    public static int toKeyCode(long action) {
        if(action == PlaybackStateCompat.ACTION_PLAY) {
            return KeyEvent.KEYCODE_MEDIA_PLAY;
        } else if(action == PlaybackStateCompat.ACTION_PAUSE) {
            return KeyEvent.KEYCODE_MEDIA_PAUSE;
        } else if(action == PlaybackStateCompat.ACTION_SKIP_TO_NEXT) {
            return KeyEvent.KEYCODE_MEDIA_NEXT;
        } else if(action == PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS) {
            return KeyEvent.KEYCODE_MEDIA_PREVIOUS;
        } else if(action == PlaybackStateCompat.ACTION_STOP) {
            return KeyEvent.KEYCODE_MEDIA_STOP;
        } else if(action == PlaybackStateCompat.ACTION_FAST_FORWARD) {
            return KeyEvent.KEYCODE_MEDIA_FAST_FORWARD;
        } else if(action == PlaybackStateCompat.ACTION_REWIND) {
            return KeyEvent.KEYCODE_MEDIA_REWIND;
        } else if(action == PlaybackStateCompat.ACTION_PLAY_PAUSE) {
            return KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE;
        }
        return KeyEvent.KEYCODE_UNKNOWN;
    }

}
