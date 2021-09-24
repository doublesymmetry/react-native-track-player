package com.guichaguri.trackplayer.service.models;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.MediaSessionCompat.QueueItem;
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

import static android.support.v4.media.MediaMetadataCompat.*;

/**
 * @author Guichaguri
 */
public class Track extends TrackMetadata {

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

    public Uri uri;
    public int resourceId;

    public TrackType type = TrackType.DEFAULT;

    public String contentType;
    public String userAgent;

    public Bundle originalItem;

    public Map<String, String> headers;

    public final long queueId;

    public Track(Context context, Bundle bundle, int ratingType) {
        resourceId = Utils.getRawResourceId(context, bundle, "url");

        if(resourceId == 0) {
            uri = Utils.getUri(context, bundle, "url");
        } else {
            uri = RawResourceDataSource.buildRawResourceUri(resourceId);
        }

        String trackType = bundle.getString("type", "default");

        for(TrackType t : TrackType.values()) {
            if(t.name.equalsIgnoreCase(trackType)) {
                type = t;
                break;
            }
        }

        contentType = bundle.getString("contentType");
        userAgent = bundle.getString("userAgent");

        Bundle httpHeaders = bundle.getBundle("headers");
        if(httpHeaders != null) {
            headers = new HashMap<>();
            for(String header : httpHeaders.keySet()) {
                headers.put(header, httpHeaders.getString(header));
            }
        }

        setMetadata(context, bundle, ratingType);

        queueId = System.currentTimeMillis();
        originalItem = bundle;
    }

    @Override
    public void setMetadata(Context context, Bundle bundle, int ratingType) {
        super.setMetadata(context, bundle, ratingType);

        if (originalItem != null && originalItem != bundle)
            originalItem.putAll(bundle);
    }

    @Override
    public MediaMetadataCompat.Builder toMediaMetadata() {
        MediaMetadataCompat.Builder builder = super.toMediaMetadata();

        builder.putString(METADATA_KEY_MEDIA_URI, uri.toString());

        return builder;
    }

    public QueueItem toQueueItem() {
        MediaDescriptionCompat descr = new MediaDescriptionCompat.Builder()
                .setTitle(title)
                .setSubtitle(artist)
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
