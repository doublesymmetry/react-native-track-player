package guichaguri.trackplayer.logic;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.KeyEvent;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.views.imagehelper.ResourceDrawableIdHelper;
import guichaguri.trackplayer.BuildConfig;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Guilherme Chaguri
 */
public class Utils {

    private static final boolean LOG = BuildConfig.DEBUG || Build.PRODUCT.contains("sdk") || Boolean.getBoolean("guichaguri.trackplayer.log");

    public static void log(String msg, Object ... args) {
        if(LOG) {
            Log.i("ReactNativeTrackPlayer", String.format(msg, args));
        }
    }

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

    public static String getString(ReadableMap map, String key) {
        return getString(map, key, null);
    }

    public static String getString(ReadableMap map, String key, String def) {
        return map.hasKey(key) && map.getType(key) == ReadableType.String ? map.getString(key) : def;
    }

    public static int getInt(ReadableMap map, String key, int def) {
        return map.hasKey(key) && map.getType(key) == ReadableType.Number ? map.getInt(key) : def;
    }

    public static double getDouble(ReadableMap map, String key, double def) {
        return map.hasKey(key) && map.getType(key) == ReadableType.Number ? map.getDouble(key) : def;
    }

    public static boolean getBoolean(ReadableMap map, String key, boolean def) {
        return map.hasKey(key) && map.getType(key) == ReadableType.Boolean ? map.getBoolean(key) : def;
    }

    public static ReadableMap getMap(ReadableMap map, String key) {
        return getMap(map, key, null);
    }

    public static ReadableMap getMap(ReadableMap map, String key, ReadableMap def) {
        return map.hasKey(key) && map.getType(key) == ReadableType.Map ? map.getMap(key) : def;
    }

    public static long getTime(ReadableMap map, String key, long def) {
        return map.hasKey(key) && map.getType(key) == ReadableType.Number ? toMillis(map.getDouble(key)) : def;
    }

    public static void setTime(WritableMap map, String key, long time) {
        map.putDouble(key, toSeconds(time));
    }

    public static ReadableArray getArray(ReadableMap map, String key, ReadableArray def) {
        return map.hasKey(key) && map.getType(key) == ReadableType.Array ? map.getArray(key) : def;
    }

    public static RatingCompat getRating(ReadableMap data, String key, int ratingType) {
        if(!data.hasKey(key) || data.getType(key) == ReadableType.Null) {
            return RatingCompat.newUnratedRating(ratingType);
        } else if(ratingType == RatingCompat.RATING_HEART) {
            return RatingCompat.newHeartRating(Utils.getBoolean(data, key, true));
        } else if(ratingType == RatingCompat.RATING_THUMB_UP_DOWN) {
            return RatingCompat.newThumbRating(Utils.getBoolean(data, key, true));
        } else if(ratingType == RatingCompat.RATING_PERCENTAGE) {
            return RatingCompat.newPercentageRating((float)Utils.getDouble(data, key, 0));
        } else {
            return RatingCompat.newStarRating(ratingType, (float)Utils.getDouble(data, key, 0));
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

    public static Uri getUri(Context context, ReadableMap map, String key, Uri def) {
        if(!map.hasKey(key)) return def;

        ReadableType type = map.getType(key);
        if(type == ReadableType.String) {
            return Uri.parse(map.getString(key));
        } else if(type == ReadableType.Map) {
            String uri = map.getMap(key).getString("uri");
            return ResourceDrawableIdHelper.getInstance().getResourceDrawableUri(context, uri);
        }
        return def;
    }

    public static int getResourceId(Context context, ReadableMap map) {
        ResourceDrawableIdHelper helper = ResourceDrawableIdHelper.getInstance();
        return helper.getResourceDrawableId(context, map.getString("uri"));
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
        return state == PlaybackStateCompat.STATE_NONE || state == PlaybackStateCompat.STATE_STOPPED ||
                state == PlaybackStateCompat.STATE_ERROR;
    }

    public static void triggerCallback(Callback callback, Object ... args) {
        if(callback != null) callback.invoke(args);
    }

    public static void resolveCallback(Promise promise, Object ... data) {
        if(promise != null) {
            Object value = null;
            if(data.length == 1) {
                value = data[0];
            } else if(data.length > 1) {
                value = Arguments.fromJavaArgs(data);
            }
            promise.resolve(value);
        }
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
