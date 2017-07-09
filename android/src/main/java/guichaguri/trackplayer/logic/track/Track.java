package guichaguri.trackplayer.logic.track;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.MediaSessionCompat.QueueItem;
import android.util.Log;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableMap;
import guichaguri.trackplayer.logic.MediaManager;
import guichaguri.trackplayer.logic.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Guilherme Chaguri
 */
public class Track {

    private static long queueIds = 1;

    public final String id;
    public final long queueId;

    public final Uri url;
    public final long duration;
    /** ExoPlayer track type */
    public final TrackType type;
    /** Cast Mime Type */
    public final String contentType;

    public final String title;
    public final String artist;
    public final String album;
    public final String genre;
    public final String date;
    public final String description;
    public final RatingCompat rating;
    public final Uri artwork;

    /** Cast Media ID */
    public final String mediaId;
    /** Cast Custom Data */
    public final JSONObject customData;

    public Track(Context context, MediaManager manager, ReadableMap data) {
        id = Utils.getString(data, "id");
        queueId = queueIds++;

        url = Utils.getUri(context, data, "url", null);
        duration = Utils.getTime(data, "duration", 0);
        type = TrackType.fromMap(data, "type");
        contentType = Utils.getString(data, "contentType", "audio/mpeg");

        title = Utils.getString(data, "title");
        artist = Utils.getString(data, "artist");
        album = Utils.getString(data, "album");
        genre = Utils.getString(data, "genre");
        date = Utils.getString(data, "date");
        description = Utils.getString(data, "description");
        rating = Utils.getRating(data, "date", manager.getRatingType());
        artwork = Utils.getUri(context, data, "artwork", null);

        boolean sendUrl = Utils.getBoolean(data, "sendUrl", true);
        mediaId = sendUrl ? url.toString() : id;

        ReadableMap custom = Utils.getMap(data, "customData");
        JSONObject obj = null;

        if(custom != null) {
            try {
                obj = transferToObject(custom);
            } catch(JSONException e) {
                Log.w("TrackPlayer", "Couldn't transform a Javascript object to JSON", e);
            }
        }

        customData = obj;
    }

    private JSONObject transferToObject(ReadableMap map) throws JSONException {
        JSONObject obj = new JSONObject();
        ReadableMapKeySetIterator i = map.keySetIterator();

        while(i.hasNextKey()) {
            String key = i.nextKey();
            ReadableType type = map.getType(key);

            if(type == ReadableType.String) {
                obj.put(key, map.getString(key));
            } else if(type == ReadableType.Number) {
                obj.put(key, map.getDouble(key));
            } else if(type == ReadableType.Boolean) {
                obj.put(key, map.getBoolean(key));
            } else if(type == ReadableType.Null) {
                obj.put(key, null);
            } else if(type == ReadableType.Array) {
                obj.put(key, transferToArray(map.getArray(key)));
            } else if(type == ReadableType.Map) {
                obj.put(key, transferToObject(map.getMap(key)));
            }
        }
        return obj;
    }

    private JSONArray transferToArray(ReadableArray arr) throws JSONException {
        JSONArray array = new JSONArray();

        for(int i = 0; i < arr.size(); i++) {
            ReadableType type = arr.getType(i);

            if(type == ReadableType.String) {
                array.put(arr.getString(i));
            } else if(type == ReadableType.Number) {
                array.put(arr.getDouble(i));
            } else if(type == ReadableType.Boolean) {
                array.put(arr.getBoolean(i));
            } else if(type == ReadableType.Null) {
                array.put(null);
            } else if(type == ReadableType.Array) {
                array.put(transferToArray(arr.getArray(i)));
            } else if(type == ReadableType.Map) {
                array.put(transferToObject(arr.getMap(i)));
            }
        }
        return array;
    }

    public boolean needsNetwork() {
        return !isLocal(url) || !isLocal(artwork);
    }

    private boolean isLocal(Uri uri) {
        String scheme = uri.getScheme();

        return scheme.equals(ContentResolver.SCHEME_FILE) ||
                scheme.equals(ContentResolver.SCHEME_ANDROID_RESOURCE) ||
                scheme.equals(ContentResolver.SCHEME_CONTENT);
    }

    public WritableMap toJavascriptMap() {
        WritableMap map = Arguments.createMap();

        map.putString("id", id);
        map.putString("url", url.toString());
        map.putDouble("duration", Utils.toSeconds(duration));
        map.putString("type", type.name);
        map.putString("contentType", contentType);
        map.putString("title", title);
        map.putString("artist", artist);
        map.putString("album", album);
        map.putString("genre", genre);
        map.putString("date", date);
        map.putString("description", description);
        Utils.setRating(map, "rating", rating);
        map.putString("artwork", artwork.toString());

        return map;
    }

    public QueueItem toQueueItem(long queueId) {
        MediaDescriptionCompat desc = new MediaDescriptionCompat.Builder()
                .setMediaId(id)
                .setMediaUri(url)
                .setTitle(title)
                .setSubtitle(artist)
                .setDescription(description)
                .setIconUri(artwork)
                .build();

        return new QueueItem(desc, queueId);
    }

}
