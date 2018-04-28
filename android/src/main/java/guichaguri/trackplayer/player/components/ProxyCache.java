package guichaguri.trackplayer.player.components;

import android.content.Context;
import android.net.Uri;
import com.danikula.videocache.HttpProxyCacheServer;
import com.danikula.videocache.file.FileNameGenerator;
import java.io.File;

/**
 * Proxy Cache using {@link HttpProxyCacheServer}.
 * Intended for players that do not support internal caching
 *
 * @author Guilherme Chaguri
 */
public class ProxyCache implements FileNameGenerator {

    private final HttpProxyCacheServer server;
    private String lastUrl, lastId;

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

    public Uri getURL(Uri uri, String id) {
        String url = uri.toString();

        lastUrl = url;
        lastId = id;

        return Uri.parse(server.getProxyUrl(url));
    }

    public void destroy() {
        server.shutdown();
    }

    @Override
    public String generate(String url) {
        if(lastUrl.equals(url)) return lastId;
        return url;
    }
}
