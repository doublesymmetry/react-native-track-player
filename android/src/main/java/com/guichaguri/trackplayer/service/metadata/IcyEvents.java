package com.guichaguri.trackplayer.service.metadata;

import android.os.Bundle;
import com.guichaguri.trackplayer.module.MusicEvents;
import com.guichaguri.trackplayer.service.MusicService;
import saschpe.exoplayer2.ext.icy.IcyHttpDataSource;

import java.util.HashMap;

public class IcyEvents implements IcyHttpDataSource.IcyMetadataListener, IcyHttpDataSource.IcyHeadersListener {

    private final MusicService service;

    public IcyEvents(MusicService service) {
        this.service = service;
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
