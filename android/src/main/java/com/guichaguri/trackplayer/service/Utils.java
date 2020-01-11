package com.guichaguri.trackplayer.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import com.facebook.react.bridge.Promise;
import com.facebook.react.views.imagehelper.ResourceDrawableIdHelper;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.RawResourceDataSource;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheSpan;
import com.google.android.exoplayer2.util.Util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.NavigableSet;

/**
 * @author Guichaguri
 */
public class Utils {

    public static final String EVENT_INTENT = "com.guichaguri.trackplayer.event";
    public static final String CONNECT_INTENT = "com.guichaguri.trackplayer.connect";
    public static final String NOTIFICATION_CHANNEL = "com.guichaguri.trackplayer";
    public static final String LOG = "RNTrackPlayer";

    public static Runnable toRunnable(Promise promise) {
        return () -> promise.resolve(null);
    }

    public static long toMillis(double seconds) {
        return (long)(seconds * 1000);
    }

    public static double toSeconds(long millis) {
        return millis / 1000D;
    }

    public static boolean isLocal(Uri uri) {
        if(uri == null) return false;

        String scheme = uri.getScheme();
        String host = uri.getHost();

        return scheme == null ||
                scheme.equals(ContentResolver.SCHEME_FILE) ||
                scheme.equals(ContentResolver.SCHEME_ANDROID_RESOURCE) ||
                scheme.equals(ContentResolver.SCHEME_CONTENT) ||
                scheme.equals(RawResourceDataSource.RAW_RESOURCE_SCHEME) ||
                scheme.equals("res") ||
                host == null ||
                host.equals("localhost") ||
                host.equals("127.0.0.1") ||
                host.equals("[::1]");
    }

    public static Uri getUri(Context context, Bundle data, String key) {
        if(!data.containsKey(key)) return null;
        Object obj = data.get(key);

        if(obj instanceof String) {
            // Remote or Local Uri

            if(((String)obj).trim().isEmpty())
                throw new RuntimeException("The URL cannot be empty");

            return Uri.parse((String)obj);

        } else if(obj instanceof Bundle) {
            // require/import

            String uri = ((Bundle)obj).getString("uri");

            ResourceDrawableIdHelper helper = ResourceDrawableIdHelper.getInstance();
            int id = helper.getResourceDrawableId(context, uri);

            if(id > 0) {
                // In production, we can obtain the resource uri
                Resources res = context.getResources();

                return new Uri.Builder()
                        .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                        .authority(res.getResourcePackageName(id))
                        .appendPath(res.getResourceTypeName(id))
                        .appendPath(res.getResourceEntryName(id))
                        .build();
            } else {
                // During development, the resources might come directly from the metro server
                return Uri.parse(uri);
            }

        }

        return null;
    }

    public static int getRawResourceId(Context context, Bundle data, String key) {
        if(!data.containsKey(key)) return 0;
        Object obj = data.get(key);

        if(!(obj instanceof Bundle)) return 0;
        String name = ((Bundle)obj).getString("uri");

        if(name == null || name.isEmpty()) return 0;
        name = name.toLowerCase().replace("-", "_");

        try {
            return Integer.parseInt(name);
        } catch (NumberFormatException ex) {
            return context.getResources().getIdentifier(name, "raw", context.getPackageName());
        }
    }

    public static boolean isPlaying(int state) {
        return state == PlaybackStateCompat.STATE_PLAYING || state == PlaybackStateCompat.STATE_BUFFERING;
    }

    public static boolean isPaused(int state) {
        return state == PlaybackStateCompat.STATE_PAUSED || state == PlaybackStateCompat.STATE_CONNECTING;
    }

    public static boolean isStopped(int state) {
        return state == PlaybackStateCompat.STATE_NONE || state == PlaybackStateCompat.STATE_STOPPED;
    }

    public static RatingCompat getRating(Bundle data, String key, int ratingType) {
        if(!data.containsKey(key) || ratingType == RatingCompat.RATING_NONE) {
            return RatingCompat.newUnratedRating(ratingType);
        } else if(ratingType == RatingCompat.RATING_HEART) {
            return RatingCompat.newHeartRating(data.getBoolean(key, true));
        } else if(ratingType == RatingCompat.RATING_THUMB_UP_DOWN) {
            return RatingCompat.newThumbRating(data.getBoolean(key, true));
        } else if(ratingType == RatingCompat.RATING_PERCENTAGE) {
            return RatingCompat.newPercentageRating(data.getFloat(key, 0));
        } else {
            return RatingCompat.newStarRating(ratingType, data.getFloat(key, 0));
        }
    }

    public static void setRating(Bundle data, String key, RatingCompat rating) {
        if(!rating.isRated()) return;
        int ratingType = rating.getRatingStyle();

        if(ratingType == RatingCompat.RATING_HEART) {
            data.putBoolean(key, rating.hasHeart());
        } else if(ratingType == RatingCompat.RATING_THUMB_UP_DOWN) {
            data.putBoolean(key, rating.isThumbUp());
        } else if(ratingType == RatingCompat.RATING_PERCENTAGE) {
            data.putDouble(key, rating.getPercentRating());
        } else {
            data.putDouble(key, rating.getStarRating());
        }
    }

    public static int getInt(Bundle data, String key, int defaultValue) {
        Object value = data.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }

    public static String getNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                Utils.NOTIFICATION_CHANNEL,
                "MusicService",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setShowBadge(false);
            channel.setSound(null, null);
            ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
        }
        return Utils.NOTIFICATION_CHANNEL;
    }

    public static int checkCachedStatus(String key, Cache cache, int length ) {
        int cachedBytes = 0;
        if (length == 0){

            NavigableSet<CacheSpan> cahcedSpans = cache.getCachedSpans(key);
            for (CacheSpan cachedSpan : cahcedSpans){
                cachedBytes += cachedSpan.length;
            }
        }else{
            cachedBytes = (int) cache.getCachedLength(key,0,length);
        }

        Log.d(Utils.LOG, "cache cachePair : Cache: total cached bytes: "+cachedBytes+" for Key: "+key+"//");
        return cachedBytes;
    }

    public static String saveToFile(Context ctx, Cache cache, String key, Uri uri, int length, String path, boolean ForceOverWrite) {


        new DownloadTask().execute(new TaskParams( ctx,  cache,  key,  uri,  length,  path, ForceOverWrite));

        return path;
        /*
        String userAgent = Util.getUserAgent(ctx, "react-native-track-player");

        // the buffer which hosts bytes read from cachedDatasource
        byte[] buffer = new byte[length];
        // the buffer which should host all the data
        byte[] fullBuffer;

        CacheDataSource  dataSource;
        DefaultHttpDataSource ds = new DefaultHttpDataSource(userAgent);
        dataSource = new CacheDataSource(cache, ds);
        dataSource.open(new DataSpec(uri,0,length,key));

        // the length returned by read sometimes doesn't match the full length of the file
        // copying the filled portion of the buffer into a new byteArray to check for integrity against length received from server
        int bufferLength = dataSource.read(buffer,0,length);
        byte[] splitBuffer = Arrays.copyOfRange(buffer,0,bufferLength);
        dataSource.close();
        Log.d(Utils.LOG, "cache - download, splitBuffer size: "+ splitBuffer.length);
        fullBuffer = Arrays.copyOf(splitBuffer,length);

        // if the buffer doesn't contain all the data, manually making a HTTP request
        if( length != splitBuffer.length){
            Log.d(Utils.LOG, "cache - download, file incomplete by : "+ (splitBuffer.length - length));
            HttpURLConnection  connection = (HttpURLConnection) new URL(uri.toString()).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type",
                    "application/json");
            connection.setRequestProperty("Range", "bytes=" + bufferLength + "-" + length + "");
            InputStream is = connection.getInputStream();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            byte[] networkBuff = new byte[length - bufferLength];
            is.read(networkBuff, 0, networkBuff.length);
            Log.d(Utils.LOG, "cache - download, networkBuffer size : "+ (networkBuff.length));
            System.arraycopy(networkBuff, 0, fullBuffer, bufferLength, length - bufferLength);
            is.close();
            os.close();
        }
        File file = new File(path);
        if (!file.exists()) {
                FileOutputStream stream = new FileOutputStream(path);
                stream.write(fullBuffer);
                return file.getAbsolutePath();
        }else{
            if(ForceOverWrite){
                    FileOutputStream stream = new FileOutputStream(path, false);
                    stream.write(fullBuffer);
                    return file.getAbsolutePath();
            } else {
                throw new IOException("file exists");
            }
        }



         */
    }

}
