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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Arrays;

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

        // the buffer which hosts bytes read from cachedDatasource
        byte[] buffer = new byte[length];
        // the buffer which should host all the data
        byte[] fullBuffer;

        CacheDataSource dataSource;
        DefaultHttpDataSource ds = new DefaultHttpDataSource(userAgent);
        dataSource = new CacheDataSource(cache, ds);
        try {
           dataSource.open(new DataSpec(uri, 0, length, key));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // the length returned by read sometimes doesn't match the full length of the file
        // copying the filled portion of the buffer into a new byteArray to check for integrity against length received from server
        int bufferLength = 0;
        try {
            bufferLength = dataSource.read(buffer,0,length);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] splitBuffer = Arrays.copyOfRange(buffer,0,bufferLength);
        try {
            dataSource.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(Utils.LOG, "cache - download, splitBuffer size: "+ splitBuffer.length);
        fullBuffer = Arrays.copyOf(splitBuffer,length);

        // if the buffer doesn't contain all the data, manually making a HTTP request
        if( length != splitBuffer.length){
            Log.d(Utils.LOG, "cache - download, file incomplete by : "+ (splitBuffer.length - length));
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(uri.toString()).openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                connection.setRequestMethod("GET");
            } catch (ProtocolException e) {
                e.printStackTrace();
            }
            connection.setRequestProperty("Content-Type",
                    "application/json");
            connection.setRequestProperty("Range", "bytes=" + bufferLength + "-" + length + "");
            InputStream is = null;
            try {
                is = connection.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            byte[] networkBuff = new byte[length - bufferLength];
            try {
                is.read(networkBuff);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d(Utils.LOG, "cache - download, networkBuffer size : "+ (networkBuff.length));
            System.arraycopy(networkBuff, 0, fullBuffer, bufferLength, length - bufferLength);
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        File file = new File(path);
        if (!file.exists()) {
            try {
                FileOutputStream stream = new FileOutputStream(path);
                stream.write(fullBuffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return file.getAbsolutePath();
        }else{
            if(ForceOverWrite){
                try {
                    FileOutputStream stream = new FileOutputStream(path, false);
                    stream.write(fullBuffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return file.getAbsolutePath();
            }
        }

        return "0";
    }

}
