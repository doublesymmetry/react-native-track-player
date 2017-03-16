package guichaguri.trackplayer.logic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.views.imagehelper.ResourceDrawableIdHelper;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author Guilherme Chaguri
 */
public class Utils {

    public static String getLocalResource(ReadableMap local) {
        return local.hasKey("uri") ? local.getString("uri") : null;
    }

    public static int getLocalResourceId(Context context, ReadableMap map) {
        String uri = getLocalResource(map);
        return ResourceDrawableIdHelper.getInstance().getResourceDrawableId(context, uri);
    }

    public static Bitmap getBitmap(Context context, ReadableMap map, String key) {
        if(map.getType(key) == ReadableType.Map) {

            String uri = getLocalResource(map.getMap(key));
            Drawable d = ResourceDrawableIdHelper.getInstance().getResourceDrawable(context, uri);

            if(d instanceof BitmapDrawable) {
                return ((BitmapDrawable)d).getBitmap();
            } else {
                return BitmapFactory.decodeFile(uri);
            }

        } else {

            Bitmap bitmap;

            try {
                URLConnection con = new URL(map.getString(key)).openConnection();
                con.connect();
                InputStream input = con.getInputStream();
                bitmap = BitmapFactory.decodeStream(input);
                input.close();
            } catch(IOException ex) {
                bitmap = null;
            }

            return bitmap;

        }
    }

    public static Uri getUri(Context context, ReadableMap map, String key) {
        if(map.getType(key) == ReadableType.Map) {
            String uri = getLocalResource(map.getMap(key));
            return ResourceDrawableIdHelper.getInstance().getResourceDrawableUri(context, uri);
        } else {
            return Uri.parse(map.getString(key));
        }
    }

}
