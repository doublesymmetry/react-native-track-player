package com.guichaguri.trackplayer.service.Tasks.CacheTasks;

import com.facebook.react.bridge.Promise;
import com.google.android.exoplayer2.upstream.cache.Cache;

public class EvictCacheParams {
    Promise callback;
    Cache cache;
    String key;

    public EvictCacheParams(Cache cache, String key, Promise callback) {
        this.cache = cache;
        this.callback = callback;
        this.key = key;
    }
}
