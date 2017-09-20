package guichaguri.trackplayer.logic.track;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.MediaSessionCompat.QueueItem;
import android.util.Log;
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

    public Track(Context context, MediaManager manager, Bundle data) {
        id = data.getString("id");
        queueId = queueIds++;

        url = Utils.getUri(context, data, "url", null);
        urlLocal = isLocal(url);

        duration = Utils.getTime(data, "duration", 0);
        type = TrackType.fromBundle(data, "type");
        contentType = data.getString("contentType", "audio/mpeg");

        title = data.getString("title");
        artist = data.getString("artist");
        album = data.getString("album");
        genre = data.getString("genre");
        date = data.getString("date");
        description = data.getString("description");
        rating = Utils.getRating(data, "date", manager.getRatingType());

        artwork = Utils.getUri(context, data, "artwork", null);
        artworkLocal = isLocal(artwork);

        boolean sendUrl = data.getBoolean("sendUrl", true);
        mediaId = sendUrl && url != null ? url.toString() : id;

        Bundle custom = data.getBundle("customData");
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

    private JSONObject transferToObject(Bundle bundle) throws JSONException {
        JSONObject json = new JSONObject();

        for(String key : bundle.keySet()) {
            Object obj = bundle.get(key);

            if(obj == null) {
                json.put(key, null);
            } else if(obj instanceof List) {
                json.put(key, transferToArray((List)obj));
            } else if(obj instanceof Bundle) {
                json.put(key, transferToObject((Bundle)obj));
            } else {
                json.put(key, obj);
            }
        }
        return json;
    }

    private JSONArray transferToArray(List list) throws JSONException {
        JSONArray array = new JSONArray();

        for(Object obj : list) {
            if(obj == null) {
                array.put(null);
            } else if(obj instanceof List) {
                array.put(transferToArray((List)obj));
            } else if(obj instanceof Bundle) {
                array.put(transferToObject((Bundle)obj));
            } else {
                array.put(obj);
            }
        }
        return array;
    }

    private boolean isLocal(Uri uri) {
        if(uri == null) return false;

        String scheme = uri.getScheme();

        return scheme == null ||
                scheme.equals(ContentResolver.SCHEME_FILE) ||
                scheme.equals(ContentResolver.SCHEME_ANDROID_RESOURCE) ||
                scheme.equals(ContentResolver.SCHEME_CONTENT) ||
                scheme.equals("res");
    }

    public Bundle toBundle() {
        Bundle bundle = new Bundle();

        bundle.putString("id", id);
        bundle.putString("url", url.toString());
        bundle.putDouble("duration", Utils.toSeconds(duration));
        bundle.putString("type", type.name);
        bundle.putString("contentType", contentType);
        bundle.putString("title", title);
        bundle.putString("artist", artist);
        bundle.putString("album", album);
        bundle.putString("genre", genre);
        bundle.putString("date", date);
        bundle.putString("description", description);
        Utils.setRating(bundle, "rating", rating);
        bundle.putString("artwork", artwork.toString());

        return bundle;
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
