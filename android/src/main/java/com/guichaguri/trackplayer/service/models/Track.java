package com.guichaguri.trackplayer.service.models;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.MediaSessionCompat.QueueItem;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.guichaguri.trackplayer.service.Utils;
import com.guichaguri.trackplayer.service.metadata.SimpleCacheManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.support.v4.media.MediaMetadataCompat.*;

/**
 * @author Guichaguri
 */
public class Track {

    public static List<Track> createTracks(Context context, List objects, int ratingType) {
        List<Track> tracks = new ArrayList<>();

        for(Object o : objects) {
            if(o instanceof Bundle) {
                tracks.add(new Track(context, (Bundle)o, ratingType));
            } else {
                return null;
            }
        }

        return tracks;
    }

    public String id;
    public Uri uri;

    public TrackType type = TrackType.DEFAULT;

    public String userAgent;

    public Uri artwork;

    public String title;
    public String artist;
    public String album;
    public String date;
    public String genre;
    public long duration;
    public Bundle originalItem;

    public RatingCompat rating;

    public final long queueId;

    public Track(Context context, Bundle bundle, int ratingType) {
        id = bundle.getString("id");
        uri = Utils.getUri(context, bundle, "url");

        String trackType = bundle.getString("type", "default");

        for(TrackType t : TrackType.values()) {
            if(t.name.equalsIgnoreCase(trackType)) {
                type = t;
                break;
            }
        }

        userAgent = bundle.getString("userAgent");
        artwork = Utils.getUri(context, bundle, "artwork");

        title = bundle.getString("title");
        artist = bundle.getString("artist");
        album = bundle.getString("album");
        date = bundle.getString("date");
        genre = bundle.getString("genre");
        duration = Utils.toMillis(bundle.getDouble("duration", 0));

        rating = Utils.getRating(bundle, "rating", ratingType);

        queueId = System.currentTimeMillis();
        originalItem = bundle;
    }

    public MediaMetadataCompat.Builder toMediaMetadata() {
        MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder();

        builder.putString(METADATA_KEY_TITLE, title);
        builder.putString(METADATA_KEY_ARTIST, title);
        builder.putString(METADATA_KEY_ALBUM, album);
        builder.putString(METADATA_KEY_DATE, date);
        builder.putString(METADATA_KEY_GENRE, genre);
        builder.putString(METADATA_KEY_MEDIA_URI, uri.toString());
        builder.putString(METADATA_KEY_MEDIA_ID, id);

        builder.putLong(METADATA_KEY_DURATION, duration);

        if (artwork != null) {
            builder.putString(METADATA_KEY_ART_URI, artwork.toString());
        }

        // TODO
//        builder.putRating(METADATA_KEY_RATING, rating);

        return builder;
    }

    public QueueItem toQueueItem() {
        MediaDescriptionCompat descr = new MediaDescriptionCompat.Builder()
                .setTitle(title)
                .setSubtitle(artist)
                .setMediaId(id)
                .setMediaUri(uri)
                .setIconUri(artwork)
                .build();

        return new QueueItem(descr, queueId);
    }

    public MediaSource toMediaSource(Context ctx, long cacheMaxSize) {
        // Updates the user agent if not set
        if(userAgent == null || !userAgent.isEmpty())
            userAgent = Util.getUserAgent(ctx, "react-native-track-player");

        // Creates a default source factory, enabling cross protocol redirects
        DataSource.Factory ds = new DefaultHttpDataSourceFactory(
                userAgent, null,
                DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
                DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS,
                true
        );

        if(cacheMaxSize > 0 && !Utils.isLocal(uri)) {
            // Enable caching
            File cacheDir = new File(ctx.getCacheDir(), "TrackPlayer");
            Cache cache = SimpleCacheManager.INSTANCE.getCache(cacheDir, cacheMaxSize);
            ds = new CacheDataSourceFactory(cache, ds, CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR, cacheMaxSize);
        }

        switch(type) {
            case DASH:
                return new DashMediaSource.Factory(new DefaultDashChunkSource.Factory(ds), ds)
                        .createMediaSource(uri);
            case HLS:
                return new HlsMediaSource.Factory(ds)
                        .createMediaSource(uri);
            case SMOOTH_STREAMING:
                return new SsMediaSource.Factory(new DefaultSsChunkSource.Factory(ds), ds)
                        .createMediaSource(uri);
            default:
                return new ExtractorMediaSource.Factory(ds)
                        .createMediaSource(uri);
        }
    }
}
