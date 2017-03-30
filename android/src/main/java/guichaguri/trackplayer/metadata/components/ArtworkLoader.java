package guichaguri.trackplayer.metadata.components;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import com.facebook.react.views.imagehelper.ResourceDrawableIdHelper;
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
    private final boolean local;
    private final String uri;

    public ArtworkLoader(Context context, Metadata metadata, boolean local, String uri, int maxSize) {
        this.context = context;
        this.metadata = metadata;
        this.maxSize = maxSize;
        this.local = local;
        this.uri = uri;
    }

    @Override
    public void run() {
        Bitmap bitmap;

        if(local) {

            ResourceDrawableIdHelper helper = ResourceDrawableIdHelper.getInstance();
            bitmap = toBitmap(helper.getResourceDrawable(context, uri));

        } else {

            try {
                URLConnection con = new URL(uri).openConnection();
                con.connect();
                InputStream input = con.getInputStream();
                bitmap = BitmapFactory.decodeStream(input);
                input.close();
            } catch(IOException ex) {
                bitmap = null;
            }

        }

        if(bitmap != null) bitmap = resize(bitmap, maxSize);

        metadata.updateArtwork(uri, bitmap, true);
    }

    private Bitmap toBitmap(Drawable drawable) {
        if(drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        int width = Math.max(drawable.getIntrinsicWidth(), 0);
        int height = Math.max(drawable.getIntrinsicHeight(), 0);

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private Bitmap resize(Bitmap bitmap, int maxSize) {
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();

        if(height <= maxSize && width <= maxSize) return bitmap;

        float ratioBitmap = (float)width / (float)height;
        return Bitmap.createScaledBitmap(bitmap, maxSize, (int)(maxSize / ratioBitmap), true);
    }

}
