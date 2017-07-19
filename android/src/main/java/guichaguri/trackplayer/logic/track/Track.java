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
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaQueueItem;
import com.google.android.gms.common.images.WebImage;
import guichaguri.trackplayer.logic.MediaManager;
import guichaguri.trackplayer.logic.Utils;
import java.util.List;
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
    public final boolean urlLocal;

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
    public final boolean artworkLocal;

    /** Cast Media ID */
    public final String mediaId;
    /** Cast Custom Data */
    public final JSONObject customData;

    public int castId = MediaQueueItem.INVALID_ITEM_ID;
    public QueueItem queueItem;
    public MediaQueueItem castQueueItem;

    public Track(Context context, MediaManager manager, ReadableMap data) {
        id = Utils.getString(data, "id");
        queueId = queueIds++;

        url = Utils.getUri(context, data, "url", null);
        urlLocal = isLocal(url);

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
        artworkLocal = isLocal(artwork);

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

    @SuppressWarnings("WrongConstant")
    public Track(MediaManager manager, MediaQueueItem item) {
        MediaInfo info = item.getMedia();
        JSONObject data = info.getCustomData();
        MediaMetadata metadata = info.getMetadata();
        List<WebImage> images = metadata.getImages();

        id = Utils.getString(data, "id", info.getContentId());
        queueId = queueIds++;

        url = Uri.parse(Utils.getString(data, "url", info.getContentId()));
        urlLocal = false;

        duration = info.getStreamDuration();
        type = TrackType.fromString(Utils.getString(data, "type", TrackType.DEFAULT.name));
        contentType = info.getContentType();

        title = metadata.getString(MediaMetadata.KEY_TITLE);
        artist = metadata.getString(MediaMetadata.KEY_ARTIST);
        album = metadata.getString(MediaMetadata.KEY_ALBUM_TITLE);
        genre = Utils.getString(data, "genre", null);
        date = metadata.getDateAsString(MediaMetadata.KEY_RELEASE_DATE);
        description = Utils.getString(data, "description", null);
        rating = RatingCompat.newUnratedRating(manager.getRatingType());

        artwork = !images.isEmpty() ? images.get(0).getUrl() : null;
        artworkLocal = false;

        mediaId = info.getContentId();
        customData = item.getCustomData();

        castId = item.getItemId();
        castQueueItem = item;
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

    private boolean isLocal(Uri uri) {
        String scheme = uri.getScheme();

        return scheme.equals(ContentResolver.SCHEME_FILE) ||
                scheme.equals(ContentResolver.SCHEME_ANDROID_RESOURCE) ||
                scheme.equals(ContentResolver.SCHEME_CONTENT) ||
                scheme.equals("res");
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

    public QueueItem toQueueItem() {
        if(queueItem != null) return queueItem;

        MediaDescriptionCompat desc = new MediaDescriptionCompat.Builder()
                .setMediaId(id)
                .setMediaUri(url)
                .setTitle(title)
                .setSubtitle(artist)
                .setDescription(description)
                .setIconUri(artwork)
                .build();

        queueItem = new QueueItem(desc, queueId);
        return queueItem;
    }

    public MediaQueueItem toCastQueueItem() {
        if(castQueueItem != null) return castQueueItem;

        MediaMetadata metadata = new MediaMetadata();
        metadata.putString(MediaMetadata.KEY_TITLE, title);
        metadata.putString(MediaMetadata.KEY_ARTIST, artist);
        metadata.putString(MediaMetadata.KEY_ALBUM_TITLE, album);
        metadata.putDate(MediaMetadata.KEY_RELEASE_DATE, metadata.getDate(date));
        metadata.addImage(new WebImage(artwork));

        JSONObject obj = new JSONObject();
        Utils.setString(obj, "id", id);
        Utils.setString(obj, "url", url.toString());
        Utils.setString(obj, "type", type.name);
        Utils.setString(obj, "genre", genre);
        Utils.setString(obj, "description", description);

        MediaInfo info = new MediaInfo.Builder(mediaId)
                .setMetadata(metadata)
                .setStreamDuration(duration)
                .setStreamType(MediaInfo.STREAM_TYPE_INVALID)
                .setContentType(contentType)
                .build();

        castQueueItem = new MediaQueueItem.Builder(info)
                .setCustomData(customData)
                .build();
        return castQueueItem;
    }

}
