package guichaguri.trackplayer.player.players;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.media.session.PlaybackStateCompat;
import com.facebook.react.bridge.Promise;
import com.google.android.gms.cast.MediaQueueItem;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.cast.framework.media.RemoteMediaClient.MediaChannelResult;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import guichaguri.trackplayer.cast.GoogleCast;
import guichaguri.trackplayer.logic.MediaManager;
import guichaguri.trackplayer.logic.Utils;
import guichaguri.trackplayer.logic.track.Track;
import guichaguri.trackplayer.player.Playback;
import guichaguri.trackplayer.player.components.CastCallbackTrigger;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Remote player using {@link RemoteMediaClient}
 *
 * @author Guilherme Chaguri
 */
public class CastPlayback extends Playback implements RemoteMediaClient.Listener {

    private final GoogleCast cast;
    private final CastSession session;
    private final RemoteMediaClient player;

    private boolean loading = false;
    private int lastKnownState = MediaStatus.PLAYER_STATE_UNKNOWN;
    private int lastKnownIdleReason = MediaStatus.IDLE_REASON_NONE;
    private long lastKnownPosition = 0;

    public CastPlayback(Context context, MediaManager manager, GoogleCast cast, CastSession session) {
        super(context, manager);
        this.cast = cast;
        this.session = session;

        player = session.getRemoteMediaClient();
        player.addListener(this);
    }

    private boolean isConnected() {
        return cast.isCurrentSession(session);
    }

    private void addCallback(PendingResult<MediaChannelResult> r, Promise callback, Object ... data) {
        r.setResultCallback(new CastCallbackTrigger(callback, data));
    }

    private void updateCurrentTrackClient(int castId) {
        // Updates the current track client-side (or "sender-side")
        for(int i = 0; i < queue.size(); i++) {
            if(queue.get(i).castId == castId) {
                if(i == currentTrack) return;
                currentTrack = i;
                manager.onTrackUpdate();
                break;
            }
        }
    }

    @Override
    protected void updateCurrentTrack(Promise callback) {
        // Updates the current track server-side (or "receiver-side")
        Track track = queue.get(currentTrack);
        if(track != null) {
            addCallback(player.queueJumpToItem(track.castId, null), callback);
        } else {
            RuntimeException ex = new RuntimeException("Track not found");
            Utils.rejectCallback(callback, ex);
            manager.onError(ex);
        }
    }

    @Override
    public void add(String insertBeforeId, List<Track> tracks, Promise callback) {
        int indexId = MediaQueueItem.INVALID_ITEM_ID;

        if(insertBeforeId != null) {
            int index = queue.size();

            for(int i = 0; i < queue.size(); i++) {
                Track track = queue.get(i);
                if(track.id.equals(insertBeforeId)) {
                    index = i;
                    indexId = track.castId;
                    break;
                }
            }

            queue.addAll(index, tracks);
            if(index <= currentTrack) currentTrack += tracks.size();
        } else {
            queue.addAll(tracks);
        }

        MediaQueueItem[] items = new MediaQueueItem[tracks.size()];

        for(int i = 0; i < tracks.size(); i++) {
            items[i] = tracks.get(i).toCastQueueItem();
        }

        addCallback(player.queueInsertItems(items, indexId, null), callback);
    }

    @Override
    public void remove(String[] ids, final Promise callback) {
        final Track currentTrack = getCurrentTrack();

        ListIterator<Track> i = queue.listIterator();
        int[] trackIds = new int[ids.length];
        int o = 0;

        while(i.hasNext()) {
            Track track = i.next();
            for(String id : ids) {
                if(track.id.equals(id)) {
                    trackIds[o++] = track.castId;
                    i.remove();
                    break;
                }
            }
        }

        PendingResult<MediaChannelResult> result = player.queueRemoveItems(trackIds, null);

        result.setResultCallback(new ResultCallback<MediaChannelResult>() {
            @Override
            public void onResult(@NonNull MediaChannelResult result) {
                Utils.resolveCallback(callback);

                int id = player.getCurrentItem().getItemId();
                if(id != currentTrack.castId) {
                    updateCurrentTrackClient(id);
                }
            }
        });
    }

    @Override
    public void skipToNext(Promise callback) {
        addCallback(player.queueNext(null), callback);
    }

    @Override
    public void skipToPrevious(Promise callback) {
        addCallback(player.queuePrev(null), callback);
    }

    @Override
    public void skip(String id, Promise callback) {
        for(Track track : queue) {
            if(track.id.equals(id)) {
                addCallback(player.queueJumpToItem(track.castId, null), callback);
                return;
            }
        }

        Utils.rejectCallback(callback, "skip", "The track was not found");
    }

    @Override
    public void load(Track track, Promise callback) {
        // NOOP
        Utils.resolveCallback(callback);
    }

    @Override
    public void reset() {
        player.stop();

        List<MediaQueueItem> items = player.getMediaStatus().getQueueItems();
        int[] ids = new int[items.size()];
        for(int i = 0; i < items.size(); i++) {
            ids[i] = items.get(i).getItemId();
        }
        player.queueRemoveItems(ids, null);

        super.reset();

        manager.onPlaybackUpdate();
    }

    @Override
    public void play() {
        loading = true;
        player.play();
    }

    @Override
    public void pause() {
        player.pause();
    }

    @Override
    public void stop() {
        player.stop();
    }

    @Override
    public int getState() {
        if(isConnected()) {
            MediaStatus status = player.getMediaStatus();
            lastKnownState = status != null ? status.getPlayerState() : lastKnownState;
            lastKnownIdleReason = status != null ? status.getIdleReason() : lastKnownIdleReason;
        }

        switch(lastKnownState) {
            case MediaStatus.PLAYER_STATE_IDLE:
                if(lastKnownIdleReason == MediaStatus.IDLE_REASON_FINISHED) {
                    return PlaybackStateCompat.STATE_STOPPED;
                } else {
                    return PlaybackStateCompat.STATE_NONE;
                }
            case MediaStatus.PLAYER_STATE_BUFFERING:
                return PlaybackStateCompat.STATE_BUFFERING;
            case MediaStatus.PLAYER_STATE_PLAYING:
                return PlaybackStateCompat.STATE_PLAYING;
            case MediaStatus.PLAYER_STATE_PAUSED:
                return PlaybackStateCompat.STATE_PAUSED;
        }
        return PlaybackStateCompat.STATE_NONE;
    }

    @Override
    public long getPosition() {
        if(isConnected()) {
            lastKnownPosition = player.getApproximateStreamPosition();
        }
        return lastKnownPosition;
    }

    @Override
    public long getBufferedPosition() {
        return 0; // Unknown :/
    }

    @Override
    public long getDuration() {
        return player.getStreamDuration();
    }

    @Override
    public void seekTo(long ms) {
        player.seek(ms);
    }

    @Override
    public float getSpeed() {
        MediaStatus status = player.getMediaStatus();
        return status != null ? (float)status.getPlaybackRate() : 1;
    }

    @Override
    public void setVolume(float volume) {
        player.setStreamVolume(volume);
    }

    @Override
    public float getVolume() {
        MediaStatus status = player.getMediaStatus();
        return status != null ? (float)status.getStreamVolume() : 1;
    }

    @Override
    public boolean isRemote() {
        return true;
    }

    @Override
    public void updateData() {
        lastKnownPosition = player.getApproximateStreamPosition();
    }

    @Override
    public void copyPlayback(Playback playback) {
        // Copy everything to the new playback
        List<Track> oldQueue = playback.getQueue();
        if(oldQueue.isEmpty()) return;

        final Track currTrack = playback.getCurrentTrack();
        final long pos = playback.getPosition();

        MediaQueueItem[] castQueue = new MediaQueueItem[oldQueue.size()];

        for(int i = 0; i < castQueue.length; i++) {
            castQueue[i] = oldQueue.get(i).toCastQueueItem();
        }

        // TODO test
        player.queueInsertItems(castQueue, MediaQueueItem.INVALID_ITEM_ID, null)
                .setResultCallback(new ResultCallback<MediaChannelResult>() {
            @Override
            public void onResult(@NonNull MediaChannelResult mediaChannelResult) {
                if(currTrack != null) {
                    player.queueJumpToItem(currTrack.castId, pos, null);
                }
            }
        });
    }

    @Override
    public void destroy() {
        if(isConnected()) {
            cast.disconnect(true);
        }
    }

    @Override
    public void onStatusUpdated() {
        int state = getState();
        updateState(state);

        if(state == PlaybackStateCompat.STATE_STOPPED) {
            manager.onEnd();
        } else if(prevState == PlaybackStateCompat.STATE_BUFFERING && state != PlaybackStateCompat.STATE_BUFFERING) {
            if(loading) manager.onLoad(getCurrentTrack());
            loading = false;
        }
    }

    @Override
    public void onMetadataUpdated() {
        manager.onTrackUpdate();
    }

    @Override
    public void onQueueStatusUpdated() {
        MediaStatus status = player.getMediaStatus();
        if(status == null) return;

        List<Track> newQueue = new ArrayList<>();
        boolean queueChanged = false;
        int i = 0;

        queueLoop: for(MediaQueueItem item : status.getQueueItems()) {
            String id = item.getMedia().getContentId();

            for(; i < queue.size(); i++) {
                Track track = queue.get(i);

                if(track.mediaId.equals(id)) {
                    track.castId = item.getItemId();
                    track.castQueueItem = item;
                    newQueue.add(track);
                    continue queueLoop;
                }
            }

            newQueue.add(new Track(manager, item));
            queueChanged = true;
        }

        if(queueChanged) {
            queue = newQueue;
            manager.onTrackUpdate();
        }

        updateCurrentTrackClient(status.getCurrentItemId());
    }

    @Override
    public void onPreloadStatusUpdated() {

    }

    @Override
    public void onSendingRemoteMediaRequest() {

    }

    @Override
    public void onAdBreakStatusUpdated() {

    }
}
