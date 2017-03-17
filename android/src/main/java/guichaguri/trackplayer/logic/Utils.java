package guichaguri.trackplayer.logic;

import android.content.Context;
import android.net.Uri;
import android.support.v4.media.RatingCompat;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableType;
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
        return map.hasKey(key) && map.getType(key) == ReadableType.Number ? (long)(map.getDouble(key) * 1000) : def;
    }

    public static RatingCompat getRating(int ratingType, ReadableMap data, String key) {
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

}
