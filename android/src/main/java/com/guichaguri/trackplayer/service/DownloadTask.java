package com.guichaguri.trackplayer.service;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.util.Util;
import com.guichaguri.trackplayer.module.MusicEvents;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

class TaskParams {
    Context ctx;
    MusicService service;
    Cache cache;
    String key;
    Uri uri;
    int length;
    String path;
    boolean ForceOverWrite;
    Promise callback;

    TaskParams(Context ctx, MusicService service, Cache cache, String key, Uri uri, int length, String path, boolean ForceOverWrite, Promise callback) {
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

public class DownloadTask extends AsyncTask<TaskParams, Integer, String> {

    MusicService service;
    String key;
    Uri uri;
    int length;
    int progress = 0;
    Promise callback;

    @Override
    protected String doInBackground(TaskParams... params) {
        callback = params[0].callback;
        service = params[0].service;
        Context ctx = params[0].ctx;
        Cache cache = params[0].cache;
        key = params[0].key;
        uri = params[0].uri;
        length = params[0].length;
        String path = params[0].path;
        boolean ForceOverWrite = params[0].ForceOverWrite;
        String userAgent = Util.getUserAgent(ctx, "react-native-track-player");
        Log.d(Utils.LOG, "cache download : getUserAgent: " + userAgent + "//");
        byte[] buffer = new byte[102400];


        DefaultHttpDataSource ds = new DefaultHttpDataSource(
                userAgent
        );
        CacheDataSource dataSource = new CacheDataSource(cache, ds);
        try {
            dataSource.open(new DataSpec(uri, 0, length, key));
            File file = new File(path);
            if (file.exists()) {
                file.delete();
                FileOutputStream fs = new FileOutputStream(path);
                int read = 0;
                while ((read = dataSource.read(buffer, 0, buffer.length)) > 0) {
                    fs.write(buffer, 0, read);
                    publishProgress(read);
                }
                fs.close();
                dataSource.close();
                ds.close();
                return (file.getAbsolutePath());
            } else {
                FileOutputStream fs = new FileOutputStream(path);
                int read = 0;
                while ((read = dataSource.read(buffer, 0, buffer.length)) > 0) {
                    fs.write(buffer, 0, read);
                    publishProgress(read);
                }
                fs.close();
                dataSource.close();
                ds.close();
                return (file.getAbsolutePath());
            }


        } catch (IOException e) {
            e.printStackTrace();
            callback.reject(e);
        }


        return "0";
    }

    @Override
    protected void onProgressUpdate(Integer... prog) {
        progress = progress + prog[0];
        Bundle bundle = new Bundle();
        bundle.putString("key", key);
        bundle.putInt("progress", progress);
        bundle.putInt("length", length);
        bundle.putString("url", uri.toString());
        service.emit(MusicEvents.DOWNLOAD_PROGRESS, bundle);
    }

    @Override
    protected void onPostExecute(String path) {
        Bundle bundle = new Bundle();
        bundle.putString("key", key);
        bundle.putInt("length", length);
        bundle.putString("url", uri.toString());
        bundle.putString("path", path);
        service.emit(MusicEvents.DOWNLOAD_COMPLETED, bundle);
        callback.resolve(path);

    }
}
