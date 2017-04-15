package guichaguri.trackplayer.player.players;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.media.session.PlaybackStateCompat;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableMap;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaQueueItem;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.RemoteMediaPlayer;
import com.google.android.gms.cast.RemoteMediaPlayer.MediaChannelResult;
import com.google.android.gms.cast.RemoteMediaPlayer.OnQueueStatusUpdatedListener;
import com.google.android.gms.cast.RemoteMediaPlayer.OnStatusUpdatedListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.images.WebImage;
import guichaguri.trackplayer.logic.MediaManager;
import guichaguri.trackplayer.logic.Utils;
import guichaguri.trackplayer.logic.track.Track;
import guichaguri.trackplayer.remote.Chromecast;
import guichaguri.trackplayer.player.RemotePlayer;
import guichaguri.trackplayer.player.components.CastCallbackTrigger;
import guichaguri.trackplayer.player.track.CastTrack;
import java.util.List;
import java.util.ListIterator;

/**
 * Remote player using {@link RemoteMediaPlayer}
 *
 * @author Guilherme Chaguri
 */
public class CastPlayer extends RemotePlayer<CastTrack> implements OnStatusUpdatedListener, OnQueueStatusUpdatedListener {

    private final Chromecast cast;
    private final GoogleApiClient client;
    private final RemoteMediaPlayer player;

    private boolean playing = false;

    public CastPlayer(Context context, Chromecast cast, MediaManager manager, GoogleApiClient client) {
        super(context, manager);
        this.cast = cast;
        this.client = client;

        player = new RemoteMediaPlayer();
        player.setOnStatusUpdatedListener(this);
        player.setOnQueueStatusUpdatedListener(this);
    }

    @Override
    protected CastTrack createTrack(ReadableMap data) {
        return new CastTrack(manager, data);
    }

    @Override
    protected CastTrack createTrack(Track track) {
        return new CastTrack(track);
    }

    @Override
    protected void updateCurrentTrack(Promise callback) {
        CastTrack track = queue.get(currentTrack);
        if(track != null) {
            addCallback(player.queueJumpToItem(client, track.queueId, null), callback);
        } else {
            RuntimeException ex = new RuntimeException("Track not found");
            Utils.rejectCallback(callback, ex);
            manager.onError(this, ex);
        }
    }

    private MediaInfo createInfo(CastTrack track) {
        MediaMetadata metadata = new MediaMetadata();
        metadata.putString(MediaMetadata.KEY_TITLE, track.title);
        metadata.putString(MediaMetadata.KEY_ARTIST, track.artist);
        metadata.putString(MediaMetadata.KEY_ALBUM_TITLE, track.album);
        metadata.putDate(MediaMetadata.KEY_RELEASE_DATE, metadata.getDate(track.date));
        metadata.addImage(new WebImage(Utils.toUri(context, track.artwork.url, track.artwork.local)));

        return new MediaInfo.Builder(track.mediaId)
                .setStreamDuration(track.duration)
                .setContentType(track.contentType)
                .setStreamType(MediaInfo.STREAM_TYPE_INVALID)
                .setMetadata(metadata)
                .setCustomData(track.customData)
                .build();
    }

    private void addCallback(PendingResult<MediaChannelResult> r, Promise callback, Object ... data) {
        r.setResultCallback(new CastCallbackTrigger(callback, data));
    }

    @Override
    public void add(String insertBeforeId, List<CastTrack> tracks, Promise callback) {
        super.add(insertBeforeId, tracks, null);

        int indexId = MediaQueueItem.INVALID_ITEM_ID;
        if(insertBeforeId != null) {
            for(int i = 0; i < queue.size(); i++) {
                CastTrack track = queue.get(i);
                if(track.id.equals(insertBeforeId)) {
                    indexId = track.queueId;
                    break;
                }
            }
        }

        MediaQueueItem[] items = new MediaQueueItem[tracks.size()];

        for(int i = 0; i < tracks.size(); i++) {
            items[i] = new MediaQueueItem.Builder(createInfo(tracks.get(i)))
                    .setAutoplay(false).build();
        }

        addCallback(player.queueInsertItems(client, items, indexId, null), callback);
    }

    @Override
    public void remove(String[] ids, Promise callback) {
        ListIterator<CastTrack> i = queue.listIterator();
        boolean trackChanged = false;
        int[] trackIds = new int[ids.length];
        int o = 0;

        while(i.hasNext()) {
            int index = i.nextIndex();
            CastTrack track = i.next();
            for(String id : ids) {
                if(track.id.equals(id)) {

                    trackIds[o++] = track.queueId;
                    i.remove();
                    if(currentTrack == index) {
                        currentTrack = i.nextIndex();
                        trackChanged = true;
                    }
                    break;
                }
            }
        }

        addCallback(player.queueRemoveItems(client, trackIds, null), callback);
        if(trackChanged) updateCurrentTrack(null);
    }

    @Override
    public void skipToNext(Promise callback) {
        addCallback(player.queueNext(client, null), callback);
    }

    @Override
    public void skipToPrevious(Promise callback) {
        addCallback(player.queuePrev(client, null), callback);
    }

    @Override
    public void load(final CastTrack track, final Promise callback) {
        PendingResult<MediaChannelResult> result = player.load(client, createInfo(track));

        result.setResultCallback(new ResultCallback<MediaChannelResult>() {
            @Override
            public void onResult(@NonNull MediaChannelResult result) {
                Utils.resolveCallback(callback);
                manager.onLoad(CastPlayer.this, track);
            }
        });
    }

    @Override
    public void reset() {
        player.stop(client);

        List<MediaQueueItem> items = player.getMediaStatus().getQueueItems();
        int[] ids = new int[items.size()];
        for(int i = 0; i < items.size(); i++) {
            ids[i] = items.get(i).getItemId();
        }
        player.queueRemoveItems(client, ids, null);

        super.reset();

        playing = false;
        updateMetadata();
    }

    @Override
    public void play() {
        player.play(client);
    }

    @Override
    public void pause() {
        player.pause(client);
    }

    @Override
    public void stop() {
        player.stop(client);
    }

    @Override
    public int getState() {
        MediaStatus status = player.getMediaStatus();
        int state = status != null ? status.getPlayerState() : MediaStatus.PLAYER_STATE_UNKNOWN;

        switch(state) {
            case MediaStatus.PLAYER_STATE_IDLE:
                return playing ? PlaybackStateCompat.STATE_STOPPED : PlaybackStateCompat.STATE_NONE;
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
        return player.getApproximateStreamPosition();
    }

    @Override
    public long getBufferedPosition() {
        return 0;
    }

    @Override
    public long getDuration() {
        return player.getStreamDuration();
    }

    @Override
    public void seekTo(long ms) {
        player.seek(client, ms);
    }

    @Override
    public float getSpeed() {
        MediaStatus status = player.getMediaStatus();
        return status != null ? (float)status.getPlaybackRate() : 1;
    }

    @Override
    public void setVolume(float volume) {
        player.setStreamVolume(client, volume);
    }

    @Override
    public float getVolume() {
        MediaStatus status = player.getMediaStatus();
        if(status != null) {
            return (float)status.getStreamVolume();
        } else {
            return (float)Cast.CastApi.getVolume(client);
        }
    }

    @Override
    public boolean canChangeVolume() {
        return true;
    }

    @Override
    public void destroy() {
        cast.disconnect();
    }

    @Override
    public void onStatusUpdated() {
        int state = getState();
        updateState(state);

        if(Utils.isPlaying(state) || Utils.isPaused(state)) {
            playing = true;
        }

        if(state == PlaybackStateCompat.STATE_STOPPED) {
            manager.onEnd(this);
        }
    }

    @Override
    public void onQueueStatusUpdated() {
        MediaStatus status = player.getMediaStatus();
        if(status == null) return;

        int currentTrackId = status.getCurrentItemId();
        int oldTrack = currentTrack;

        for(MediaQueueItem item : status.getQueueItems()) {
            String id = item.getMedia().getContentId();

            for(int i = 0; i < queue.size(); i++) {
                CastTrack track = queue.get(i);

                if(track.mediaId.equals(id)) {
                    track.queueId = item.getItemId();

                    if(track.queueId == currentTrackId) {
                        currentTrack = i;
                    }
                    break;
                }
            }
        }

        if(currentTrack != oldTrack) updateMetadata();
    }

    public void onVolumeChanged() {
        updateMetadata();
    }
}
