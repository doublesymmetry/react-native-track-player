package guichaguri.trackplayer.logic;

/**
 * @author Guilherme Chaguri
 */
public class LibHelper {

    public static final boolean EXOPLAYER_AVAILABLE = isAvailable("com.google.android.exoplayer2.SimpleExoPlayer");
    public static final boolean PROXY_CACHE_AVAILABLE = isAvailable("com.danikula.videocache.HttpProxyCacheServer");

    private static boolean isAvailable(String className) {
        try {
            Class.forName(className);
        } catch(ClassNotFoundException ex) {
            return false;
        }
        return true;
    }

}
