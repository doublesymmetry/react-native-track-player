package com.guichaguri.trackplayer.service.player;


import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.cache.CacheEvictor;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheKeyFactory;
import com.google.android.exoplayer2.upstream.cache.CacheSpan;
import com.google.android.exoplayer2.upstream.cache.CacheUtil;
import com.guichaguri.trackplayer.module.MusicEvents;
import com.guichaguri.trackplayer.service.MusicService;
import com.guichaguri.trackplayer.service.Utils;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import android.util.Pair;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.NavigableSet;

/**
 * @author Drazail
 */

public final class Evictor implements CacheEvictor, Comparator<CacheSpan> {
    private final MusicService service;
    private final long maxBytes;
    private final TreeSet<CacheSpan> leastRecentlyUsed;

    private long currentSize;

    public Evictor(MusicService service, long maxBytes) {
        this.service = service;
        this.maxBytes = maxBytes;
        this.leastRecentlyUsed = new TreeSet<>(this);

    }

    @Override
    public boolean requiresCacheSpanTouches() {
        return true;
    }

    @Override
    public void onCacheInitialized() {
        Log.d(Utils.LOG, "cache initialized");

    }

    @Override
    public void onStartFile(Cache cache, String key, long position, long length) {
        Log.d(Utils.LOG, "cache onStartFile : Cache:"+cache+"/ key: "+key+"/ position: "+ position +"/ Length: "+ length + "//");

        if (length != C.LENGTH_UNSET) {
            evictCache(cache, length);
        }
    }

    @Override
    public void onSpanAdded(Cache cache, CacheSpan span) {

        Log.d(Utils.LOG, "cache onSpanAdded : Cache:"+cache+"/ CacheSpan: "+span+"//");
        leastRecentlyUsed.add(span);
        currentSize += span.length;
        evictCache(cache, 0);
        checkCachedStatus(span, cache);
    }

    @Override
    public void onSpanRemoved(Cache cache, CacheSpan span) {
        Log.d(Utils.LOG, "cache onSpanRemoved : Cache:"+cache+"/ CacheSpan: "+span+"//");
        leastRecentlyUsed.remove(span);
        currentSize -= span.length;
    }

    @Override
    public void onSpanTouched(Cache cache, CacheSpan oldSpan, CacheSpan newSpan) {
        Log.d(Utils.LOG, "cache onSpanTouched : Cache:"+cache+"/ oldSpan: "+oldSpan+"/ newSpan: "+newSpan+"//");
        onSpanRemoved(cache, oldSpan);
        onSpanAdded(cache, newSpan);
    }

    @Override
    public int compare(CacheSpan lhs, CacheSpan rhs) {
        long lastTouchTimestampDelta = lhs.lastTouchTimestamp - rhs.lastTouchTimestamp;
        if (lastTouchTimestampDelta == 0) {
            // Use the standard compareTo method as a tie-break.
            return lhs.compareTo(rhs);
        }
        return lhs.lastTouchTimestamp < rhs.lastTouchTimestamp ? -1 : 1;
    }

    private void evictCache(Cache cache, long requiredSpace) {
        Log.d(Utils.LOG, "cache evictCache : Cache:"+cache+"/ requiredSpace: "+requiredSpace+"//");

        while (currentSize + requiredSpace > maxBytes && !leastRecentlyUsed.isEmpty()) {
            try {
                cache.removeSpan(leastRecentlyUsed.first());
            } catch (Cache.CacheException e) {
                // do nothing.
            }
        }
    }

    private void evictSpans(Cache cache, String key) {
        Log.d(Utils.LOG, "cache evictSpans for : Cache:"+cache+"/ key: "+key+"//");
        NavigableSet<CacheSpan> spansToRemove = cache.getCachedSpans(key);
        Iterator<CacheSpan> itr = spansToRemove.iterator();

            try {
                while (itr.hasNext()) {
                    cache.removeSpan(itr.next());
                }
            } catch (Cache.CacheException e) {
                // do nothing.
            }
    }


    private void checkCachedStatus(CacheSpan span, Cache cache) {

       // Uri uri = Uri.parse(url);
        //long fileSize = getFileSize(url);
       // DataSpec dataSpec = new DataSpec(uri,0,fileSize,key);
        // get information about what is cached for the given data spec
       // CacheKeyFactory fac = dataSpec1 -> key;
       // Pair<Long, Long> cachePair= CacheUtil.getCached(dataSpec, cache, fac);

        //long requestedBytes = cachePair.first;
        //long cachedBytes = cachePair.second;
        Long cachedBytes = Long.valueOf(0);
        NavigableSet<CacheSpan> cahcedSpans = cache.getCachedSpans(span.key);
        for (CacheSpan cachedSpan : cahcedSpans){
            cachedBytes += cachedSpan.length;
        }
        Bundle bundle = new Bundle();
        //bundle.putString("url", url);
        bundle.putString("key", span.key);
        bundle.putString("spanIsCached", String.valueOf(span.isCached));
        bundle.putString("bytes cached", String.valueOf(cachedBytes));
        //bundle.putString("content-length",  Long.toString(fileSize));
        //bundle.putString("cached", Long.toString(cachedBytes));
        //bundle.putString("requestedBytes",  Long.toString(requestedBytes));
        service.emit(MusicEvents.PLAYBACK_CACHED, bundle);
        Log.d(Utils.LOG, "cached");

        Log.d(Utils.LOG, "cache cachePair : Cache:"+span.isCached+" total cached bytes: "+cachedBytes+" for Key: "+span.key+"//");
    }

    private long getFileSize(String uri) {
        URL url;
        long fileSize = 0;
        try {
            url = new URL(uri);
            URLConnection conn = null;
            try {
                conn = url.openConnection();
                if(conn instanceof HttpURLConnection) {
                    ((HttpURLConnection)conn).setRequestMethod("GET");
                    fileSize = conn.getContentLength();
                    Log.d(Utils.LOG, "cache HeaderFields : "+conn.getHeaderFields()+" for Key: "+uri+"//");

                }
            } catch (IOException e) {
                Log.d(Utils.LOG, "cache IOException : "+e+" for Key: "+uri+"//");
            } finally {
                if(conn instanceof HttpURLConnection) {
                    ((HttpURLConnection)conn).disconnect();
                }
            }
        } catch (MalformedURLException e) {
            Log.d(Utils.LOG, "cache MalformedURLException : "+e+" for Key: "+uri+"//");
        }
        Log.d(Utils.LOG, "cache fileSize : "+fileSize+" for Key: "+uri+"//");
        return fileSize;
    }

}
