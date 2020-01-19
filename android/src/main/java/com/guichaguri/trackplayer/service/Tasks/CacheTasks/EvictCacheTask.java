package com.guichaguri.trackplayer.service.Tasks.CacheTasks;

import android.os.AsyncTask;

import com.facebook.react.bridge.Promise;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheSpan;

import java.util.Iterator;
import java.util.NavigableSet;


public class EvictCacheTask extends AsyncTask<EvictCacheParams, Void, Void> {

    Promise callback;
    String key;
    Cache cache;

    @Override
    protected Void doInBackground(EvictCacheParams... params) {
        callback = params[0].callback;
        key = params[0].key;
        cache = params[0].cache;
        try {
            NavigableSet<CacheSpan> spansToRemove = cache.getCachedSpans(key);
            Iterator<CacheSpan> itr = spansToRemove.iterator();

            while (itr.hasNext()) {
                try {
                    cache.removeSpan(itr.next());

                } catch (Cache.CacheException e) {
                    e.printStackTrace();
                    callback.reject(e);
                }
            }
            callback.resolve(itr);
        } catch (Exception e) {
            callback.reject(e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void results) {
        callback.resolve(key);
    }

}

