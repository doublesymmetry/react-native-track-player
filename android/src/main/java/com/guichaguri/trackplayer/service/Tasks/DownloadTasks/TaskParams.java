package com.guichaguri.trackplayer.service.Tasks.DownloadTasks;

import android.content.Context;
import android.net.Uri;

import com.facebook.react.bridge.Promise;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.guichaguri.trackplayer.service.MusicService;

public class TaskParams {
    Context ctx;
    MusicService service;
    Cache cache;
    String key;
    Uri uri;
    int length;
    String path;
    boolean ForceOverWrite;
    Promise callback;

    public TaskParams(Context ctx, MusicService service, Cache cache, String key, Uri uri, int length, String path, boolean ForceOverWrite, Promise callback) {
        this.ctx = ctx;
        this.service = service;
        this.cache = cache;
        this.key = key;
        this.uri = uri;
        this.length = length;
        this.path = path;
        this.ForceOverWrite = ForceOverWrite;
        this.callback = callback;
    }
}