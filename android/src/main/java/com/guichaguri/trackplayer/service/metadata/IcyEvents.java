package com.guichaguri.trackplayer.service.metadata;

import android.os.Bundle;
import com.facebook.react.modules.network.OkHttpClientProvider;
import com.guichaguri.trackplayer.module.MusicEvents;
import com.guichaguri.trackplayer.service.MusicService;
import com.google.android.exoplayer2.upstream.DataSource;
import saschpe.exoplayer2.ext.icy.IcyHttpDataSource;
import saschpe.exoplayer2.ext.icy.IcyHttpDataSourceFactory;

import java.util.HashMap;

public class IcyEvents implements IcyHttpDataSource.IcyMetadataListener, IcyHttpDataSource.IcyHeadersListener {

    private final MusicService service;
    private final DataSource.Factory ds;

    public IcyEvents(MusicService service, String userAgent) {
        this.service = service;

        this.ds = new IcyHttpDataSourceFactory.Builder(OkHttpClientProvider.getOkHttpClient())
        .setUserAgent(userAgent)
        .setIcyMetadataChangeListener(this)
        .setIcyHeadersListener(this)
        .build();
    }

    public DataSource.Factory getIcyDataSource() {
        return ds;
    }

    @Override
    public void onIcyMetaData(IcyHttpDataSource.IcyMetadata metadata) {
        Bundle bundle = new Bundle();
        Bundle fullMetadata = new Bundle();

        bundle.putString("type", "icy-metadata");
        bundle.putString("title", metadata.getStreamTitle());
        bundle.putString("url", metadata.getStreamUrl());

        HashMap<String, String> data = metadata.getMetadata();
        for(String key : data.keySet()) {
            fullMetadata.putString(key, data.get(key));
        }

        bundle.putBundle("metadata", fullMetadata);

        service.emit(MusicEvents.PLAYBACK_METADATA, bundle);
    }

    @Override
    public void onIcyHeaders(IcyHttpDataSource.IcyHeaders headers) {
        Bundle bundle = new Bundle();

        bundle.putString("type", "icy-headers");
        bundle.putString("name", headers.getName());
        bundle.putString("genre", headers.getGenre());
        bundle.putString("url", headers.getUrl());
        bundle.putInt("bitrate", headers.getBitRate());
        bundle.putBoolean("public", headers.isPublic());

        service.emit(MusicEvents.PLAYBACK_METADATA, bundle);
    }

}
