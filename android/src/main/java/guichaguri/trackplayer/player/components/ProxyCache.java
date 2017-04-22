package guichaguri.trackplayer.player.components;

import android.content.Context;
import com.danikula.videocache.HttpProxyCacheServer;
import com.danikula.videocache.file.FileNameGenerator;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Proxy Cache using {@link HttpProxyCacheServer}.
 * Intended for players that do not support internal caching
 *
 * @author Guilherme Chaguri
 */
public class ProxyCache implements FileNameGenerator {

    private final HttpProxyCacheServer server;
    private final Map<String, String> urlIds = new HashMap<>();

    public ProxyCache(Context context, int maxFiles, long maxSize) {
        HttpProxyCacheServer.Builder builder = new HttpProxyCacheServer.Builder(context);

        builder.cacheDirectory(new File(context.getCacheDir(), "TrackPlayer"));
        builder.fileNameGenerator(this);

        if(maxFiles > 0) {
            builder.maxCacheFilesCount(maxFiles);
        } else if(maxSize > 0) {
            builder.maxCacheSize(maxSize);
        }

        server = builder.build();
    }

    public String getURL(String url, String id) {
        if(id != null) urlIds.put(url, id);
        return server.getProxyUrl(url);
    }

    public void destroy() {
        server.shutdown();
    }

    @Override
    public String generate(String url) {
        String id = urlIds.get(url);
        return id == null ? url : id;
    }
}
