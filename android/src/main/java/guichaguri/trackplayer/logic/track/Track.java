package guichaguri.trackplayer.logic.track;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.MediaSessionCompat.QueueItem;
import guichaguri.trackplayer.logic.MediaManager;
import guichaguri.trackplayer.logic.Utils;

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

    public QueueItem queueItem;
    public Bundle originalItem;

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
        rating = Utils.getRating(data, "date", manager.getMetadata().getRatingType());

        artwork = Utils.getUri(context, data, "artwork", null);
        artworkLocal = isLocal(artwork);

        originalItem = data;
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

}
