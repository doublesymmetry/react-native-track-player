package com.guichaguri.trackplayer.service.metadata;

import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;

import java.io.File;

/**
 * @author David Chavez (@dcvz)
 */
public enum SimpleCacheManager {
    INSTANCE;

    // getters and setters

    private SimpleCache _cache;

    public SimpleCache getCache(File cacheDir, long cacheMaxSize) {
        if (_cache != null) return _cache;

        _cache = new SimpleCache(cacheDir, new LeastRecentlyUsedCacheEvictor(cacheMaxSize));
        return _cache;
    }


}