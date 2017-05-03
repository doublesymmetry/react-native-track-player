package guichaguri.trackplayer.logic.components;

import android.os.Binder;
import android.support.v4.media.MediaBrowserCompat.MediaItem;
import android.support.v4.media.MediaBrowserServiceCompat.Result;
import android.support.v4.media.MediaDescriptionCompat;
import guichaguri.trackplayer.logic.MediaManager;
import guichaguri.trackplayer.logic.Utils;
import guichaguri.trackplayer.logic.track.Track;
import guichaguri.trackplayer.logic.workers.PlayerService;
import guichaguri.trackplayer.player.Player;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Guilherme Chaguri
 */
public class BrowserWrapper extends Binder {

    private final PlayerService service;
    private final MediaManager manager;

    public BrowserWrapper(PlayerService service, MediaManager manager) {
        this.service = service;
        this.manager = manager;
    }

    public void sendQueue(List<Result<List<MediaItem>>> results) {
        List<MediaItem> items = createItems();
        for(Result<List<MediaItem>> result : results) {
            result.sendResult(items);
        }
    }

    public void sendQueue(Result<List<MediaItem>> result) {
        result.sendResult(createItems());
    }

    private List<MediaItem> createItems() {
        // TODO: Make better browser, a customizable one
        Player<? extends Track> mainPlayer = manager.getMainPlayer();

        if(mainPlayer == null) return Collections.emptyList();

        Utils.log("Sending queue items to the MediaBrowser...");

        List<MediaItem> items = new ArrayList<>();

        for(Track track : mainPlayer.getQueue()) {
            MediaDescriptionCompat desc = new MediaDescriptionCompat.Builder()
                    .setMediaId(track.id)
                    .setMediaUri(Utils.toUri(service, track.url.url, track.url.local))
                    .setIconUri(Utils.toUri(service, track.artwork.url, track.artwork.local))
                    .setTitle(track.title)
                    .setSubtitle(track.artist)
                    .setDescription(track.album)
                    .build();

            items.add(new MediaItem(desc, MediaItem.FLAG_PLAYABLE));
        }

        return items;
    }
}
