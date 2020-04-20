package com.guichaguri.trackplayer.service.models;

import android.content.Context;
import android.net.Uri;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.RatingCompat;
import com.facebook.react.bridge.ReadableMap;
import com.guichaguri.trackplayer.service.Utils;

import static android.support.v4.media.MediaMetadataCompat.*;

public abstract class TrackMetadata {
    public Uri artwork;

    public String title;
    public String artist;
    public String album;
    public String date;
    public String genre;
    public long duration;

    public RatingCompat rating;

    public void setMetadata(Context context, ReadableMap data, int ratingType) {
        artwork = Utils.getUri(context, data, "artwork");

        title = data.getString("title");
        artist = data.getString("artist");
        album = data.getString("album");
        date = data.getString("date");
        genre = data.getString("genre");
        duration = Utils.toMillis(Utils.getDouble(data, "duration", 0));

        rating = Utils.getRating(data, "rating", ratingType);
    }

    public MediaMetadataCompat.Builder toMediaMetadata() {
        MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder();

        builder.putString(METADATA_KEY_TITLE, title);
        builder.putString(METADATA_KEY_ARTIST, artist);
        builder.putString(METADATA_KEY_ALBUM, album);
        builder.putString(METADATA_KEY_DATE, date);
        builder.putString(METADATA_KEY_GENRE, genre);

        if (duration > 0) {
            builder.putLong(METADATA_KEY_DURATION, duration);
        }

        if (artwork != null) {
            builder.putString(METADATA_KEY_ART_URI, artwork.toString());
        }

        if (rating != null) {
            builder.putRating(METADATA_KEY_RATING, rating);
        }

        return builder;
    }

}
