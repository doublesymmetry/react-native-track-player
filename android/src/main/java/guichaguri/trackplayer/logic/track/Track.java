package guichaguri.trackplayer.logic.track;

import android.support.v4.media.RatingCompat;
import com.facebook.react.bridge.ReadableMap;
import guichaguri.trackplayer.logic.MediaManager;
import guichaguri.trackplayer.logic.Utils;

/**
 * @author Guilherme Chaguri
 */
public class Track {

    public final String id;

    public final TrackURL url;
    public final TrackCache cache;
    public final long duration;

    public final String title;
    public final String artist;
    public final String album;
    public final String genre;
    public final String date;
    public final String description;
    public final RatingCompat rating;
    public final TrackURL artwork;

    public Track(Track track) {
        id = track.id;
        url = track.url;
        cache = track.cache;
        duration = track.duration;
        title = track.title;
        artist = track.artist;
        album = track.album;
        genre = track.genre;
        date = track.date;
        description = track.description;
        rating = track.rating;
        artwork = track.artwork;
    }

    public Track(MediaManager manager, ReadableMap data) {
        id = Utils.getString(data, "id");

        url = new TrackURL(data, "url");
        cache = new TrackCache(data, "cache");
        duration = Utils.getTime(data, "duration", 0);

        title = Utils.getString(data, "title");
        artist = Utils.getString(data, "artist");
        album = Utils.getString(data, "album");
        genre = Utils.getString(data, "genre");
        date = Utils.getString(data, "date");
        description = Utils.getString(data, "description");
        rating = Utils.getRating(data, "date", manager.getRatingType());
        artwork = new TrackURL(data, "artwork");
    }

    public boolean needsNetwork() {
        return !url.local || !artwork.local;
    }

}
