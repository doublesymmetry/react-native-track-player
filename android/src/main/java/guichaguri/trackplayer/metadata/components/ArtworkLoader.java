package guichaguri.trackplayer.metadata.components;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import com.facebook.react.views.imagehelper.ResourceDrawableIdHelper;
import guichaguri.trackplayer.logic.track.TrackURL;
import guichaguri.trackplayer.metadata.Metadata;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author Guilherme Chaguri
 */
public class ArtworkLoader extends Thread {

    private final Context context;
    private final Metadata metadata;

    private final int maxSize;
    private final TrackURL data;

    public ArtworkLoader(Context context, Metadata metadata, TrackURL data, int maxSize) {
        this.context = context;
        this.metadata = metadata;
        this.maxSize = maxSize;
        this.data = data;
    }

    @Override
    public void run() {
        Bitmap bitmap;

        if(data.local) {

            // Retrieve the bitmap from a local resource
            ResourceDrawableIdHelper helper = ResourceDrawableIdHelper.getInstance();
            bitmap = toBitmap(helper.getResourceDrawable(context, data.url));

        } else {

            try {
                // Open connection to a remote resource, download and decode it into a bitmap
                URLConnection con = new URL(data.url).openConnection();
                con.connect();
                InputStream input = con.getInputStream();
                bitmap = BitmapFactory.decodeStream(input);
                input.close();
            } catch(IOException ex) {
                bitmap = null;
            }

        }

        // Resize the bitmap if it's too big. Let's save a bit of memory :)
        if(bitmap != null) bitmap = resize(bitmap, maxSize);

        // Update the metadata with the new artwork
        metadata.updateArtwork(data, bitmap, true);
    }

    private Bitmap toBitmap(Drawable drawable) {
        if(drawable instanceof BitmapDrawable) {
            // When the drawable is from a bitmap, we can just retrieve the bitmap!
            return ((BitmapDrawable)drawable).getBitmap();
        }

        int width = Math.max(drawable.getIntrinsicWidth(), 0);
        int height = Math.max(drawable.getIntrinsicHeight(), 0);

        // Create a bitmap
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // Draw the drawable into a bitmap
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private Bitmap resize(Bitmap bitmap, int maxSize) {
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();

        // If the bitmap is not too big, we'll not resize it
        if(height <= maxSize && width <= maxSize) return bitmap;

        // Create a new bitmap making its width and height not exceed the maximum size
        float ratioBitmap = (float)width / (float)height;
        return Bitmap.createScaledBitmap(bitmap, maxSize, (int)(maxSize / ratioBitmap), true);
    }

}
