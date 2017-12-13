package guichaguri.trackplayer.logic;

/**
 * @author Guilherme Chaguri
 */
public class LibHelper {

    private static Boolean EXOPLAYER = null;
    private static Boolean PROXY_CACHE = null;

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

    private static boolean isAvailable(String className) {
        try {
            Class.forName(className);
        } catch(ClassNotFoundException ex) {
            return false;
        }
        return true;
    }

}
