package guichaguri.trackplayer.metadata.components;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import com.facebook.react.modules.network.OkHttpClientProvider;
import guichaguri.trackplayer.metadata.Metadata;
import java.io.IOException;
import java.io.InputStream;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * @author Guilherme Chaguri
 */
public class ArtworkLoader extends Thread {

    private final Context context;
    private final Metadata metadata;

    private final int maxSize;
    private final Uri uri;
    private final boolean local;

    public ArtworkLoader(Context context, Metadata metadata, Uri uri, boolean local, int maxSize) {
        this.context = context;
        this.metadata = metadata;
        this.local = local;
        this.maxSize = maxSize;
        this.uri = uri;
    }

    @Override
    public void run() {
        Bitmap bitmap = null;
        InputStream input = null;

        try {
            if(local) {
                input = context.getContentResolver().openInputStream(uri);
            } else {
                // Use the OkHttp client from the provider in case the user has it configured
                OkHttpClient client = OkHttpClientProvider.getOkHttpClient();
                Call call = client.newCall(new Request.Builder().url(uri.toString()).build());
                input = call.execute().body().byteStream();
            }
            bitmap = BitmapFactory.decodeStream(input);
        } catch(IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if(input != null) input.close();
            } catch(Exception ignored) {}
        }

        // Resize the bitmap if it's too big. Let's save a bit of memory :)
        if(bitmap != null) bitmap = resize(bitmap, maxSize);

        // Update the metadata with the new artwork
        metadata.updateArtwork(uri, bitmap);
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
