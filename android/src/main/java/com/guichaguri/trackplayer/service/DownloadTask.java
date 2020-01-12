package com.guichaguri.trackplayer.service;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

class TaskParams {
    Context ctx;
    Cache cache;
    String key;
    Uri uri;
    int length;
    String path;
    boolean ForceOverWrite;

    TaskParams(Context ctx, Cache cache, String key, Uri uri, int length, String path, boolean ForceOverWrite) {
        this.ctx = ctx;
        this.cache = cache;
        this.key = key;
        this.uri = uri;
        this.length = length;
        this.path = path;
        this.ForceOverWrite = ForceOverWrite;
    }
}

public class DownloadTask extends AsyncTask<TaskParams, Integer, String> {
    @Override
    protected String doInBackground (TaskParams ... params) {

        Context ctx = params[0].ctx;
        Cache cache= params[0].cache;
        String key= params[0].key;
        Uri uri= params[0].uri;
        int length= params[0].length;
        String path= params[0].path;
        boolean ForceOverWrite= params[0].ForceOverWrite;
        String userAgent = Util.getUserAgent(ctx, "react-native-track-player");
        Log.d(Utils.LOG, "cache download : getUserAgent: " + userAgent + "//");
        // the buffer which hosts bytes read from cachedDatasource
        byte[] buffer = new byte[1024];
        // the buffer which should host all the data

        DefaultHttpDataSource ds = new DefaultHttpDataSource(
                userAgent
        );
        CacheDataSource dataSource = new CacheDataSource(cache, ds);
        try {
            dataSource.open(new DataSpec(uri,0, length,key));
            File file = new File(path);
            if(file.exists()){
                file.delete();
                FileOutputStream fs = new FileOutputStream(path);
                int read = 0;
                while( (read = dataSource.read(buffer,0, buffer.length)) > 0) {
                    fs.write(buffer, 0, read);
                }
                fs.close();
                dataSource.close();
                ds.close();
            } else {
                FileOutputStream fs = new FileOutputStream(path);
                int read = 0;
                while( (read = dataSource.read(buffer,0, buffer.length)) > 0) {
                    fs.write(buffer, 0, read);
                }
                fs.close();
                dataSource.close();
                ds.close();
            }



        } catch (IOException e) {
            e.printStackTrace();
        }


        return "0";
    }

}
