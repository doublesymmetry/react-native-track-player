package guichaguri.trackplayer.logic;

import android.content.Context;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

/**
 * @author Guilherme Chaguri
 */
public class LibHelper {

    private static Boolean EXOPLAYER = null;
    private static Boolean PROXY_CACHE = null;
    private static Boolean CHROMECAST = null;

    public static boolean isExoPlayerAvailable() {
        if(EXOPLAYER == null) {
            EXOPLAYER = isAvailable("com.google.android.exoplayer2.SimpleExoPlayer");
        }
        return EXOPLAYER;
    }

    public static boolean isProxyCacheAvailable() {
        if(PROXY_CACHE == null) {
            PROXY_CACHE = isAvailable("com.danikula.videocache.HttpProxyCacheServer");
        }
        return PROXY_CACHE;
    }

    public static boolean isChromecastAvailable(Context context) {
        if(CHROMECAST == null) {
            GoogleApiAvailability availability = GoogleApiAvailability.getInstance();
            CHROMECAST = availability.isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS;
        }
        return CHROMECAST;
    }

    private static boolean isAvailable(String className) {
        try {
            Class.forName(className);
        } catch(ClassNotFoundException ex) {
            return false;
        }
        return true;
    }

}
