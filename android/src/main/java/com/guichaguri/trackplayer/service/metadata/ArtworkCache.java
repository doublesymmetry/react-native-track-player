package com.guichaguri.trackplayer.service.metadata;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.LruCache;
import com.guichaguri.trackplayer.service.Utils;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Loads artworks, caches them using Android's LRU system
 * @author Guichaguri
 */
public class ArtworkCache {

    private final Context context;
    private final MetadataManager manager;
    private final LruCache<Uri, Bitmap> artworks;

    private int maxWidth = 500;
    private int maxHeight = 500;

    private AsyncTask<Void, Void, Bitmap> task;
    private Uri taskUri;

    public ArtworkCache(Context context, MetadataManager manager) {
        this.context = context;
        this.manager = manager;

        // Allocate 12 mb or 1/4 of memory for artworks
        int maxMemory = Math.min(12 * 1024 * 1024, (int)(Runtime.getRuntime().maxMemory() / 4));

        this.artworks = new LruCache<>(maxMemory);
    }

    public void updateOptions(int maxWidth, int maxHeight) {
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }

    public Bitmap getBitmap(Uri uri) {
        return artworks.get(uri);
    }

    public void loadBackground(final Uri uri) {
        Bitmap bitmap = artworks.get(uri);

        if(bitmap != null) {
            manager.updateArtwork(bitmap);
            return;
        }

        if(task != null) {
            if(uri.equals(taskUri)) return;
            task.cancel(true);
        }

        taskUri = uri;
        task = new ArtworkTask(this).execute();
    }

    private Bitmap loadArtwork(Uri uri) throws IOException {
        InputStream in = null;

        try {
            if(Utils.isLocal(uri)) {
                in = context.getContentResolver().openInputStream(uri);
            } else {
                URL url = new URL(uri.toString());
                in = url.openConnection().getInputStream();
            }

            return loadScaledArtwork(in);
        } finally {
            if(in != null) in.close();
        }
    }

    private Bitmap loadScaledArtwork(InputStream in) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true; // Only decodes the artwork size

        BitmapFactory.decodeStream(in, null, opts);

        int factor = Math.min(opts.outWidth / maxWidth, opts.outHeight / maxHeight);

        opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = false;
        opts.inSampleSize = factor;

        // Decodes the whole artwork now
        return BitmapFactory.decodeStream(in, null, opts);
    }

    private static class ArtworkTask extends AsyncTask<Void, Void, Bitmap> {
        private final ArtworkCache cache;

        private ArtworkTask(ArtworkCache cache) {
            this.cache = cache;
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            try {
                return cache.loadArtwork(cache.taskUri);
            } catch(IOException ex) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            cache.artworks.put(cache.taskUri, bitmap);
            cache.manager.updateArtwork(bitmap);
            cache.task = null;
            cache.taskUri = null;
        }

    }
}
