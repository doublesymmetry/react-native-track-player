package com.guichaguri.trackplayer.service.models;

import android.content.Context;
import android.net.Uri;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat.QueueItem;
import com.facebook.react.bridge.*;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.upstream.*;
import com.google.android.exoplayer2.util.Util;
import com.guichaguri.trackplayer.service.Utils;
import com.guichaguri.trackplayer.service.player.LocalPlayback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID;
import static android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_URI;

/**
 * @author Guichaguri
 */
public class Track extends TrackMetadata {

    public static List<Track> createTracks(Context context, ReadableArray objects, int ratingType) {
        List<Track> tracks = new ArrayList<>();

        for (int i = 0; i < objects.size(); i++) {
            if (objects.getType(i) != ReadableType.Map) {
                throw new IllegalArgumentException("Expected the track to be an object");
            }
            tracks.add(new Track(context, objects.getMap(i), ratingType));
        }

        return tracks;
    }

    public String id;
    public Uri uri;
    public int resourceId;

    public TrackType type = TrackType.DEFAULT;

    public String contentType;
    public String userAgent;

    public WritableMap originalItem;

    public Map<String, String> headers;

    public final long queueId;

    public Track(Context context, ReadableMap map, int ratingType) {
        id = map.getString("id");

        resourceId = Utils.getRawResourceId(context, map, "url");

        if(resourceId == 0) {
            uri = Utils.getUri(context, map, "url");
        } else {
            uri = RawResourceDataSource.buildRawResourceUri(resourceId);
        }

        String trackType = Utils.getString(map, "type", "default");

        for(TrackType t : TrackType.values()) {
            if(t.name.equalsIgnoreCase(trackType)) {
                type = t;
                break;
            }
        }

        contentType = map.getString("contentType");
        userAgent = map.getString("userAgent");

        if (map.hasKey("headers")) {
            ReadableMap httpHeaders = map.getMap("headers");
            ReadableMapKeySetIterator iterator = httpHeaders.keySetIterator();
            headers = new HashMap<>();
            while (iterator.hasNextKey()) {
                String key = iterator.nextKey();
                headers.put(key, httpHeaders.getString(key));
            }
        }

        setMetadata(context, map, ratingType);

        queueId = System.currentTimeMillis();
        originalItem = new JavaOnlyMap();
        originalItem.merge(map);
    }

    @Override
    public void setMetadata(Context context, ReadableMap data, int ratingType) {
        super.setMetadata(context, data, ratingType);

        if (originalItem != null && originalItem != data)
            originalItem.merge(data);
    }

    @Override
    public MediaMetadataCompat.Builder toMediaMetadata() {
        MediaMetadataCompat.Builder builder = super.toMediaMetadata();

        builder.putString(METADATA_KEY_MEDIA_URI, uri.toString());
        builder.putString(METADATA_KEY_MEDIA_ID, id);

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

    public MediaSource toMediaSource(Context ctx, LocalPlayback playback) {
        // Updates the user agent if not set
        if(userAgent == null || userAgent.isEmpty())
            userAgent = Util.getUserAgent(ctx, "react-native-track-player");

        DataSource.Factory ds;

        if(resourceId != 0) {

            try {
                RawResourceDataSource raw = new RawResourceDataSource(ctx);
                raw.open(new DataSpec(uri));
                ds = new DataSource.Factory() {
                    @Override
                    public DataSource createDataSource() {
                        return raw;
                    }
                };
            } catch(IOException ex) {
                // Should never happen
                throw new RuntimeException(ex);
            }

        } else if(Utils.isLocal(uri)) {

            // Creates a local source factory
            ds = new DefaultDataSourceFactory(ctx, userAgent);

        } else {

            // Creates a default http source factory, enabling cross protocol redirects
            DefaultHttpDataSourceFactory factory = new DefaultHttpDataSourceFactory(
                    userAgent, null,
                    DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
                    DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS,
                    true
            );

            if(headers != null) {
                factory.getDefaultRequestProperties().set(headers);
            }

            ds = playback.enableCaching(factory);

        }

        switch(type) {
            case DASH:
                return createDashSource(ds);
            case HLS:
                return createHlsSource(ds);
            case SMOOTH_STREAMING:
                return createSsSource(ds);
            default:
                return new ProgressiveMediaSource.Factory(ds, new DefaultExtractorsFactory()
                        .setConstantBitrateSeekingEnabled(true))
                        .createMediaSource(uri);
        }
    }

    private MediaSource createDashSource(DataSource.Factory factory) {
        return new DashMediaSource.Factory(new DefaultDashChunkSource.Factory(factory), factory)
                .createMediaSource(uri);
    }

    private MediaSource createHlsSource(DataSource.Factory factory) {
        return new HlsMediaSource.Factory(factory)
                .createMediaSource(uri);
    }

    private MediaSource createSsSource(DataSource.Factory factory) {
        return new SsMediaSource.Factory(new DefaultSsChunkSource.Factory(factory), factory)
                .createMediaSource(uri);
    }

}
