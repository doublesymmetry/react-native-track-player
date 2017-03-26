package guichaguri.trackplayer.logic;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.KeyEvent;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.views.imagehelper.ResourceDrawableIdHelper;

/**
 * @author Guilherme Chaguri
 */
public class Utils {

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

    public static long getTime(ReadableMap map, String key, long def) {
        return map.hasKey(key) && map.getType(key) == ReadableType.Number ? toMillis(map.getDouble(key)) : def;
    }

    public static void setTime(WritableMap map, String key, long time) {
        map.putDouble(key, toSeconds(time));
    }

    public static ReadableArray getArray(ReadableMap map, String key, ReadableArray def) {
        return map.hasKey(key) && map.getType(key) == ReadableType.Array ? map.getArray(key) : def;
    }

    public static RatingCompat getRating(String key, ReadableMap data, int ratingType) {
        if(!data.hasKey(key)) {
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

    public static void setRating(String key, WritableMap data, RatingCompat rating) {
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

    public static long toMillis(double seconds) {
        return (long)(seconds * 1000);
    }

    public static double toSeconds(long millis) {
        return millis / 1000D;
    }

    public static String getLocalResource(ReadableMap local) {
        return local.hasKey("uri") ? local.getString("uri") : null;
    }

    public static int getLocalResourceId(Context context, ReadableMap map) {
        String uri = getLocalResource(map);
        return ResourceDrawableIdHelper.getInstance().getResourceDrawableId(context, uri);
    }

    public static Uri getUri(Context context, ReadableMap map, String key) {
        if(map.getType(key) == ReadableType.Map) {
            String uri = getLocalResource(map.getMap(key));
            return ResourceDrawableIdHelper.getInstance().getResourceDrawableUri(context, uri);
        } else {
            return Uri.parse(map.getString(key));
        }
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

    public static void dispatchEvent(Context context, String event, WritableMap data) {
        Intent i = new Intent(context, PlayerTask.class);

        if(event != null) i.putExtra(PlayerTask.EVENT_TYPE, event);
        if(data != null) i.putExtra(PlayerTask.EVENT_DATA, Arguments.toBundle(data));

        context.startService(i);
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
