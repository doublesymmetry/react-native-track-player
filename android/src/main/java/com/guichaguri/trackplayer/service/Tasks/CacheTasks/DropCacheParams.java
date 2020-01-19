package com.guichaguri.trackplayer.service.Tasks.CacheTasks;

import com.facebook.react.bridge.Promise;
import com.google.android.exoplayer2.upstream.cache.Cache;

public class DropCacheParams {
    Promise callback;
    Cache cache;

    public DropCacheParams(Cache cache, Promise callback) {
        this.cache = cache;
        this.callback = callback;
    }
}
